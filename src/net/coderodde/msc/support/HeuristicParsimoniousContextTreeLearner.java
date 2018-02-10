package net.coderodde.msc.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;
import net.coderodde.msc.ResponseVariableDistribution;

/**
 * This class implements a heuristic PCT learner.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Feb 2, 2018)
 * @param <C> the character type.
 */
public final class HeuristicParsimoniousContextTreeLearner<C> 
extends AbstractParsimoniousContextTreeLearner<C> {

    private Alphabet<C> alphabet;
    private List<DataRow<C>> dataRows;
    private double k;
    private ParsimoniousContextTreeNode<C> root;
    private final Map<C, Integer> characterCountMap = new HashMap<>();
    private Deque<ParsimoniousContextTreeNode<C>> queue;
    private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap;
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        Objects.requireNonNull(listOfDataRows);
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        
        HeuristicParsimoniousContextTreeLearner<C> state = 
                new HeuristicParsimoniousContextTreeLearner<>();
        
        state.alphabet = getAlphabet(listOfDataRows);
        state.dataRows = listOfDataRows;
        state.k = 0.5 * (state.alphabet.size() - 1) * 
                         Math.log(listOfDataRows.size());
        state.depthMap = new HashMap<>();
        state.queue = new ArrayDeque<>();
        state.build();
        return new ParsimoniousContextTree<>(state.root);
    }
    
    private void build() {
        int depth = dataRows.get(0).getNumberOfExplanatoryVariables();
        root = new ParsimoniousContextTreeNode<>();
        root.setLabel(Collections.emptySet());
        build(root, depth, dataRows);
//        fixScores();
        computeScores();
    }
    
    private void fixScores() {
        root.setScore(fixScores(root));
    }
    
    private double fixScores(ParsimoniousContextTreeNode<C> node) {
        if (node.getChildren() == null) {
            return node.getScore();
        }
        
        double score = 0.0;
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            score += fixScores(child);
        }
        
        node.setScore(score);
        return score;
    }
    
    private List<ParsimoniousContextTreeNode<C>> createChildren() {
        List<ParsimoniousContextTreeNode<C>> childrenList = 
                new ArrayList<>(alphabet.size());
        
        for (C ch : alphabet.getCharacters()) {
            ParsimoniousContextTreeNode<C> child = 
                    new ParsimoniousContextTreeNode<>();
            Set<C> childLabel = new HashSet<>();
            childLabel.add(ch);
            child.setLabel(childLabel);
            childrenList.add(child);
            child.setScore(-k);
        }
        
        return childrenList;
    }
    
    private void build(ParsimoniousContextTreeNode<C> parent,
                       int depth,
                       List<DataRow<C>> dataRows) {
        if (depth == 0) {
            return;
        }
        
        // Create the children list fo the parent node.
        List<ParsimoniousContextTreeNode<C>> childrenList = createChildren();
        parent.setChildren(new HashSet<>(childrenList));        
        parent.setScore(-k * childrenList.size());
        ParsimoniousContextTreeNode<C> bestChild1 = null;
        ParsimoniousContextTreeNode<C> bestChild2 = null;
        ParsimoniousContextTreeNode<C> child1 = null;
        ParsimoniousContextTreeNode<C> child2 = null;
        
        while (true) {
            boolean improved = false;
            // Try pairwise merging.
            for (int i = 0; i < childrenList.size(); i++) {
                child1 = childrenList.get(i);
                
                for (int j = i + 1; j < childrenList.size(); j++) {
                    child2 = childrenList.get(j);
                    double mergedScore = findMergedScore(depth,
                                                         dataRows,
                                                         child1,
                                                         child2);
                    double candidateScore = 
                            parent.getScore() + mergedScore
                                              - child1.getScore()
                                              - child2.getScore();
                    
                    if (parent.getScore() < candidateScore) {
                        parent.setScore(candidateScore);
                        improved = true;
                        bestChild1 = child1;
                        bestChild2 = child2;
                    }
                }
            }
            
            if (!improved) {
                Map<ParsimoniousContextTreeNode<C>, 
                    List<DataRow<C>>> nodeToDataRowsMap = new HashMap<>();
                Map<C, ParsimoniousContextTreeNode<C>> charToNodeMap = 
                        new HashMap<>();
                
                // Build the children.
                // First split the data row list.
                for (ParsimoniousContextTreeNode<C> node : childrenList) {
                    nodeToDataRowsMap.put(node, new ArrayList<>());
                }
                
                for (ParsimoniousContextTreeNode<C> node : childrenList) {
                    for (C ch : node.getLabel()) {
                        charToNodeMap.put(ch, node);
                    }
                }
                
                int charIndex = 
                        dataRows.get(0).getNumberOfExplanatoryVariables() -
                        depth;
                
                for (DataRow<C> dataRow : dataRows) {
                    C ch = dataRow.getExplanatoryVariable(charIndex);
                    ParsimoniousContextTreeNode<C> child =
                            charToNodeMap.get(ch);
                    List<DataRow<C>> childDataRows = 
                            nodeToDataRowsMap.get(child);
                    childDataRows.add(dataRow);
                }
                
                for (ParsimoniousContextTreeNode<C> child : parent.getChildren()) {
                    build(child, depth - 1, nodeToDataRowsMap.get(child));
                }
                
                return;
            } else {
                // Merge:
                parent.getChildren().remove(bestChild2);
                childrenList.remove(bestChild2);
                // Reuse bestChild1:
                bestChild1.getLabel().addAll(bestChild2.getLabel());
            }
        }
    }
    
    private double findMergedScore(int depth,
                                   List<DataRow<C>> dataRows,
                                   ParsimoniousContextTreeNode<C> node1,
                                   ParsimoniousContextTreeNode<C> node2) {
        int charIndex = 
                dataRows.get(0).getNumberOfExplanatoryVariables() - depth;
        int count = 0;
        double score = -k;
        
        for (DataRow<C> dataRow : dataRows) {
            C ch = dataRow.getExplanatoryVariable(charIndex);
            
            if (node1.getLabel().contains(ch)
                    || node2.getLabel().contains(ch)) {
                count++;
                characterCountMap.put(
                        ch, 
                        characterCountMap.getOrDefault(ch, 0) + 1);
            }
        }
        
        for (Map.Entry<C, Integer> entry : characterCountMap.entrySet()) {
            score += entry.getValue() * Math.log(1.0 * entry.getValue() / count);
        }
        
        characterCountMap.clear();
        return score;
    }
    
    private void computeScores() {
        computeScores(root);
    }
    
    private void computeScores(ParsimoniousContextTreeNode<C> node) {
        if (node.getChildren() == null) {
            node.setScore(computeBayesianInformationCriterion(node));
            return;
        }
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            computeScores(child);
        }
        
        double score = 0.0;
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            score += child.getScore();
        }
        
        node.setScore(score);
    }
    
    private double computeBayesianInformationCriterion(
            ParsimoniousContextTreeNode<C> node) {
        this.characterCountMap.clear();
        int totalCount = 0;
        
        for (DataRow<C> dataRow : dataRows) {
            if (dataRowMatchesLeafNode(dataRow, node)) {
                totalCount++;
                C responseVariable = dataRow.getResponseVariable();
                Integer count = this.characterCountMap.get(responseVariable);
                
                if (count != null) {
                    this.characterCountMap.put(responseVariable, count + 1);
                } else {
                    this.characterCountMap.put(responseVariable, 1);
                }
            }
        }
        
        double score = -this.k;
        ResponseVariableDistribution<C> distribution = 
                new ResponseVariableDistribution<>();
        
        for (Map.Entry<C, Integer> e : this.characterCountMap.entrySet()) {
            score += e.getValue() * 
                    Math.log((1.0 * e.getValue()) / totalCount);
            distribution.putResponseVariableProbability(
                    e.getKey(), 
                    Double.valueOf(e.getValue()) / totalCount);
        }

        node.setResponseVariableDistribution(distribution);
        return score;
    }
    private boolean dataRowMatchesLeafNode(
            DataRow<C> dataRow, 
            ParsimoniousContextTreeNode<C> leafNode) {
        this.queue.clear();
        this.depthMap.clear();
        int treeDepth = this.dataRows.get(0).getNumberOfExplanatoryVariables();

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
    
    public static void main(String[] args) {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
//        dataRows.add(new DataRow<>(1, 3, 2, 1));
//        dataRows.add(new DataRow<>(3, 3, 1, 2));
//        dataRows.add(new DataRow<>(2, 1, 3, 3));
//        dataRows.add(new DataRow<>(1, 1, 2, 1));
//        dataRows.add(new DataRow<>(2, 3, 3, 2));
        dataRows.add(new DataRow<>(1, 3, 1));
        dataRows.add(new DataRow<>(3, 3, 2));
        dataRows.add(new DataRow<>(2, 1, 3));
        dataRows.add(new DataRow<>(1, 1, 1));
        dataRows.add(new DataRow<>(2, 3, 2));
        HeuristicParsimoniousContextTreeLearner<Integer> learner = 
                new HeuristicParsimoniousContextTreeLearner<>();
        
        ParsimoniousContextTree<Integer> tree = learner.learn(dataRows);
        System.out.println(tree);
    }
}
