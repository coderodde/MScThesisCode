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
 * This class implements the partial optimal PCT learner for the hybrid 
 * heuristic search.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 22, 2018)
 */
public final class PartialBasicParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    private int requestedStartDepthLevel = -1;
    private int requestedTreeDepth       = -1;
    
    /**
     * Maps a single character to its absolute frequency.
     */
    private Map<C, Integer> characterCountMap;
    
    /**
     * Holds all the possible node labels (proper and improper subsets of the 
     * alphabet.
     */
    private List<Set<C>> listOfAllPossibleNodeLabels;
    
    /**
     * The root node of the resulting tree.
     */
    private ParsimoniousContextTreeNode<C> root;
    
    /**
     * The leaf node penalty. TODO: Do I need this?
     */
    private double k;
    
    /**
     * The alphabet to use.
     */
    private Alphabet<C> alphabet;
    
    private Map<List<Set<C>>, Double> mapPartitionToScore;
    private List<List<Set<C>>> listOfAllAlphabetPartitions;
    
    public void setRequestedStartDepthLevel(int requestedStartDepthLevel) {
        this.requestedStartDepthLevel = requestedStartDepthLevel;
    }
    
    public void setRequestedTreeDepth(int requestedTreeDepth) {
        this.requestedTreeDepth = requestedTreeDepth;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> dataRows) {
        PartialBasicParsimoniousContextTreeLearner<C> state = 
                new PartialBasicParsimoniousContextTreeLearner<>();
        
        state.alphabet = getAlphabet(dataRows);
        
        checkDataRowListNotEmpty(dataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(dataRows);
        
        state.listOfAllPossibleNodeLabels = 
                state.alphabet.getAllPossibleLabels();
        
        state.characterCountMap = new HashMap<>();
        state.mapPartitionToScore = new HashMap<>();
        state.root = new ParsimoniousContextTreeNode<>();
        state.root.setLabel(Collections.<C>emptySet());
        state.k = 0.5 * (state.alphabet.size() - 1) * Math.log(dataRows.size());
        state.generateAllAlphabetPartitions();
        state.buildTree(state.root, 
                        dataRows,
                        requestedStartDepthLevel,
                        requestedStartDepthLevel + requestedTreeDepth);
        
        return new ParsimoniousContextTree<>(state.root);
    }
    
    private void buildTree(ParsimoniousContextTreeNode<C> node,
                           List<DataRow<C>> dataRows,
                           int currentDepth,
                           int totalDepth) {
        if (currentDepth == totalDepth) {
            node.setScore(computeScore(dataRows));
            return;
        }
        
        Set<ParsimoniousContextTreeNode<C>> children = 
                new HashSet<>(
                        this.alphabet
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
        
        for (DataRow<C> dataRow : dataRows) {
            C ch = dataRow.getExplanatoryVariable(currentDepth);
            List<ParsimoniousContextTreeNode<C>> tmpNodes =
                    mapCharToNodes.get(ch);
            
            for (ParsimoniousContextTreeNode<C> tmpNode : tmpNodes) {
                nodeToDataMap.get(tmpNode).add(dataRow);
            }
        }
        
        for (ParsimoniousContextTreeNode<C> child : children) {
            buildTree(child,
                      nodeToDataMap.get(child),
                      currentDepth + 1,
                      totalDepth);
        }
        
        this.mapPartitionToScore.clear();
        
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
    
    public static void main(String[] args) {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
        dataRows.add(new DataRow<>(1, 1, 2, 3, 3));
        dataRows.add(new DataRow<>(1, 2, 2, 1, 1));
        dataRows.add(new DataRow<>(2, 3, 1, 2, 2));
        dataRows.add(new DataRow<>(3, 1, 1, 2, 2));
        dataRows.add(new DataRow<>(3, 3, 3, 3, 1));
        
        PartialBasicParsimoniousContextTreeLearner<Integer> learner = 
                new PartialBasicParsimoniousContextTreeLearner<>();
        
        learner.setRequestedTreeDepth(2);
        learner.setRequestedStartDepthLevel(1);
        
        ParsimoniousContextTree<Integer> tree = learner.learn(dataRows);
        
        System.out.println(tree);
    }
}
