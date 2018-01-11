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
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 11, 2018)
 */
public final class RandomParsimoniousContextTreeLearner3<C> 
extends AbstractParsimoniousContextTreeLearner<C> {

    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    private List<DataRow<C>> dataRows;
    
    private Map<C, Integer> characterCountMap;
    
    private Deque<ParsimoniousContextTreeNode<C>> queue;
    
    private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap;
    
    private double k;
    
    private Random random;
    
    public void setRandom(Random random) {
        this.random = Objects.requireNonNull(random);
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        Objects.requireNonNull(listOfDataRows);
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        
        RandomParsimoniousContextTreeLearner3<C> state = 
                new RandomParsimoniousContextTreeLearner3<>();
        
        state.random = random;
        state.dataRows = listOfDataRows;
        state.alphabet = getAlphabet(listOfDataRows);
        state.k = 0.5 * (state.alphabet.size() - 1) * 
                         Math.log(listOfDataRows.size());
        state.characterCountMap = new HashMap<>();
        state.queue = new ArrayDeque<>();
        state.depthMap = new HashMap<>();
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
