package net.coderodde.msc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class implements a data structure for getting a probability distribution
 * of a response variable given a string.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 2, 2017)
 * @param <C> the character type.
 */
public final class ParsimoniousContextTree<C> {

    /**
     * This static inner class holds all the data needed for representing a 
     * parsimonious context tree node.
     * 
     * @param <C> the type of characters.
     */
    private static final class ParsimoniousContextTreeNode<C> {
        
        /**
         * The set of characters for this node.
         */
        private Set<C> label;
        
        /**
         * The set of children nodes. If this node is a leaf, this field is set
         * to {@code null}.
         */
        private Set<ParsimoniousContextTreeNode<C>> children;
        
        /**
         * The conditional probability distribution of the response variable.
         * This field is set only if this node is a leaf.
         */
        private ResponseVariableDistribution<C> responseVariableDistribution;
        
        /**
         * The score of this node.
         */
        private double score;
        
        void convertToTextualRepresentation(String indentation, 
                                            String singleSeparator,
                                            StringBuilder stringBuilder) {
            stringBuilder.append(indentation)
                         .append("[{");
            String sep = "";
            
            for (C ch : label) {
                stringBuilder.append(sep)
                             .append(ch);
                sep = ",";
            }
            
            stringBuilder.append("} score ")
                         .append(score)
                         .append("]\n");
            
            if (children != null) {
                for (ParsimoniousContextTreeNode<C> child : children) {
                    child.convertToTextualRepresentation(
                            indentation + singleSeparator,
                            singleSeparator,
                            stringBuilder);
                }
            }
        }
    }
    
    /**
     * The array of boolean flags for generating the children labels of a 
     * parsimonious context tree nodes. We cache this field in order to not 
     * create the flag array every time we are computing the children.
     */
    private final boolean[] combinationFlags;
    
    /**
     * The list containing all possible subsets of the given alphabet.
     */
    private final List<Set<C>> listOfAllNodeLabels;
    
    /**
     * The alphabet of this tree.
     */
    private final Alphabet<C> alphabet;
    
    /**
     * The root node of this tree.
     */
    private final ParsimoniousContextTreeNode<C> root =
            new ParsimoniousContextTreeNode<>();
    
    /**
     * Holds the list of actual data rows.
     */
    private final List<DataRow<C>> dataRowList;
    
    /**
     * Used for counting character frequencies.
     */
    private final Map<C, Integer> characterCountMap = new HashMap<>();
    
    /**
     * The penalty contribution to a single leaf.
     */
    private final double K;
    
    /**
     * Used to assure that a set of node labels comprise an entire alphabet.
     */
    private final Set<C> characterFilterSet = new HashSet<>();
    
    /**
     * Constructs a parsimonious context tree from the given data over the input
     * alphabet.
     * 
     * @param alphabet    the alphabet to use.
     * @param dataRowList a list of data rows.
     */
    public ParsimoniousContextTree(Alphabet<C> alphabet, 
                                   List<DataRow<C>> dataRowList) {
        this.alphabet = 
                Objects.requireNonNull(
                        alphabet, 
                        "The input alphabet is null.");
        
        this.dataRowList = 
                Objects.requireNonNull(
                        dataRowList,
                        "The data row list is null.");
        
        checkDataRowListNotEmpty(dataRowList);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(dataRowList);
        
        this.listOfAllNodeLabels = 
                new ArrayList<>(
                        alphabet.getNumberOfNonemptyCharacterCombinations());
        this.combinationFlags = new boolean[alphabet.size()];
        
        loadListOfAllPossibleSubsetsOfAlphabet();
        
        int depth = dataRowList.get(0).getNumberOfExplanatoryVariables();
        K = 0.5 * ((alphabet.size() - 1) * Math.log(dataRowList.size()));
        buildTree(root, depth);
        root.label = Collections.<C>emptySet();
    }
    
    private void buildTree(ParsimoniousContextTreeNode<C> node, int depth) {
        if (depth == 0) {
            node.score = computeBayesianInformationCriterion(node);
            return;
        }
        
        Set<ParsimoniousContextTreeNode<C>> children = 
                new HashSet<>(this.listOfAllNodeLabels.size());
        
        node.children = children;
        Map<Set<C>, ParsimoniousContextTreeNode<C>> nodeMap = 
                new HashMap<>(
                        this.alphabet
                            .getNumberOfNonemptyCharacterCombinations());
        
        for (Set<C> label : this.listOfAllNodeLabels) {
            ParsimoniousContextTreeNode<C> childNode = 
                    new ParsimoniousContextTreeNode<>();
            childNode.label = label;
            nodeMap.put(label, childNode);
            children.add(childNode);
            buildTree(childNode, depth - 1);
        }
        
        for (List<Set<C>> labelCombination : 
                new CombinationIterable<Set<C>>(this.listOfAllNodeLabels)) {
            if (!isPartitionOfAlphabet(labelCombination)) {
                continue;
            }
            
            // We need a node having as its label 'labelCombination':
            Set<C> childLabel = new HashSet<>();
           
            for (Set<C> label : labelCombination) {
                childLabel.addAll(label);
            }
            
            ParsimoniousContextTreeNode<C> currentChildTree = 
                    nodeMap.get(childLabel);
            
            if (currentChildTree.children != null) {
                double score = 0.0;

                for (ParsimoniousContextTreeNode<C> m 
                        : currentChildTree.children) {
                    score += m.score;
                }
                
                currentChildTree.score = score;
                System.out.println("Setting " + score);
            }
        }
    }
    
