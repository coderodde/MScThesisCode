package net.coderodde.msc.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;
import net.coderodde.msc.util.CombinationIterable;

/**
 * This class implements a basic algorithm for learning parsimonious context 
 * trees.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 15, 2017)
 * @param <C> the character type.
 */
public final class BasicParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {
    
    /**
     * The list of all possible node labels. Each label is a set of characters 
     * from the alphabet.
     */
    private final List<Set<C>> listOfAllPossibleNodeLabels;
    
    /**
     * 
     */
    private final Map<List<Set<C>>, Double> mapPartitionToScore;
    
    public BasicParsimoniousContextTreeLearner() {
        super(null);
        this.listOfAllPossibleNodeLabels = null;
        this.mapPartitionToScore = null;
    }
    
    private BasicParsimoniousContextTreeLearner(Alphabet alphabet) {
        super(alphabet);
        this.listOfAllPossibleNodeLabels = 
                new ArrayList<>(
                        alphabet.getNumberOfNonemptyCharacterCombinations());
        this.mapPartitionToScore = new HashMap<>();
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(Alphabet alphabet,
                                            List<DataRow<C>> dataRowList) {
        Objects.requireNonNull(alphabet, "The input alphabet is null.");
        Objects.requireNonNull(dataRowList, "The input data row list is null.");
        checkDataRowListNotEmpty(dataRowList);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(dataRowList);
        BasicParsimoniousContextTreeLearner state =
                new BasicParsimoniousContextTreeLearner(alphabet);
        ParsimoniousContextTreeNode<C> root = 
                new ParsimoniousContextTreeNode<>();
        root.setLabel(Collections.<C>emptySet());
        state.buildTree(root, 1);
        return new ParsimoniousContextTree<C>(root);
    }

    private double computeBayesianInformationCriterion(ParsimoniousContextTreeNode<C> node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Builds recursively the tree.
     * 
     * @param node  the current node to process.
     * @param depth the current depth. Leaves are considered to be at depth
     *              zero.
     */
    private void buildTree(ParsimoniousContextTreeNode<C> node, int depth) {
        if (depth == 0) {
            node.setScore(computeBayesianInformationCriterion(node));
            return;
        }
        
        Set<ParsimoniousContextTreeNode<C>> children =
                new HashSet<>(this.listOfAllPossibleNodeLabels.size());
        
        node.setChildren(children);
        Map<Set<C>, ParsimoniousContextTreeNode<C>> nodeMap =
                new HashMap<>(
                        this.alphabet.getNumberOfNonemptyCharacterCombinations()
                );
        
        for (Set<C> label : this.listOfAllPossibleNodeLabels) {
            ParsimoniousContextTreeNode<C> childNode =
                    new ParsimoniousContextTreeNode<>();
            
            childNode.setLabel(label);
            nodeMap.put(label, childNode);
            children.add(childNode);
            buildTree(childNode, depth - 1);
        }
        
        this.mapPartitionToScore.clear();
        
        for (List<Set<C>> labelCombination :
                new CombinationIterable<Set<C>>(
                        this.listOfAllPossibleNodeLabels)) {
            if (!isPartitionOfAlphabet(labelCombination)) {
                // labelCombination is not a valid alphabet partition; omit.
                continue;
            }
            
            double score = 0.0;
            
            for (Set<C> label : labelCombination) {
                score += nodeMap.get(label).getScore();
            }
            
            this.mapPartitionToScore.put(labelCombination, score);
        }
        
        double bestScore = Double.NEGATIVE_INFINITY;
        List<Set<C>> bestPartition = null;
        
        for (Map.Entry<List<Set<C>>, Double> entry : 
                this.mapPartitionToScore.entrySet()) {
            if (bestScore < entry.getValue()) {
                bestScore = entry.getValue();
                bestPartition = entry.getKey();
            }
        }
        
        node.setScore(bestScore);
        Set<Set<C>> bestPartitionFilter = new HashSet<>();
        Iterator<ParsimoniousContextTreeNode<C>> iterator =
                node.getChildren().iterator();
        
        while (iterator.hasNext()) {
            ParsimoniousContextTreeNode<C> currentChildNode = iterator.next();
            
            if (!bestPartitionFilter.contains(currentChildNode.getLabel())) {
                iterator.remove();;
            }
        }
    }
}
