package net.coderodde.msc.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;
import net.coderodde.msc.util.CombinationIterable;

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
     * Holds the list of data rows.
     */
    private List<DataRow<C>> listOfDataRows;
    
    /**
     * The frontier queue for the breadth-first search. We need this whenever
     * asking whether a data row leads to a particular leaf node.
     */
    private Deque<ParsimoniousContextTreeNode<C>> queue;
    
    /**
     * Maps each node to its depth in the breadth-first search.
     */
    private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap;
    
    /**
     * Maps each partition of the alphabet to its score.
     */
    private Map<List<Set<C>>, Double> mapPartitionToScore;
    
    /**
     * Whenever validating a partition candidate, this set is used to make sure 
     * that all characters are present in the candidate.
     */
    private Set<C> characterFilterSet;
    
    private Deque<ParsimoniousContextTreeNode<C>> debugStack = new ArrayDeque();
    
    @Override
    public ParsimoniousContextTree<C> 
        learn(List<DataRow<C>> listOfDataRows) {
           
        // Build internals:
        BasicParsimoniousContextTreeLearner<C> state = 
                new BasicParsimoniousContextTreeLearner<>();
        
        state.alphabet = getAlphabet(listOfDataRows);
        state.listOfDataRows = 
                Objects.requireNonNull(listOfDataRows, 
                                       "The list of data rows is null.");
        
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        
        state.listOfAllPossibleNodeLabels =
                state.alphabet.getAllPossibleLabels();
        
        state.characterFilterSet = new HashSet<>();
        state.depthMap = new HashMap<>();
        state.characterCountMap = new HashMap<>();
        state.mapPartitionToScore = new HashMap<>();
        state.queue = new ArrayDeque<>();
        state.root = new ParsimoniousContextTreeNode<>();
        state.root.setLabel(Collections.<C>emptySet());
        state.k = 0.5 * (state.alphabet.size() - 1) * 
                         Math.log(listOfDataRows.size());
        
        int depth = listOfDataRows.get(0).getNumberOfExplanatoryVariables();
        
        state.buildTree(state.root, depth);
        
        return new ParsimoniousContextTree<>(state.root);
    }
        
    private void buildTree(ParsimoniousContextTreeNode<C> node, int depth) {
        debugStack.addLast(node);
        
        if (depth == 0) {
            node.createCharacterCountMap();
            node.setScore(computeBayesianInformationCriterion(node));
            debugStack.removeLast();
            return;
        }
        
        Set<ParsimoniousContextTreeNode<C>> children = 
                new HashSet<>(this.alphabet
                                  .getNumberOfNonemptyCharacterCombinations());
        
        node.setChildren(children);
        Map<Set<C>, ParsimoniousContextTreeNode<C>> nodeMap =
                new HashMap<>(
                    this.alphabet.getNumberOfNonemptyCharacterCombinations());
        
        for (Set<C> label : this.listOfAllPossibleNodeLabels) {
            ParsimoniousContextTreeNode<C> childNode =
                    new ParsimoniousContextTreeNode<>();
            
            childNode.setLabel(label);
            nodeMap.put(label, childNode);
            children.add(childNode);
            buildTree(childNode, depth - 1);
        }
        
        this.mapPartitionToScore.clear();
        
        for (List<Set<C>> labelCombination :
                new CombinationIterable<Set<C>>(
                        this.listOfAllPossibleNodeLabels)) {
            if (!isPartitionOfAlphabet(labelCombination)) {
                continue;
            }
            
            double score = 0.0;
            
            for (Set<C> label : labelCombination) {
                score += nodeMap.get(label).getScore();
            }
            
            this.mapPartitionToScore.put(labelCombination, score);
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
        
        debugStack.removeLast();
    }
    
    private boolean isPartitionOfAlphabet(List<Set<C>> labelCombination) {
        int labels = labelCombination.size();
        
        for (int i = 0; i < labels; ++i) {
            Set<C> label1 = labelCombination.get(i);
            
            for (int j = i + 1; j < labels; ++j) {
                Set<C> label2 = labelCombination.get(j);
                
                if (!Collections.<C>disjoint(label1, label2)) {
                    return false;
                }
            }
        }
        
        this.characterFilterSet.clear();
        
        for (Set<C> label : labelCombination) {
            this.characterFilterSet.addAll(label);
        }
        
        return this.characterFilterSet.size() == this.alphabet.size();
    }
    
    private double computeBayesianInformationCriterion(
            ParsimoniousContextTreeNode<C> node) {
        this.characterCountMap.clear();
        int totalCount = 0;
        
        for (DataRow<C> dataRow : this.listOfDataRows) {
            if (dataRowMatchesLeafNode(dataRow, node)) {
                totalCount++;
                C responseVariable = dataRow.getResponseVariable();
                node.addToCharacterCountMap(responseVariable);
                //System.out.println("In BIC: choosing data row " + dataRow + " for " + debugStack);
                
                Integer count = this.characterCountMap.get(responseVariable);
                
                if (count != null) {
                    this.characterCountMap.put(responseVariable, count + 1);
                } else {
                    this.characterCountMap.put(responseVariable, 1);
                }
            }
        }
        
        double score = -this.k;
        
        for (Map.Entry<C, Integer> e : this.characterCountMap.entrySet()) {
            score += e.getValue() * 
                    Math.log((1.0 * e.getValue()) / totalCount);
        }

        return score;
    }
    
    private boolean dataRowMatchesLeafNode(
            DataRow<C> dataRow, 
            ParsimoniousContextTreeNode<C> leafNode) {
        this.queue.clear();
        this.depthMap.clear();
        int treeDepth = this.listOfDataRows
                            .get(0).getNumberOfExplanatoryVariables();

        for (ParsimoniousContextTreeNode<C> childOfRoot : 
                root.getChildren()) {
            if (childOfRoot.getLabel()
                           .contains(dataRow.getExplanatoryVariable(0))) {
                this.queue.addLast(childOfRoot);
                this.depthMap.put(childOfRoot, 1);
            }
        }

        while (!this.queue.isEmpty()) {
            ParsimoniousContextTreeNode<C> currentNode = 
                    this.queue.removeFirst();
            int currentNodeDepth = this.depthMap.get(currentNode);

            if (currentNodeDepth == treeDepth) {
                if (currentNode == leafNode) {
                    return true;
                }
            } else {
                C targetChar = dataRow.getExplanatoryVariable(currentNodeDepth);
                
                for (ParsimoniousContextTreeNode<C> child :
                        currentNode.getChildren()) {
                    if (child.getLabel().contains(targetChar)) {
                        this.queue.addLast(child);
                        this.depthMap.put(child, currentNodeDepth + 1);
                    }
                }
            }
        }
        
        return false;
    }
}