    private boolean isPartitionOfAlphabet(List<Set<C>> labelCombination) {
        int labels = labelCombination.size();
        
        for (int i = 0; i < labels; ++i) {
            Set<C> set1 = labelCombination.get(i);
            
            for (int j = i + 1; j < labels; ++j) {
                Set<C> set2 = labelCombination.get(j);
                
                if (!Collections.<C>disjoint(set1, set2)) {
                    return false;
                }
            }
        }
        
        this.characterFilterSet.clear();
        
        for (Set<C> label : labelCombination) {
            this.characterFilterSet.addAll(label);
        }
            
        return this.characterFilterSet.size() == this.alphabet.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        root.convertToTextualRepresentation("", "  ", sb);
        return sb.toString();
    }
    
    private double computeBayesianInformationCriterion(
            ParsimoniousContextTreeNode<C> node) {
        this.characterCountMap.clear();
        int totalCounts = 0;
        
        for (DataRow<C> dataRow : this.dataRowList) {
            if (getNodeOfDataRow(dataRow) == node) {
                totalCounts++;
                C lastChar = dataRow.getExplanatoryVariable(
                        dataRow.getNumberOfExplanatoryVariables() - 1);
                Integer count = this.characterCountMap.get(lastChar);
                
                if (count != null) {
                    this.characterCountMap.put(lastChar, count + 1);
                } else {
                    this.characterCountMap.put(lastChar, 1);
                }
            }
        }
        
        double score = -K;
        
        for (Map.Entry<C, Integer> e : this.characterCountMap.entrySet()) {
            score += e.getValue() * Math.log(e.getValue() / totalCounts);
        }
        
        return score;
    }
    
    private ParsimoniousContextTreeNode<C> getNodeOfDataRow(DataRow dataRow) {
        int length = dataRow.getNumberOfExplanatoryVariables();
        ParsimoniousContextTreeNode<C> node = root;
        
        outer:
        for (int i = 0; i < length; ++i) {
            for (ParsimoniousContextTreeNode<C> child : node.children) {
                if (child.label.contains(dataRow.getExplanatoryVariable(i))) {
                    node = child;
                    continue outer;
                }
            }
        }
        
        return node;
    }
    
    private void loadListOfAllPossibleSubsetsOfAlphabet() {
        while (incrementCombinationFlags()) {
            loadCharacterCombination();
        }
    }
    
    private void loadCharacterCombination() {
        Set<C> characterSet = new HashSet<>();
        
        for (int i = 0; i < combinationFlags.length; ++i) {
            if (combinationFlags[i] == true) {
                characterSet.add(alphabet.get(i));
            }
        }
        
        this.listOfAllNodeLabels.add(characterSet);
    }
    
    private boolean incrementCombinationFlags() {
        int alphabetSize = combinationFlags.length;
        
        for (int i = 0; i < alphabetSize; ++i) {
            if (combinationFlags[i] == false) {
                combinationFlags[i] = true;
                return true;
            } else {
                combinationFlags[i] = false;
            }
        }
        
        return false;
    }
    
    private void checkDataRowListNotEmpty(List<DataRow<C>> dataRowList) {
        if (dataRowList.isEmpty()) {
            throw new IllegalArgumentException(
                    "There is no data rows in the list.");
        }
    }
    
    private void checkDataRowListHasConstantNumberOfExplanatoryVariables(
            List<DataRow<C>> dataRowList) {
        int expectedNumberOfExplanatoryVariables = 
                dataRowList.get(0).getNumberOfExplanatoryVariables();
        
        for (int i = 1; i < dataRowList.size(); ++i) {
            if (dataRowList.get(i).getNumberOfExplanatoryVariables() !=
                    expectedNumberOfExplanatoryVariables) {
                throw new IllegalArgumentException(
                        "The data row " + i + " does not have " +
                                expectedNumberOfExplanatoryVariables + 
                                " explanatory variables.");
            }
        }
    }
}
