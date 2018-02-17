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
        
        for (ParsimoniousContextTreeNode<C> tmpNode : children) {
            for (C ch : tmpNode.getLabel()) {
                if (!mapCharToNodes.containsKey(ch)) {
                    mapCharToNodes.put(ch, new ArrayList<>());
                }
                
                mapCharToNodes.get(ch).add(tmpNode);
            }
        }
        
        int charIndex = totalDepth - currentDepth;
        
        for (DataRow<C> dataRow : dataRows) {
            C ch = dataRow.getExplanatoryVariable(charIndex);
            List<ParsimoniousContextTreeNode<C>> tmpNodes = 
                    mapCharToNodes.get(ch);
            
            for (ParsimoniousContextTreeNode<C> tmpNode : tmpNodes) {
                nodeToDataMap.get(tmpNode).add(dataRow);
            }
        }
        
        for (ParsimoniousContextTreeNode<C> child : children) {
            buildTree(child, 
                      currentDepth - 1,
                      totalDepth, 
                      nodeToDataMap.get(child));
        }
        
        this.mapPartitionToScore.clear();
        // Enumerate all valid partitions, Google. + (paper).
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
    
//    public static void main(String[] args) {
//        BasicParsimoniousContextTreeLearner<Integer> learner = 
//                new BasicParsimoniousContextTreeLearner<>();
//        
//        BasicParsimoniousContextTreeLearnerV2<Integer> learnerV2 = 
//                new BasicParsimoniousContextTreeLearnerV2<>();
//        
//        List<DataRow<Integer>> data = new ArrayList<>();
//        data.add(new DataRow(1, 0, 1));
//        data.add(new DataRow(0, 1, 0));
//        data.add(new DataRow(1, 1, 0));
//        data.add(new DataRow(1, 0, 1));
//        data.add(new DataRow(1, 0, 1));
//        data.add(new DataRow(0, 0, 0));
//        System.out.println(learner.learn(data));
//        System.out.println(learnerV2.learn(data));
//    }
}
