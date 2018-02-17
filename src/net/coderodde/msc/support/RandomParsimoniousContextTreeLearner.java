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
 * In this learner each children configuration is equally probable.
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
    
    private void computeScores() {
        int treeDepth = dataRows.get(0).getNumberOfExplanatoryVariables();
        computeScores(root,
                      dataRows,
                      treeDepth,
                      treeDepth);
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
        Set<Set<C>> labels = createRandomLabelSet();
        
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
    
    private Set<Set<C>> createRandomLabelSet() {
        Set<Set<C>> labelSet = new HashSet<>();
        Set<C> filter = new HashSet<>(alphabet.size());
        
        while (true) {
            Set<C> label = probabilityDistribution.sampleElement();
            filter.addAll(label);
            labelSet.add(label);
            
            if (filter.size() == alphabet.size()) {
                if (isPartition(labelSet)) {
                    return labelSet;
                }
                
                labelSet.clear();
                filter.clear();
            }
        }
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
}
