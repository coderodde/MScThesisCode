package net.coderodde.msc.support;

import java.util.ArrayList;
import java.util.Collections;
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
        state.build();
        return new ParsimoniousContextTree<>(state.root);
    }
    
    private void build() {
        int depth = dataRows.get(0).getNumberOfExplanatoryVariables();
        root = new ParsimoniousContextTreeNode<>();
        root.setLabel(Collections.emptySet());
        build(root, depth, dataRows);
        fixScores();
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
    
    public static void main(String[] args) {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
//        dataRows.add(new DataRow<>(3, 2, 1));
//        dataRows.add(new DataRow<>(3, 1, 2));
//        dataRows.add(new DataRow<>(1, 3, 3));
//        dataRows.add(new DataRow<>(1, 2, 1));
//        dataRows.add(new DataRow<>(3, 3, 2));
        dataRows.add(new DataRow<>(1, 3, 2, 1));
        dataRows.add(new DataRow<>(3, 3, 1, 2));
        dataRows.add(new DataRow<>(2, 1, 3, 3));
        dataRows.add(new DataRow<>(1, 1, 2, 1));
        dataRows.add(new DataRow<>(2, 3, 3, 2));
        HeuristicParsimoniousContextTreeLearner<Integer> learner = 
                new HeuristicParsimoniousContextTreeLearner<>();
        
        ParsimoniousContextTree<Integer> tree = learner.learn(dataRows);
        System.out.println(tree);
    }
}
