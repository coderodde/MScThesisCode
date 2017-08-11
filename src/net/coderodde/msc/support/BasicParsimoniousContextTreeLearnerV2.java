package net.coderodde.msc.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
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

/**
 * This class implements a basic algorithm for learning parsimonious context 
 * trees.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 9, 2017)
 * @param <C> the character type.
 */
public final class BasicParsimoniousContextTreeLearnerV2<C> 
extends AbstractParsimoniousContextTreeLearner<C> {

    /**
     * The root node of the resulting tree.
     */
    private ParsimoniousContextTreeNode<C> root;
    
    /**
     * Stores the list of all possible node labels.
     */
    private List<Set<C>> listOfAllPossibleNodeLabels;
    
    /**
     * The alphabet to use.
     */
    private Alphabet<C> alphabet;
    
    /**
     * The list of data rows.
     */
    private List<DataRow<C>> listOfDataRows;
    
    /**
     * Reused every time we compute a BIC-score for each leaf node.
     */
    private Map<C, Integer> characterCountMap = new HashMap<>();
    
    /**
     * The frontier queue for the breadth-first search. We need this whenever
     * asking whether a data row leads to a particular leaf node.
     */
    private Deque<ParsimoniousContextTreeNode<C>> queue = new ArrayDeque<>();
    
    /**
     * Maps each node to its depth in the breadth-first search.
     */
    private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap = new HashMap<>();
    
    /**
     * The penalty score.
     */
    private double k;
    
    /**
     * Used for holding a combination of labels while pruning.
     */
    private List<Set<C>> labelCombination = new ArrayList<>();
    
    /**
     * Used for verifying that the labels cover the entire alphabet.
     */
    private Set<C> characterFilterSet = new HashSet<>();
    
    /**
     * Maps each partition of the alphabet to its score.
     */
    private Map<List<Set<C>>, Double> mapPartitionToScore = new HashMap<>();
    
    /**
     * Used for computing combinations of labels.
     */
    private boolean[] labelFlags;
    
    @Override
    public ParsimoniousContextTree<C> learn(Alphabet<C> alphabet,
                                            List<DataRow<C>> listOfDataRows) {
        this.alphabet = Objects.requireNonNull(alphabet, 
                                               "The input alphabet is null.");
        
        this.listOfDataRows = 
                Objects.requireNonNull(listOfDataRows, 
                                       "The input data row list is null.");
        
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        
        root = new ParsimoniousContextTreeNode<>();
        root.setLabel(new HashSet<>()); // The root's label is empty.
        
        this.k = computeK();
        
        // First, just build the entire extended PCT:
        buildExtendedPCT();
        computeLeafNodeScores();
        pruneExtendedPCT();
        return new ParsimoniousContextTree<>(root);
    }
    
    /**
     * Recursively builds the entire PCT.
     */
    private void buildExtendedPCT() {
        createListOfAllPossibleLabels();
        
        int depth = 
                this.listOfDataRows.get(0).getNumberOfExplanatoryVariables();
        
        buildExtendedPCT(root, depth);
    }
    
    /**
     * Prunes the extended PCT into an ordinary PCT.
     */
    private void pruneExtendedPCT() {
        this.labelFlags = new boolean[this.listOfAllPossibleNodeLabels.size()];
        pruneExtendedPCT(root);
    }
    
    /**
     * Turns off all flags.
     */
    private void wipeOutLabelFlags() {
        Arrays.fill(labelFlags, 0, labelFlags.length, false);
    }
    
    /**
     * Unless {@code node} is a leaf node, prunes this node.
     * 
     * @param node the node to prune.
     */
    private void pruneExtendedPCT(ParsimoniousContextTreeNode<C> node) {
        if (node.getChildren() == null) {
            // Terminate the recursion:
            return;
        }
        
        wipeOutLabelFlags();
        Map<Set<C>, ParsimoniousContextTreeNode<C>> nodeMap = 
                new HashMap<>(
                this.alphabet.getNumberOfNonemptyCharacterCombinations());
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            nodeMap.put(child.getLabel(), node);
        }
        
        while (incrementCombinationFlags(labelFlags)) {
            loadCombinationList(labelFlags);
            
            if (!isPartitionOfAlphabet(this.labelCombination)) {
                this.labelCombination.clear();
                continue;
            }
            
            double score = 0.0;
            
            for (Set<C> label : this.labelCombination) {
                score += nodeMap.get(label).getScore();
            }
            
            this.mapPartitionToScore.put(labelCombination, score);
            this.labelCombination.clear();
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
        
        Set<Set<C>> bestPartitionAsSet = new HashSet<>(bestPartition);
        Iterator<ParsimoniousContextTreeNode<C>> iterator = 
                node.getChildren().iterator();
        
        while (iterator.hasNext()) {
            ParsimoniousContextTreeNode<C> currentChildNode = iterator.next();
            
            if (!bestPartitionAsSet.contains(currentChildNode.getLabel())) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Returns {@code true} if and only if all sets in {@code labelCombination} 
     * are disjoint, and their union equals the alphabet.
     * 
     * @param labelCombination the label combination.
     * @return {@code true} only if the label combination partitions the 
     *         alphabet.
     */
    private boolean isPartitionOfAlphabet(List<Set<C>> labelCombination) {
        int labels = labelCombination.size();
        
        // All distinct labels are disjoint?
        for (int i = 0; i != labels; ++i) {
            Set<C> label1 = labelCombination.get(i);
            
            for (int j = i + 1; j < labels; ++j) {
                Set<C> label2 = labelCombination.get(j);
                
                if (!Collections.<C>disjoint(label1, label2)) {
                    return false;
                }
            }
        }
        
        // Once here, the labels are disjoint, but do they cover the entire
        // alphabet?
        for (Set<C> label : labelCombination) {
            this.characterFilterSet.addAll(label);
        }
        
        boolean isPartition = 
                this.characterFilterSet.size() == this.alphabet.size();
        
        this.characterFilterSet.clear();
        return isPartition;
    }
    
    /**
     * Loads the labels into {@code this.labelCombination}.
     * 
     * @param flags the flag array. If {@code flags[i]} is {@code true}, 
     *              {@code listOfAllPossibleNodeLabels.get(i)} is added to the
     *              {@code labelCombination}.
     */
    private void loadCombinationList(boolean[] flags) {
        for (int i = 0; i != flags.length; ++i) {
            if (flags[i]) {
                this.labelCombination.add(
                        this.listOfAllPossibleNodeLabels.get(i));
            }
        }
    }
    
    /**
     * Recurs to each leaf in order to compute their scores.
     */
    private void computeLeafNodeScores() {
        for (ParsimoniousContextTreeNode<C> childOfRoot : root.getChildren()) {
            computeLeafNodeScores(childOfRoot);
        }
    }
    
    /**
     * Implements the leaf score computing routine. If {@code node} is a leaf
     * node, computes its BIC-score. Otherwise, recurs to the children of the
     * node.
     * 
     * @param node the current node.
     */
    private void computeLeafNodeScores(ParsimoniousContextTreeNode<C> node) {
        if (node.getChildren() == null) {
            node.setScore(computeBayesianInformationCriterion(node));
            return;
        }
        
        for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
            computeLeafNodeScores(child);
        }
    }
    
    /**
     * Implements the extended PCT construction algorithm.
     * 
     * @param node the node from which to start constructing the subtree.
     * @param depth the current depth of the input node.
     */
    private void buildExtendedPCT(
            ParsimoniousContextTreeNode<C> node, 
            int depth) {
        if (depth == 0) {
            // 'node' is a leaf terminate the depth-first traversal.
            return;
        }
        
        // Here we store all the newly created child nodes for 'node':
        Set<ParsimoniousContextTreeNode<C>> childrenSetOfNode =
                new HashSet<>(this.listOfAllPossibleNodeLabels.size());
        
        for (Set<C> label : this.listOfAllPossibleNodeLabels) {
            ParsimoniousContextTreeNode<C> newChild = 
                    new ParsimoniousContextTreeNode<>();
            
            newChild.setLabel(label);
            childrenSetOfNode.add(newChild);
        }
        
        node.setChildren(childrenSetOfNode);
        
        // Recur to the children:
        for (ParsimoniousContextTreeNode<C> child : childrenSetOfNode) {
            buildExtendedPCT(child, depth - 1);
        }
    }
    
    /**
     * Computes the Bayesian information criterion (BIC) score for {@code node}.
     * 
     * @param node the node whose BIC to compute; expected to be a leaf node.
     */
    private double computeBayesianInformationCriterion(
            ParsimoniousContextTreeNode<C> node) {
        int totalCount = 0;
        
        for (DataRow<C> dataRow : this.listOfDataRows) {
            if (dataRowMatchesLeafNode(dataRow, node)) {
                totalCount++;
                C responseVariable = dataRow.getResponseVariable();
                this.characterCountMap.put(
                        responseVariable, 
                        this.characterCountMap
                            .getOrDefault(responseVariable, 0) + 1);
            }
        }
        
        double score = -this.k;
        
        for (Map.Entry<C, Integer> e : this.characterCountMap.entrySet()) {
            score += e.getValue() * 
                    Math.log((1.0 * e.getValue()) / totalCount);
        }
        
        // Clear for the next possible leaf node:
        this.characterCountMap.clear();
        return score;
    }
    
    /**
     * Returns {@code true} only if the input data row leads along the labels
     * towards the given leaf node.
     * 
     * @param dataRow the data row to test.
     * @param leafNode the target leaf node.
     * @return {@code true} if it is possible to get into {@code leafNode} by 
     *                      using the characters of the {@code dataRow}.
     */
    private boolean dataRowMatchesLeafNode(
            DataRow<C> dataRow,
            ParsimoniousContextTreeNode<C> leafNode) {
        int treeDepth = this.listOfDataRows
                            .get(0)
                            .getNumberOfExplanatoryVariables();
        
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
                    // Clear the data structures for future possible reuse:
                    this.queue.clear();
                    this.depthMap.clear();

                    return true;
                }
            } else {
                C targetCharacter = 
                        dataRow.getExplanatoryVariable(currentNodeDepth);
                
                for (ParsimoniousContextTreeNode<C> child :
                        currentNode.getChildren()) {
                    if (child.getLabel().contains(targetCharacter)) {
                        this.queue.addLast(child);
                        this.depthMap.put(child, currentNodeDepth + 1);
                    }
                }
            }
        }
        
        // Clear the data structures for future possible reuse:
        this.queue.clear();
        this.depthMap.clear();
        
        return false;
    }
    
    /**
     * Creates and populates the list of all possible, non-empty character 
     * combinations. These are used as node labels.
     */
    private void createListOfAllPossibleLabels() {
        this.listOfAllPossibleNodeLabels = 
                new ArrayList<>(
                        alphabet.getNumberOfNonemptyCharacterCombinations());
        
        // Used for generating the combinations.
        boolean[] flags = new boolean[alphabet.size()];
        
        // Keep populating the labels until they are all there:
        while (incrementCombinationFlags(flags)) {
            this.listOfAllPossibleNodeLabels
                    .add(loadCharacterCombination(alphabet, flags));
        }
    }
    
    /**
     * Produces the next combination.
     * 
     * @param flags the flag arrary.
     * @return {@code true} if a new combination was created; {@code false} 
     * indicates that all possible combinations where generated.
     */
    private static boolean incrementCombinationFlags(boolean[] flags) {
        for (int i = 0; i < flags.length; ++i) {
            if (flags[i] == false) {
                flags[i] = true;
                return true;
            } else {
                flags[i] = false;
            }
        }
        
        return false;
    }
    
    /**
     * Creates and returns a new combination.
     * 
     * @param <C> the character type.
     * @param alphabet the alphabet.
     * @param flags the flag array.
     * @return a new set containing a character combination.
     */
    private static <C> Set<C> loadCharacterCombination(Alphabet<C> alphabet,
                                                       boolean[] flags) {
        Set<C> characterCombination = new HashSet<>();
        
        for (int i = 0; i < flags.length; ++i) {
            if (flags[i]) {
                characterCombination.add(alphabet.get(i));
            }
        }
        
        return characterCombination;
    }
    
    /**
     * Computes the penalty score.
     * 
     * @return the penalty score.
     */
    private double computeK() {
        return 0.5 * (alphabet.size() - 1) * Math.log(listOfDataRows.size());
    }
}
