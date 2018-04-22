package net.coderodde.msc.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;

/**
 * This class implements a basic algorithm for learning parsimonious context 
 * trees.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 15, 2017)
 * @param <C> the character type.
 */
public final class BasicParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    /**
     * Specifies the maximum alphabet size at which the algorithm can switch to
     * optimal search.
     */
    private static final int MAXIMUM_ALPHABET_SIZE = 13;
    
    /**
     * Maps an index to a corresponding Bell number.
     */
    private static final int[] BELL_NUMBERS = 
            new int[MAXIMUM_ALPHABET_SIZE + 1];
    
    static {
        BELL_NUMBERS[0]  = 1;
        BELL_NUMBERS[1]  = 1;
        BELL_NUMBERS[2]  = 2;
        BELL_NUMBERS[3]  = 5;
        BELL_NUMBERS[4]  = 15;
        BELL_NUMBERS[5]  = 52;
        BELL_NUMBERS[6]  = 203;
        BELL_NUMBERS[7]  = 877;
        BELL_NUMBERS[8]  = 4140;
        BELL_NUMBERS[9]  = 21147;
        BELL_NUMBERS[10] = 115975;
        BELL_NUMBERS[11] = 678570;
        BELL_NUMBERS[12] = 4213597;
        BELL_NUMBERS[13] = 27644437;
    }
    
    /**
     * Maps a single character to its absolute frequency.
     */
    private Map<C, Integer> characterCountMap;
    
    /**
     * Holds all possible node labels (proper and improper subsets of the 
     * alphabet.
     */
    private List<Set<C>> listOfAllPossibleNodeLabels;
    
    /**
     * The root node of the resulting tree.
     */
    private ParsimoniousContextTreeNode<C> root;
    
    /**
     * The leaf node penalty.
     */
    private double k;
    
    /**
     * The alphabet to use.
     */
    private Alphabet<C> alphabet;
    
    /**
     * Maps each partition of the alphabet to its score.
     */
    private Map<List<Set<C>>, Double> mapPartitionToScore;
    
    /**
     * This list holds all the partitions of the alphabet.
     */
    private List<List<Set<C>>> listOfAllAlphabetPartitions;
    
    @Override
    public ParsimoniousContextTree<C> 
        learn(List<DataRow<C>> listOfDataRows) {
           
        // Build internals:
        BasicParsimoniousContextTreeLearner<C> state = 
                new BasicParsimoniousContextTreeLearner<>();
        
        state.alphabet = getAlphabet(listOfDataRows);
        
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        
        state.listOfAllPossibleNodeLabels =
                state.alphabet.getAllPossibleLabels();
        
        state.characterCountMap = new HashMap<>();
        state.mapPartitionToScore = new HashMap<>();
        state.root = new ParsimoniousContextTreeNode<>();
        state.root.setLabel(Collections.<C>emptySet());
        state.k = 0.5 * (state.alphabet.size() - 1) * 
                         Math.log(listOfDataRows.size());
        state.generateAllAlphabetPartitions();
        int depth = listOfDataRows.get(0).getNumberOfExplanatoryVariables();
        
        state.buildTree(state.root, depth, depth, listOfDataRows);
        
        long nodes = getNumberOfNodesInTree(depth, state.alphabet.size());
        long workEstimatePerNode = getNodeWorkEstimate(MAXIMUM_ALPHABET_SIZE);
        
//        System.out.println("DEBUG: nodes = " + nodes);
//        System.out.println("DEBUG: work estimate = " + workEstimatePerNode);
//        System.out.println("DEBUG: total work  = " + nodes * workEstimatePerNode);
        
        return new ParsimoniousContextTree<>(state.root);
    }
        
    private void generateAllAlphabetPartitions() {
        this.listOfAllAlphabetPartitions = new ArrayList<>();
        
        for (int blocks = 1; blocks <= this.alphabet.size(); ++blocks) {
            PartitionIterable<C> iterable = 
                    new PartitionIterable<>(this.alphabet.getCharacters(), 
                                            blocks);
            
            for (List<Set<C>> partition : iterable) {
                this.listOfAllAlphabetPartitions.add(partition);
            }
        }
    }
        
    private double computeScore(List<DataRow<C>> dataRows) {
        double score = -k;
        characterCountMap.clear();
        
        for (DataRow<C> dataRow : dataRows) {
            C responseVariable = dataRow.getResponseVariable();
            characterCountMap.put(
                    responseVariable, 
                    characterCountMap.getOrDefault(responseVariable, 0) + 1);
        }
        
        for (Map.Entry<C, Integer> entry : characterCountMap.entrySet()) {
            score += entry.getValue() * 
                     Math.log((1.0 * entry.getValue()) / dataRows.size());
        }
        
        return score;
    }
    
    private void buildTree(ParsimoniousContextTreeNode<C> node, 
                           int currentDepth,
                           int totalDepth,
                           List<DataRow<C>> dataRows) {
        if (currentDepth == 0) {
            node.setScore(computeScore(dataRows));
            return;
        }
        
        Set<ParsimoniousContextTreeNode<C>> children = 
                new HashSet<>(this.alphabet
                                  .getNumberOfNonemptyCharacterCombinations());
        
        node.setChildren(children);
        Map<Set<C>, ParsimoniousContextTreeNode<C>> nodeMap =
                new HashMap<>(
                    this.alphabet.getNumberOfNonemptyCharacterCombinations());
        
        Map<ParsimoniousContextTreeNode<C>, 
            List<DataRow<C>>> nodeToDataMap = new HashMap<>();
        
        // Iterates O(2^n) times where n is the alphabet size.
        for (Set<C> label : this.listOfAllPossibleNodeLabels) {
            ParsimoniousContextTreeNode<C> childNode =
                    new ParsimoniousContextTreeNode<>();
            
            childNode.setLabel(label);
            nodeMap.put(label, childNode);
            children.add(childNode);
            nodeToDataMap.put(childNode, new ArrayList<>());
        }
        
        // Maps each alphabet character to the list of PCT nodes whose labels
        // contain the character in question:
        Map<C, List<ParsimoniousContextTreeNode<C>>> mapCharToNodes = 
                new HashMap<>();
        
        // Iterates in O(2^n) times, n = alphabet size.The inner loop runs in
        // O(n 2^n) time.
        for (ParsimoniousContextTreeNode<C> tmpNode : children) {
            for (C ch : tmpNode.getLabel()) {
                if (!mapCharToNodes.containsKey(ch)) {
                    mapCharToNodes.put(ch, new ArrayList<>());
                }
                
                mapCharToNodes.get(ch).add(tmpNode);
            }
        }
        
        int charIndex = totalDepth - currentDepth;
        
        // Redistributes the input data rows over all the children nodes:
        for (DataRow<C> dataRow : dataRows) {
            C ch = dataRow.getExplanatoryVariable(charIndex);
            List<ParsimoniousContextTreeNode<C>> tmpNodes = 
                    mapCharToNodes.get(ch);
            
            for (ParsimoniousContextTreeNode<C> tmpNode : tmpNodes) {
                nodeToDataMap.get(tmpNode).add(dataRow);
            }
        }
        
        // Recur to build each child node:
        for (ParsimoniousContextTreeNode<C> child : children) {
            buildTree(child, 
                      currentDepth - 1,
                      totalDepth, 
                      nodeToDataMap.get(child));
        }
        
        this.mapPartitionToScore.clear();
        // Enumerate all valid partitions and compute the score of each 
        // partition. Runs in \sigma B_\sigma time.
        for (List<Set<C>> alphabetPartition :
                this.listOfAllAlphabetPartitions) {
            double score = 0.0;
            
            for (Set<C> label : alphabetPartition) {
                score += nodeMap.get(label).getScore();
            }
            
            this.mapPartitionToScore.put(alphabetPartition, score);
        }
        
        double bestScore = Double.NEGATIVE_INFINITY;
        List<Set<C>> bestPartition = null;
        
        for (Map.Entry<List<Set<C>>, Double> entry :
                this.mapPartitionToScore.entrySet()) {
            if (bestScore < entry.getValue()) {
                bestScore = entry.getValue();
                bestPartition = entry.getKey();
            }
        }   
        
        node.setScore(bestScore);
        
        Set<Set<C>> bestPartitionAsSet = new HashSet<>(bestPartition);
        Iterator<ParsimoniousContextTreeNode<C>> iterator =
                node.getChildren().iterator();
        
        while (iterator.hasNext()) {
            ParsimoniousContextTreeNode<C> currentChildNode = iterator.next();
            
            if (!bestPartitionAsSet.contains(currentChildNode.getLabel())) {
                iterator.remove();
            }
        }
    }
    
    private static long getNumberOfNodesInTree(int depth, int alphabetSize) {
        long numberOfAlphabetCombinations = myPow(2, alphabetSize) - 1;
        long tmp = myPow(numberOfAlphabetCombinations, depth + 1);
        return (1 - tmp) / (1 - numberOfAlphabetCombinations);
    }
    
    private static long getNodeWorkEstimate(int alphabetSize) {
        return alphabetSize * BELL_NUMBERS[alphabetSize] + 
               alphabetSize * myPow(2, alphabetSize) +
               myPow(2, alphabetSize);
    }
    
    private static long myPow(long base, int exponent) {
        int result = 1;
        
        for (int i = 0; i < exponent; i++)  {
            result *= base;
        }
        
        return result;
    }
}
