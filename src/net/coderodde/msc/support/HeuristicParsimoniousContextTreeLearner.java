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
    }
    
    private void build(ParsimoniousContextTreeNode<C> parent,
                       int depth,
                       List<DataRow<C>> dataRows) {
        if (depth == 0) {
            return;
        }
        
        // Create the children list fo the parent node.
        List<ParsimoniousContextTreeNode<C>> childrenList = new ArrayList<>();
        
        for (C ch : alphabet.getCharacters()) {
            ParsimoniousContextTreeNode<C> child = 
                    new ParsimoniousContextTreeNode<>();
            Set<C> childLabel = new HashSet<>();
            childLabel.add(ch);
            child.setLabel(childLabel);
            childrenList.add(child);
            child.setScore(findScore(child, dataRows, depth));
        }
        
        double parentScore = 0.0;
        
        for (ParsimoniousContextTreeNode<C> node : childrenList) {
            parentScore += node.getScore();
        }
        
        double currentBestParentScore = parent.getScore();
        Set<ParsimoniousContextTreeNode<C>> bestPairSoFar = new HashSet<>(2);
        double mergedScore = Double.NaN;
        ParsimoniousContextTreeNode<C> child1 = null;
        ParsimoniousContextTreeNode<C> child2 = null;
        
        while (true) {
            boolean improved = false;
            // Try pairwise merging.
            for (int i = 0; i < childrenList.size(); i++) {
                child1 = childrenList.get(i);
                
                for (int j = i + 1; j < childrenList.size(); j++) {
                    child2 = childrenList.get(j);
                    mergedScore = findMergedScore(depth,
                                                  dataRows,
                                                  child1,
                                                  child2);
                    
                    if (currentBestParentScore < mergedScore) {
                        currentBestParentScore = mergedScore;
                        improved = true;
                        bestPairSoFar.clear();
                        bestPairSoFar.add(child1);
                        bestPairSoFar.add(child2);
                    }
                }
            }
            
            if (!improved) {
                // Build the children.
                // First split the data row list.
                Map<ParsimoniousContextTreeNode<C>, List<DataRow<C>>> map =
                        new HashMap<>();
                Map<C, ParsimoniousContextTreeNode<C>> map2 = 
                        new HashMap<>();
                
                for (ParsimoniousContextTreeNode<C> node : childrenList) {
                    if (!map.containsKey(node)) {
                        map.put(node, new ArrayList<>());
                    }
                }
                
                for (ParsimoniousContextTreeNode<C> node : childrenList) {
                    for (C ch : node.getLabel()) {
                        map2.put(ch, node);
                    }
                }
                
                int charIndex = 
                        dataRows.get(0).getNumberOfExplanatoryVariables() -
                        depth;
                
                for (DataRow<C> dataRow : dataRows) {
                    C ch = dataRow.getExplanatoryVariable(charIndex);
                    ParsimoniousContextTreeNode<C> child = map2.get(ch);
                    List<DataRow<C>> childDataRows = map.get(child);
                    childDataRows.add(dataRow);
                }
                
                for (ParsimoniousContextTreeNode<C> child : childrenList) {
                    build(child, depth - 1, map.get(child));
                }
            } else {
                // Merge:
                for (ParsimoniousContextTreeNode<C> node : bestPairSoFar) {
                    parent.setScore(parent.getScore() - node.getScore());
                }
                
                parent.setScore(parent.getScore() + mergedScore);
                
                parent.getChildren().removeAll(bestPairSoFar);
                childrenList.removeAll(bestPairSoFar);
                // Reuse child1:
                child1.getLabel().addAll(child2.getLabel());
                child1.setScore(mergedScore);
                childrenList.add(child1);
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
        Map<C, Integer> charCountMap = new HashMap<>();
        
        for (DataRow<C> dataRow : dataRows) {
            C ch = dataRow.getExplanatoryVariable(charIndex);
            
            if (node1.getLabel().contains(ch)
                    || node2.getLabel().contains(ch)) {
                count++;
                charCountMap.put(ch, charCountMap.getOrDefault(ch, 0) + 1);
            }
        }
        
        for (Map.Entry<C, Integer> entry : charCountMap.entrySet()) {
            score += entry.getValue() * Math.log(entry.getValue() / count);
        }
        
        return score;
    }
    
    private double findScore(ParsimoniousContextTreeNode<C> node,
                             List<DataRow<C>> dataRows, 
                             int depth) {
        int charIndex = 
                dataRows.get(0).getNumberOfExplanatoryVariables() - depth;
        
        int count = 0;
        double score = -k;
        Map<C, Integer> charCountMap = new HashMap<>();
        
        for (DataRow<C> dataRow : dataRows) {
            C ch = dataRow.getExplanatoryVariable(charIndex);
            
            if (node.getLabel().contains(ch)) {
                count++;
                charCountMap.put(ch, charCountMap.getOrDefault(ch, 0) + 1);
            }
        }
        
        for (Map.Entry<C, Integer> entry : charCountMap.entrySet()) {
            score += entry.getValue() * Math.log(entry.getValue() / count);
        }
        
        return score;
    }
    
    public static void main(String[] args) {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
        dataRows.add(new DataRow<>(1, 3, 2, 1));
        dataRows.add(new DataRow<>(3, 3, 1, 2));
        dataRows.add(new DataRow<>(2, 1, 3, 3));
        dataRows.add(new DataRow<>(1, 1, 2, 1));
        dataRows.add(new DataRow<>(2, 3, 3, 2));
        HeuristicParsimoniousContextTreeLearner<Integer> learner = 
                new HeuristicParsimoniousContextTreeLearner<>();
        
        learner.learn(dataRows);
    }
}
