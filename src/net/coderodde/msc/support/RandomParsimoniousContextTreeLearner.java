package net.coderodde.msc.support;

import java.util.ArrayList;
import java.util.Collections;
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
 * In this learner each children configuration is equally probable. The 
 * procedure for creating an alphabet partition randomly samples the labels
 * until the sum of all characters equals the alphabet size, and then checks
 * that the set of labels is a partition.
 * 
 * @author Rodion "rodde" Efremov
 * @param <C> the actual character type.
 */
public final class RandomParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    private AbstractProbabilityDistribution<Set<C>> probabilityDistribution;
    
    private List<DataRow<C>> dataRows;
    
    private double k;
    
    private Random random;
    
    public void setRandom(Random random) {
        this.random = Objects.requireNonNull(random, "Random is null.");
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        Objects.requireNonNull(listOfDataRows, "The data row list is null.");
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
    
        RandomParsimoniousContextTreeLearner<C> state = 
                new RandomParsimoniousContextTreeLearner<>();
        
        state.random = random;
        state.dataRows = listOfDataRows;
        state.alphabet = getAlphabet(listOfDataRows);
        state.k = 0.5 * (state.alphabet.size() - 1)
                      * Math.log(listOfDataRows.size());
        state.probabilityDistribution =
                createProbabilityDistribution(state.alphabet);
        state.root = state.buildTree();
        state.computeScores();
        return new ParsimoniousContextTree<>(state.root);
    }
    
    /**
     * Computes the scores for all nodes of the PCT.
     */
    private void computeScores() {
        int treeDepth = dataRows.get(0).getNumberOfExplanatoryVariables();
        computeScores(root,
                      dataRows,
                      treeDepth,
                      treeDepth);
    }
    
    /**
     * Computes the BIC over the input data rows.
     * 
     * @param dataRows the list of rows to consider.
     * @return the BIC score.
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
     * Computes the score for the {@code node}.
     * 
     * @param node         the node whose score to compute.
     * @param dataRows     the list of relevant data rows.
     * @param currentDepth the current depth of {@code node}.
     * @param totalDepth   the total depth of the entire PCT.
     */
    private void computeScores(ParsimoniousContextTreeNode<C> node,
                               List<DataRow<C>> dataRows,
                               int currentDepth,
                               int totalDepth) {
        if (node.getChildren() == null) {
            // If here, 'node' is a leaf node. Compute and set the BIC.
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
    
    /**
     * Creates a uniform probability distribution over all possible alphabet
     * subsets.
     * 
     * @param alphabet the target alphabet.
     * @return a probability distribution of alphabet subsets.
     */
    private AbstractProbabilityDistribution<Set<C>> 
            createProbabilityDistribution(Alphabet<C> alphabet) {
        List<Set<C>> labels = alphabet.getAllPossibleLabels();
        AbstractProbabilityDistribution<Set<C>> probabilityDistribution =
                new BinaryTreeProbabilityDistribution<>(this.random);
        
        for (Set<C> label : labels) {
            probabilityDistribution.addElement(label, 1.0);
        }
        
        return probabilityDistribution;
    }
    
    /**
     * Builds the entire PCT.
     * 
     * @return the root of the resulting PCT.
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
     * Creates a set of child PCT nodes at depth {@code depth}.
     * 
     * @param depth the target depth.
     * @return a set of children.
     */
    private Set<ParsimoniousContextTreeNode<C>> createChildren(int depth) {
        Set<ParsimoniousContextTreeNode<C>> children = new HashSet<>();
        // Generate children set:
        Set<Set<C>> labels = createRandomChildLabelPartition();
        
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
     * Creates randomly a partition of the alphabet.
     * 
     * @return an alphabet partition.
     */
    private Set<Set<C>> createRandomChildLabelPartition() {
        Set<Set<C>> labelSet = new HashSet<>();
        Set<C> filter = new HashSet<>(alphabet.size());
        
        while (true) {
            Set<C> label = probabilityDistribution.sampleElement();
            filter.addAll(label);
            labelSet.add(label);
            
            if (filter.size() == alphabet.size()) {
                if (isAlphabetPartition(labelSet)) {
                    return labelSet;
                }
                
                labelSet.clear();
                filter.clear();
            }
        }
    }
    
    /**
     * Checks that the input label set comprises a partition of the alphabet.
     * 
     * @param labelSet the label set to check.
     * @return {@code true] only if {@code labelSet} is a valid alphabet 
     *         partiion.
     */
    private boolean isAlphabetPartition(Set<Set<C>> labelSet) {
        List<Set<C>> labelList = new ArrayList<>(labelSet);
        int labelListSize = labelList.size();
        
        // Checks that all labels are disjoint:
        for (int i = 0; i < labelListSize; ++i) {
            Set<C> label1 = labelList.get(i);
            
            for (int j = i + 1; j < labelListSize; ++j) {
                Set<C> label2 = labelList.get(j);
                
                if (!Collections.<C>disjoint(label1, label2)) {
                    return false;
                }
            }
        }
        
        // Check that the label set covers the alphabet:
        Set<C> filter = new HashSet<>();
        
        for (Set<C> label : labelList) {
            filter.addAll(label);
        }
        
        return filter.size() == alphabet.size();
    }
}
