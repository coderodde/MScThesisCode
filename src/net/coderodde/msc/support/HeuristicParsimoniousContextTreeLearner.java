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
import java.util.Random;
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
        computeScores();
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
        computeScores(root, 
                      dataRows, 
                      dataRows.get(0).getNumberOfExplanatoryVariables());
    }
    
    private void computeScores(ParsimoniousContextTreeNode<C> node,
                                 List<DataRow<C>> data,
                                 int depth) {
        if (depth == 0) {
            Map<C, Integer> map = new HashMap<>();
            
            for (DataRow<C> dataRow : data) {
                C responseVariable = dataRow.getResponseVariable();
                map.put(responseVariable, 
                        map.getOrDefault(responseVariable, 0) + 1);
            }
            
            double score = -this.k;
            
            for (Map.Entry<C, Integer> entry : map.entrySet()) {
                score += entry.getValue() *
                         Math.log((1.0 * entry.getValue()) / data.size());
            }
            
            node.setScore(score);
            return;
        }
        
        Map<ParsimoniousContextTreeNode<C>, List<DataRow<C>>> map = 
                new HashMap<>();
        
        Map<C, ParsimoniousContextTreeNode<C>> map2 = new HashMap<>();
        
        for (ParsimoniousContextTreeNode<C> tmpNode : node.getChildren()) {
            for (C ch : tmpNode.getLabel()) {
                map2.put(ch, tmpNode);
            }
            
            map.put(tmpNode, new ArrayList<>());
        }
        
        int charIndex = data.get(0).getNumberOfExplanatoryVariables() - depth;
        
        for (DataRow<C> dataRow : data) {
            C ch = dataRow.getExplanatoryVariable(charIndex);
            ParsimoniousContextTreeNode<C> myNode = map2.get(ch);
            map.get(myNode).add(dataRow);
        }
        
        double score = 0.0;
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            computeScores(child, map.get(child), depth - 1);
            score += child.getScore();
        }
        
        node.setScore(score);
    }
    
    private static final int NUMBER_OF_DATA_ROWS = 10000;
    private static final int NUMBER_OF_EXPLANATORY_VARIABLES = 5;
    private static final int ALPHABET_SIZE = 5;
    
    public static void main(String[] args) {
//        benchmarkSmall();
        benchmarkLarge();
    }
    
    private static void benchmarkSmall() {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
        dataRows.add(new DataRow<>(1, 3, 2, 1));
        dataRows.add(new DataRow<>(3, 3, 1, 2));
        dataRows.add(new DataRow<>(2, 1, 3, 3));
        dataRows.add(new DataRow<>(1, 1, 2, 1));
        dataRows.add(new DataRow<>(2, 3, 3, 2));
//        dataRows.add(new DataRow<>(1, 3, 1));
//        dataRows.add(new DataRow<>(3, 3, 2));
//        dataRows.add(new DataRow<>(2, 1, 3));
//        dataRows.add(new DataRow<>(1, 1, 1));
//        dataRows.add(new DataRow<>(2, 3, 2));
        HeuristicParsimoniousContextTreeLearner<Integer> learner = 
                new HeuristicParsimoniousContextTreeLearner<>();
        
        ParsimoniousContextTree<Integer> tree = learner.learn(dataRows);
        System.out.println(tree);
    }
    
    private static void benchmarkLarge() {
        long seed = 100L; System.currentTimeMillis();
        Random random = new Random(seed);
        List<DataRow<Integer>> data = 
                createRandomData(NUMBER_OF_DATA_ROWS,
                                 NUMBER_OF_EXPLANATORY_VARIABLES,
                                 ALPHABET_SIZE,
                                 random);
        
        System.out.println("Seed = " + seed);
        HeuristicParsimoniousContextTreeLearner<Integer> learner = 
                new HeuristicParsimoniousContextTreeLearner<>();
        
        long startTime = System.currentTimeMillis();
        ParsimoniousContextTree<Integer> tree = learner.learn(data);
        long endTime = System.currentTimeMillis();
        
        System.out.println(tree);
        System.out.println(
                "Duration: " + (endTime - startTime) + " milliseconds.");
    }
    
    private static List<DataRow<Integer>> 
        createRandomData(int dataRows,
                         int numberOfExplanatoryVariables,
                         int alphabetSize,
                         Random random) {
        List<DataRow<Integer>> data = new ArrayList<>(dataRows);

        for (int i = 0; i < dataRows; i++) {
            data.add(createRandomDataRow(numberOfExplanatoryVariables, alphabetSize, random));
        }

        return data;
    }
        
    private static DataRow<Integer> 
        createRandomDataRow(int numberOfExplanatoryVariables,
                            int alphabetSize,
                            Random random) {
        Integer[] array = new Integer[numberOfExplanatoryVariables + 1];

        for (int i = 0; i < array.length; i++) {
            array[i] = createRandomValue(alphabetSize, random);
        }

        return new DataRow<>(array);
    }
        
    private static int createRandomValue(int alphabetSize, Random random) {
        return 1 + random.nextInt(alphabetSize);
    }
}
