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

/**
 * This learner selects the number of children randomly from a uniform 
 * distribution, but does not select more children than a given threshold.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 14, 2018)
 * @param <C> the actual character type.
 */
public final class RandomParsimoniousContextTreeLearnerV3<C> 
extends AbstractParsimoniousContextTreeLearner<C>{

    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    /**
     * The default number of maximum labels per node. The default value sets the
     * maximum in question virtually to infinity.
     */
    private static final int DEFAULT_MAXIMUM_CHILDREN_PER_NODE = 
            Integer.MAX_VALUE;
    
    private List<DataRow<C>> dataRows;
    
    private double k;
    
    private Random random;
    
    private int maximumChildrenPerNode = DEFAULT_MAXIMUM_CHILDREN_PER_NODE;
    
    public void setMaximumChildrenPerNode(int maximumLabelsPerNode) {
        this.maximumChildrenPerNode = maximumLabelsPerNode;
    }
    
    public void setRandom(Random random) {
        this.random = Objects.requireNonNull(random);
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> dataRows) {
        Objects.requireNonNull(dataRows);
        checkDataRowListNotEmpty(dataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(dataRows);
        
        RandomParsimoniousContextTreeLearnerV3<C> state = 
                new RandomParsimoniousContextTreeLearnerV3<>();
        
        state.random = random;
        state.dataRows = dataRows;
        state.alphabet = getAlphabet(dataRows);
        state.k = 0.5 * (state.alphabet.size() - 1) * 
                         Math.log(dataRows.size());
        state.maximumChildrenPerNode = maximumChildrenPerNode;
        state.root = state.buildTree();
        state.computeScoresV2();
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
    
    /**
     * Creates a set of child nodes for the depth {@code depth}. First, the 
     * number of children nodes is randomly and uniformly chosen. Then the 
     * minimum of that number and the maximum child count is taken. Finally, the
     * alphabet characters are redistributed over child nodes randomly.
     * 
     * @param depth the depth.
     * @return a set of child PCT nodes.
     */
    private Set<ParsimoniousContextTreeNode<C>> createChildren(int depth) {
        int childCount = random.nextInt(alphabet.size()) + 1;
        childCount = Math.min(childCount, maximumChildrenPerNode);
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
            if (!label.isEmpty()) {
                ParsimoniousContextTreeNode<C> child = 
                        new ParsimoniousContextTreeNode<>();
                child.setLabel(label);
                children.add(child);

                if (depth > 0) {
                    child.setChildren(createChildren(depth - 1));
                }
            }
        }
        
        return children;
    }
    
    private void computeScoresV2() {
        int treeDepth = dataRows.get(0).getNumberOfExplanatoryVariables();
        computeScoresV2(root,
                        dataRows,
                        treeDepth,
                        treeDepth);
    }
    
    private void computeScoresV2(ParsimoniousContextTreeNode<C> node,
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
            computeScoresV2(child, 
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
}
