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
import net.coderodde.msc.util.AbstractProbabilityDistribution;
import net.coderodde.msc.util.support.BinaryTreeProbabilityDistribution;

public final class RandomParsimoniousContextTreeLearner2<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    private AbstractProbabilityDistribution<Integer> bucketSizeDistribution;
    
    private List<DataRow<C>> dataRows;
    
    private Map<C, Integer> characterCountMap;
    
    private Deque<ParsimoniousContextTreeNode<C>> queue;
    
    private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap;
    
    private double k;
    
    private Random random;
    
    private final double beta;
    
    public void setRandom(Random random) {
        this.random = Objects.requireNonNull(random, "Random is null.");
    }
    
    public RandomParsimoniousContextTreeLearner2(double beta) {
        this.beta = beta;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        Objects.requireNonNull(listOfDataRows, "The data row list is null.");
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
    
        RandomParsimoniousContextTreeLearner2<C> state = 
                new RandomParsimoniousContextTreeLearner2<>(this.beta);
        
        state.random = random;
        state.dataRows = listOfDataRows;
        state.alphabet = getAlphabet(listOfDataRows);
        state.k = 0.5 * (state.alphabet.size() - 1)
                      * Math.log(listOfDataRows.size());
        state.bucketSizeDistribution = state.createBucketSizeDistribution();
        state.characterCountMap = new HashMap<>();
        state.queue = new ArrayDeque<>();
        state.depthMap = new HashMap<>();
        state.root = state.buildTree();
        state.computeScores();
        return new ParsimoniousContextTree<>(state.root);
    }
    
    private AbstractProbabilityDistribution<Integer> 
        createBucketSizeDistribution() {
        AbstractProbabilityDistribution<Integer> distribution = 
                new BinaryTreeProbabilityDistribution<>();
        double probability = beta;
        
        for (int bucketSize = 1; 
                bucketSize <= this.alphabet.size(); 
                bucketSize++) {
            distribution.addElement(bucketSize, probability);
            probability *= beta;
        }
        
        return distribution;
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
    
    private ParsimoniousContextTreeNode<C> buildTree() {
        int depth = dataRows.get(0).getNumberOfExplanatoryVariables();
        ParsimoniousContextTreeNode<C> root = 
                new ParsimoniousContextTreeNode<>();
        
        root.setLabel(new HashSet<>());
        root.setChildren(createChildren(depth - 1));
        
        return root;
    }
    
    private Set<ParsimoniousContextTreeNode<C>> createChildren(int depth) {
        Set<ParsimoniousContextTreeNode<C>> children = new HashSet<>();
        // Generate children set:
        List<Set<C>> labels = createRandomLabelSet();
        
        for (Set<C> label : labels) {
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
    
    private List<Set<C>> createRandomLabelSet() {
        int buckets = bucketSizeDistribution.sampleElement();
        List<Set<C>> labelList = new ArrayList<>();
        
        for (int i = 0; i < buckets; ++i) {
            labelList.add(new HashSet<>());
        }
        
        for (C character : alphabet) {
            int labelIndex = random.nextInt(buckets);
            labelList.get(labelIndex).add(character);
        }
        
        return labelList;
    }
    
    private boolean isPartition(Set<Set<C>> labelSet) {
        List<Set<C>> labelList = new ArrayList<>(labelSet);
        int labelListSize = labelList.size();
        
        for (int i = 0; i < labelListSize; ++i) {
            Set<C> label1 = labelList.get(i);
            
            for (int j = i + 1; j < labelListSize; ++j) {
                Set<C> label2 = labelList.get(j);
                
                if (!Collections.<C>disjoint(label1, label2)) {
                    return false;
                }
            }
        }
        
        Set<C> filter = new HashSet<>();
        
        for (Set<C> label : labelList) {
            filter.addAll(label);
        }
        
        return filter.size() == alphabet.size();
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
}