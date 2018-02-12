package net.coderodde.msc.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
 * This is a basic iterative random learner for the Monte Carlo learners.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 14, 2018)
 */
public final class BasicIterativeRandomLearner<C> 
extends AbstractParsimoniousContextTreeLearner<C>{

    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    private List<DataRow<C>> dataRows;
    
    private Map<C, Integer> characterCountMap;
    
    private Deque<ParsimoniousContextTreeNode<C>> queue;
    
    private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap;
    
    private double k;
    
    private Random random;
    
    private int maximumLabelsPerNode;
    
    public void setMaximumLabelsPerNode(int maximumLabelsPerNode) {
        this.maximumLabelsPerNode = maximumLabelsPerNode;
    }
    
    public void setRandom(Random random) {
        this.random = Objects.requireNonNull(random);
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        Objects.requireNonNull(listOfDataRows);
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        
        BasicIterativeRandomLearner<C> state = 
                new BasicIterativeRandomLearner<>();
        
        state.random = random;
        state.dataRows = listOfDataRows;
        state.alphabet = getAlphabet(listOfDataRows);
        state.k = 0.5 * (state.alphabet.size() - 1) * 
                         Math.log(listOfDataRows.size());
        state.characterCountMap = new HashMap<>();
        state.queue = new ArrayDeque<>();
        state.depthMap = new HashMap<>();
        state.maximumLabelsPerNode = maximumLabelsPerNode;
        state.root = state.buildTree();
        state.computeScores();
        return new ParsimoniousContextTree<>(state.root);
    }
    
    private ParsimoniousContextTreeNode<C> buildTree() {
        int depth = dataRows.get(0).getNumberOfExplanatoryVariables();
        ParsimoniousContextTreeNode<C> root = 
                new ParsimoniousContextTreeNode<>();
        
        root.setLabel(new HashSet<>());
        root.setChildren(createChildren(depth - 1));
        
        return root;
    }
    
    private Set<ParsimoniousContextTreeNode<C>> createChildren(int depth) {
        int childCount = random.nextInt(alphabet.size()) + 1;
        childCount = Math.min(childCount, maximumLabelsPerNode);
        Set<ParsimoniousContextTreeNode<C>> children = 
                new HashSet<>(childCount);
        
        List<Set<C>> labels = new ArrayList<>(childCount);
        
        for (int i = 0; i < childCount; ++i) {
            labels.add(new HashSet<>());
        }
        
        for (C character : alphabet) {
            int labelIndex = random.nextInt(labels.size());
            labels.get(labelIndex).add(character);
        }
        
        for (Set<C> label : labels) {
            if (label.isEmpty()) {
                continue;
            }
            
            ParsimoniousContextTreeNode<C> node = 
                    new ParsimoniousContextTreeNode<>();
            node.setLabel(label);
            children.add(node);
        }
        
        if (depth > 0) {
            for (ParsimoniousContextTreeNode<C> node : children) {
                node.setChildren(createChildren(depth - 1));
            }
        }
        
        return children;
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
    
    //// BENCHMARK ////
    private static final int NUMBER_OF_DATA_ROWS = 1000;
    private static final int NUMBER_OF_EXPLANATORY_VARIABLES = 3;
    private static final int ALPHABET_SIZE = 4;
    
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
        BasicParsimoniousContextTreeLearner<Integer> learner = 
                new BasicParsimoniousContextTreeLearner<>();
        
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
        BasicParsimoniousContextTreeLearner<Integer> learner = 
                new BasicParsimoniousContextTreeLearner<>();
        
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
