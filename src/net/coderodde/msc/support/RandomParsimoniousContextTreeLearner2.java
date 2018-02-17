package net.coderodde.msc.support;

import java.util.ArrayList;
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
import net.coderodde.msc.util.AbstractProbabilityDistribution;
import net.coderodde.msc.util.support.BinaryTreeProbabilityDistribution;

// Beta-driven number of children. One child with prob. beta, two childs with 
// prob. pow(beta,2), and so on.
/**
 * In this learner, the probability of having <code>n</code> children is 
 * <code>pow(beta,n)</code>.
 * 
 * @author rodionefremov
 * @param <C> 
 */
public final class RandomParsimoniousContextTreeLearner2<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    private AbstractProbabilityDistribution<Integer> bucketSizeDistribution;
    
    private List<DataRow<C>> dataRows;
    
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
        int treeDepth = dataRows.get(0).getNumberOfExplanatoryVariables();
        computeScores(root,
                      dataRows,
                      treeDepth,
                      treeDepth);
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
    
    private double computeBIC(List<DataRow<C>> dataRows) {
        double score = -this.k;
        Map<C, Integer> characterToCountMap = new HashMap<>();
        
        for (DataRow<C> dataRow : dataRows) {
            C responseVariable = dataRow.getResponseVariable();
            characterToCountMap.put(
                    responseVariable, 
                    characterToCountMap.getOrDefault(responseVariable, 0) + 1);
        }
        
        for (Map.Entry<C, Integer> entry : characterToCountMap.entrySet()) {
            score += entry.getValue() * 
                     Math.log((1.0 * entry.getValue()) / dataRows.size());
        }
        
        return score;
    }
    
    private void computeScores(ParsimoniousContextTreeNode<C> node,
                               List<DataRow<C>> dataRows,
                               int currentDepth,
                               int totalDepth) {
        if (node.getChildren() == null) {
            node.setScore(computeBIC(dataRows));
            return;
        }
        
        Map<C, ParsimoniousContextTreeNode<C>> characterToNodeMap = 
                new HashMap<>();
        
        Map<ParsimoniousContextTreeNode<C>, List<DataRow<C>>> nodeToDataMap = 
                new HashMap<>();
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            for (C character : child.getLabel()) {
                characterToNodeMap.put(character, child);
            }
            
            nodeToDataMap.put(child, new ArrayList<>());
        }
        
        int charIndex = totalDepth - currentDepth;
        
        for (DataRow<C> dataRow : dataRows) {
            C ch = dataRow.getExplanatoryVariable(charIndex);
            ParsimoniousContextTreeNode<C> tmpNode = characterToNodeMap.get(ch);
            nodeToDataMap.get(tmpNode).add(dataRow);
        }
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            computeScores(child, 
                          nodeToDataMap.get(child), 
                          currentDepth - 1, 
                          totalDepth);
        }
        
        // Collect the scores.
        double score = 0.0;
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            score += child.getScore();
        }
        
        node.setScore(score);
    }
}
