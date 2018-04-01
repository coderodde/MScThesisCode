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

/**
 * In this learner, the probability of having <code>n</code> children is 
 * <code>pow(beta,n)</code>. When the number of children is chosen according to
 * that very distribution, the alphabet characters are redistributed over 
 * child nodes randomly.
 * 
 * @author Rodion "rodde" Efremov
 * @param <C> the character type.
 */
public final class RandomParsimoniousContextTreeLearnerV2<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    /**
     * The default value for the Beta parameter. The alphabet characters are
     * redistributed over the children randomly.
     */
    private static final double DEFAULT_BETA = 0.9;
    
    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    private AbstractProbabilityDistribution<Integer> bucketSizeDistribution;
    
    private List<DataRow<C>> dataRows;
    
    private double k;
    
    private Random random;
    
    private double beta = DEFAULT_BETA;
    
    public void setBeta(double beta) {
        this.beta = beta;
    }
    
    public void setRandom(Random random) {
        this.random = Objects.requireNonNull(random, "Random is null.");
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> dataRows) {
        Objects.requireNonNull(dataRows, "The data row list is null.");
        checkDataRowListNotEmpty(dataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(dataRows);
    
        RandomParsimoniousContextTreeLearnerV2<C> state = 
                new RandomParsimoniousContextTreeLearnerV2<>();
        state.setBeta(beta);
        
        state.random = random;
        state.dataRows = dataRows;
        state.alphabet = getAlphabet(dataRows);
        state.k = 0.5 * (state.alphabet.size() - 1)
                      * Math.log(dataRows.size());
        state.bucketSizeDistribution = state.createBucketSizeDistribution();
        state.root = state.buildTree();
        state.computeScores();
        return new ParsimoniousContextTree<>(state.root);
    }
    
    /**
     * Creates a probability distribution. Given a positive parameter 
     * {@code beta}, the probability of one child is [@code beta}, the 
     * probability of two children is {@code beta^2}, and so on.
     * 
     * @return the probability distribution.
     */
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
        
    /**
     * Computes the scores of all the nodes in the PCT.
     */
    private void computeScores() {
        int treeDepth = dataRows.get(0).getNumberOfExplanatoryVariables();
        computeScores(root,
                      dataRows,
                      treeDepth,
                      treeDepth);
    }
    
    /**
     * Builds the entire PCT.
     * 
     * @return the root node of the created PCT.
     */
    private ParsimoniousContextTreeNode<C> buildTree() {
        int depth = dataRows.get(0).getNumberOfExplanatoryVariables();
        ParsimoniousContextTreeNode<C> root = 
                new ParsimoniousContextTreeNode<>();
        
        root.setLabel(new HashSet<>());
        root.setChildren(createChildren(depth - 1));
        
        return root;
    }
    
    /**
     * Creates a set of child PCT nodes of a node at depth {@code depth}.
     * 
     * @param depth the depth of the node whose children to create.
     * @return a set of child PCT nodes.
     */
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
    
    /**
     * Creates an alphabet partition. The number of blocks in the resulting 
     * partition is sampled according to the {@code beta} parameter. After that,
     * the alphabet characters are randomly distributed over all partition 
     * blocks.
     * 
     * @return an alphabet partition.
     */
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
    
    /**
     * Computes the BIC score over the input data set.
     * 
     * @param dataRows the data rows to consider.
     * @return a BIC score.
     */
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
    
    /**
     * Computes the score for the PCT node {@code node}.
     * 
     * @param node         the node whose score to compute.
     * @param dataRows     the list of relevant data rows.
     * @param currentDepth the depth of the input PCT node.
     * @param totalDepth   the total depth of the PCT being built.
     */
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
