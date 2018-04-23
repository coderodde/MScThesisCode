package net.coderodde.msc.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
 * This class implements a simple hybrid heuristic PCT learner. For each new 
 * PCT node, this learner continues it with both heuristic and optimal 
 * algorithms, then it compares the two partial solutions and uses the better 
 * one to continue the new node.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 21, 2018)
 */
public final class HybridHeuristicParsimoniousContextTreeLearnerV1<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    /**
     * Specifies the maximum alphabet size at which the algorithm can switch to
     * optimal search.
     */
    private static final int MAXIMUM_ALPHABET_SIZE = 13;
    
    /**
     * The alphabet of the data set.
     */
    private Alphabet<C> alphabet;
    
    /**
     * The alphabet size.
     */
    private int alphabetSize;
    
    /**
     * The root node of the resulting tree.
     */
    private ParsimoniousContextTreeNode<C> root;
    
    /**
     * The total depth of the tree.
     */
    private int totalDepth;
    
    /**
     * The list of data rows.
     */
    private List<DataRow<C>> dataRows;
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> dataRows) {
        this.alphabet = getAlphabet(dataRows);
        this.alphabetSize = this.alphabet.size();
        
        // If the alphabet too large for computing all the alphabet partitions,
        // then:
        if  (alphabetSize > MAXIMUM_ALPHABET_SIZE) {
            return new HeuristicParsimoniousContextTreeLearner<C>()
                       .learn(dataRows);
        }
        
        return new InternalHeuristicParsimoniousContextTreeLearner<C>()
                  .learn(dataRows);
    }
    
    private static final class 
            InternalHeuristicParsimoniousContextTreeLearner<C> 
    extends AbstractParsimoniousContextTreeLearner<C> {

        private Alphabet<C> alphabet;
        private List<DataRow<C>> dataRows;
        private double k;
        private ParsimoniousContextTreeNode<C> root;
        private int totalDepth;
        private final Map<C, Integer> characterCountMap = new HashMap<>();
        private final PartialBasicParsimoniousContextTreeLearner<C> 
                optimalLearner = 
                new PartialBasicParsimoniousContextTreeLearner<>();
        
        @Override
        public ParsimoniousContextTree<C> learn(List<DataRow<C>> dataRows) {
            Objects.requireNonNull(dataRows);
            checkDataRowListNotEmpty(dataRows);
            checkDataRowListHasConstantNumberOfExplanatoryVariables(dataRows);

            InternalHeuristicParsimoniousContextTreeLearner<C> state = 
                    new InternalHeuristicParsimoniousContextTreeLearner<>();

            state.alphabet = getAlphabet(dataRows);
            state.dataRows = dataRows;
            state.k = 0.5 * (state.alphabet.size() - 1) * 
                             Math.log(dataRows.size());
            // In this hybrid learner we build only one step further:
            state.optimalLearner.setRequestedTreeDepth(1);
            state.totalDepth = 
                    dataRows.get(0)
                            .getNumberOfExplanatoryVariables();
            
            state.build();
            return new ParsimoniousContextTree<>(state.root);    
        }
    
        private void build() {
            root = new ParsimoniousContextTreeNode<>();
            root.setLabel(Collections.emptySet());
            build(root, totalDepth, dataRows);
            computeScores();
        }

        // We need to return the children in a list because we need to index them
        // while trying to pair a child with another.
        private List<ParsimoniousContextTreeNode<C>> createChildren() {
            List<ParsimoniousContextTreeNode<C>> childrenList = 
                    new ArrayList<>(alphabet.size());

            for (C ch : alphabet.getCharacters()) {
                ParsimoniousContextTreeNode<C> child = 
                        new ParsimoniousContextTreeNode<>();
                Set<C> childLabel = new HashSet<>();
                childLabel.add(ch);
                child.setLabel(childLabel);
                childrenList.add(child);
                child.setScore(-k);
            }

            return childrenList;
        }

        // As we go down the tree, 'currentDepth' becomes smaller.
        private void build(ParsimoniousContextTreeNode<C> parent,
                           int currentDepth,
                           List<DataRow<C>> dataRows) {
            // Create the children list for the parent node.
            List<ParsimoniousContextTreeNode<C>> childrenList = 
                    createChildren();
            
            //TODO: Why createChildren() returns a list and not a set.
            parent.setChildren(new HashSet<>(childrenList)); 
            computeInitialScores(currentDepth, dataRows, parent);
            ParsimoniousContextTreeNode<C> bestChild1 = null;
            ParsimoniousContextTreeNode<C> bestChild2 = null;
            ParsimoniousContextTreeNode<C> child1;
            ParsimoniousContextTreeNode<C> child2;

            // Redistribute the data rows to their respective buckets:
            Map<C, List<DataRow<C>>> mapCharToDataRows = new HashMap<>();

            for (C character : alphabet.getCharacters()) {
                mapCharToDataRows.put(character, new ArrayList<>());
            }

            int characterIndex = dataRows.get(0).getNumberOfExplanatoryVariables()
                                 - currentDepth;

            for (DataRow<C> dataRow : dataRows) {
                C currentCharacter = dataRow.getExplanatoryVariable(characterIndex);
                mapCharToDataRows.get(currentCharacter).add(dataRow);
            }

            double bestParentScore = parent.getScore();
            double bestMergedScore = Double.NaN;

            while (true) {
                boolean improved = false;
                // Try pairwise merging.
                for (int i = 0; i < childrenList.size(); i++) {
                    child1 = childrenList.get(i);

                    for (int j = i + 1; j < childrenList.size(); j++) {
                        child2 = childrenList.get(j);
                        double mergedScore = findMergedScore(currentDepth,
                                                             mapCharToDataRows,
                                                             child1,
                                                             child2);
                        double candidateScore = 
                                parent.getScore() + mergedScore
                                                  - child1.getScore()
                                                  - child2.getScore();

                        if (bestParentScore < candidateScore) {
                            bestParentScore = candidateScore;
                            bestMergedScore = mergedScore;
                            bestChild1 = child1;
                            bestChild2 = child2;
                            improved = true;
                        }
                    }
                }

                if (!improved) {
                    if (currentDepth == 1) {
                        // Don't build any deeper:
                        return;
                    }
                    
                    // Try to extend the 'parent' via optimal learner.
                    // 'requestedStartDepthLevel' becomes larger as we go down
                    // the tree.
                    optimalLearner
                            .setRequestedStartDepthLevel(
                                    totalDepth - currentDepth);
                    
                    ParsimoniousContextTree<C> optimalTree = 
                            optimalLearner.learn(dataRows);
                    
                    ParsimoniousContextTreeNode<C> optimalTreeRoot = 
                            optimalTree.getRoot();
                    
                    double optimalTreeRootScore = optimalTreeRoot.getScore();
                    
                    if (bestParentScore < optimalTreeRootScore) {
                        // Once here, we better extend 'parent' with 
                        // 'optimalTree'.
                        // TODO: Debug this.
                        // System.out.println("Yeah!");
                        parent.setChildren(optimalTreeRoot.getChildren());
                    }

//                    if (parent.getChildren().size() > 2) {
//                        // There is a chance that creating only one child with the
//                        // label that equals the entire alphabet, would improve the
//                        // score:
//                        System.out.println("Might improve.");
//                        double singleChildScore = 
//                                findSingleChildScore(currentDepth, dataRows);
//                        System.out.println("New score: " + singleChildScore);
//                        System.out.println("Parent score: " + parent.getScore());
//                    }

                    Map<ParsimoniousContextTreeNode<C>, 
                        List<DataRow<C>>> nodeToDataRowsMap = new HashMap<>();
                    Map<C, ParsimoniousContextTreeNode<C>> charToNodeMap = 
                            new HashMap<>();

                    // Build the children.
                    // First split the data row list.
                    for (ParsimoniousContextTreeNode<C> node 
                            : parent.getChildren()) {
                        nodeToDataRowsMap.put(node, new ArrayList<>());
                    }

                    for (ParsimoniousContextTreeNode<C> node 
                            : parent.getChildren()) {
                        for (C ch : node.getLabel()) {
                            charToNodeMap.put(ch, node);
                        }
                    }

                    // TODO: Use totalDepth.
                    int charIndex = totalDepth - currentDepth;

                    for (DataRow<C> dataRow : dataRows) {
                        C ch = dataRow.getExplanatoryVariable(charIndex);
                        ParsimoniousContextTreeNode<C> child =
                                charToNodeMap.get(ch);
                        List<DataRow<C>> childDataRows = 
                                nodeToDataRowsMap.get(child);
                        childDataRows.add(dataRow);
                    }

                    for (ParsimoniousContextTreeNode<C> child 
                            : parent.getChildren()) {
                        build(child, 
                              currentDepth - 1, 
                              nodeToDataRowsMap.get(child));
                    }

                    return;
                } else {
                    // Merge:
                    parent.getChildren().remove(bestChild2);
                    childrenList.remove(bestChild2);
                    // Reuse bestChild1:
                    bestChild1.getLabel().addAll(bestChild2.getLabel());
                    bestChild1.setScore(bestMergedScore);

                    // Update the parent score:
                    double parentScore = 0.0; 

                    for (ParsimoniousContextTreeNode<C> child 
                            : parent.getChildren()) {
                        parentScore += child.getScore();
                    }

                    parent.setScore(parentScore);
                }
            }
        }

        private void computeScores(ParsimoniousContextTreeNode<C> node,
                                     List<DataRow<C>> data,
                                     int depth) {
            if (depth == 0) {
                Map<C, Integer> map = new HashMap<>();

                for (DataRow<C> dataRow : data) {
                    C responseVariable = dataRow.getResponseVariable();
                    map.put(responseVariable, 
                            map.getOrDefault(responseVariable, 0) + 1);
                }

                double score = -this.k;

                for (Map.Entry<C, Integer> entry : map.entrySet()) {
                    score += entry.getValue() *
                             Math.log((1.0 * entry.getValue()) / data.size());
                }

                node.setScore(score);
                return;
            }

            Map<ParsimoniousContextTreeNode<C>, List<DataRow<C>>> map = 
                    new HashMap<>();

            Map<C, ParsimoniousContextTreeNode<C>> map2 = new HashMap<>();

            for (ParsimoniousContextTreeNode<C> tmpNode : node.getChildren()) {
                for (C ch : tmpNode.getLabel()) {
                    map2.put(ch, tmpNode);
                }

                map.put(tmpNode, new ArrayList<>());
            }

            int charIndex = data.get(0).getNumberOfExplanatoryVariables() - depth;

            for (DataRow<C> dataRow : data) {
                C ch = dataRow.getExplanatoryVariable(charIndex);
                ParsimoniousContextTreeNode<C> myNode = map2.get(ch);
                map.get(myNode).add(dataRow);
            }

            double score = 0.0;

            for (ParsimoniousContextTreeNode<C> child : node.getChildren()) {
                computeScores(child, map.get(child), depth - 1);
                score += child.getScore();
            }

            node.setScore(score);
        }

        private void computeScores() {
            computeScores(root, 
                          dataRows, 
                          dataRows.get(0).getNumberOfExplanatoryVariables());
        }

        /**
         * Computes the scores of children of {@code parentNode}, and then computes
         * the initial score of {@code parentNode}.
         * 
         * @param currentDepth the current depth of the input {@code parentNode}.
         * @param dataRows     the list of relevant data rows.
         * @param parentNode   the parent node.
         */
        private void computeInitialScores(
                int currentDepth,
                List<DataRow<C>> dataRows,
                ParsimoniousContextTreeNode<C> parentNode) {
            int characterIndex =
                    dataRows.get(0).getNumberOfExplanatoryVariables()
                    - currentDepth;

            // Compute the scores for the children:
            Map<C, Map<C, Integer>> mapCharToCounterMap = new HashMap<>();
            Map<C, ParsimoniousContextTreeNode<C>> mapCharToNode = new HashMap<>();

            // Map each single character label to corresponding PCT node:
            for (ParsimoniousContextTreeNode<C> child : parentNode.getChildren()) {
                mapCharToNode.put(child.getLabel().iterator().next(), child);
            }

            // Initialize all the character counts for each alphabet character:
            for (C character : alphabet.getCharacters()) {
                mapCharToCounterMap.put(character, new HashMap<>());
            }

            // Do the actual counting:
            for (DataRow<C> dataRow : dataRows) {
                C currentCharacter = dataRow.getExplanatoryVariable(characterIndex);
                Map<C, Integer> characterMap = 
                        mapCharToCounterMap.get(currentCharacter);
                C responseCharacter = dataRow.getResponseVariable();
                characterMap.put(responseCharacter,
                                 characterMap
                                         .getOrDefault(responseCharacter, 0) + 1);
            }

            // Build the actual child node scores:
            for (ParsimoniousContextTreeNode<C> child : parentNode.getChildren()) {
                Map<C, Integer> characterCountMap = 
                        mapCharToCounterMap.get(child.getLabel().iterator().next());

                double score = -k;
                int count = 0;

                for (Map.Entry<C, Integer> entry : characterCountMap.entrySet()) {
                    count += entry.getValue();
                }

                for (Map.Entry<C, Integer> entry : characterCountMap.entrySet()) {
                    score += entry.getValue() * 
                             Math.log(((double) entry.getValue()) / count);
                }

                child.setScore(score);
            }

            // Set the parent node score:
            double parentScore = 0.0;

            for (ParsimoniousContextTreeNode<C> child : parentNode.getChildren()) {
                parentScore += child.getScore();
            }

            parentNode.setScore(parentScore);
        }
        

        private double findMergedScore(
                 int depth,
                 Map<C, List<DataRow<C>>> mapCharacterToDataRows,
                 ParsimoniousContextTreeNode<C> node1,
                 ParsimoniousContextTreeNode<C> node2) {
            int charIndex = 
                    dataRows.get(0).getNumberOfExplanatoryVariables() - depth;
            int count = 0;
            double score = -k;
            characterCountMap.clear();

            // Get the data row count:
            for (C character : node1.getLabel()) {
                count += mapCharacterToDataRows.get(character).size();
            }

            for (C character : node2.getLabel()) {
                count += mapCharacterToDataRows.get(character).size();
            }

            // Build the N_{V_a} values:
            for (C character : node1.getLabel()) {
                for (DataRow<C> dataRow : mapCharacterToDataRows.get(character)) {
                    C responseVariable = dataRow.getResponseVariable();
                    characterCountMap.put(responseVariable, 
                                          characterCountMap
                                                  .getOrDefault(responseVariable, 0) 
                                                  + 1);
                }
            }

            for (C character : node2.getLabel()) {
                for (DataRow<C> dataRow : mapCharacterToDataRows.get(character)) {
                    C responseVariable = dataRow.getResponseVariable();
                    characterCountMap.put(responseVariable, 
                                          characterCountMap
                                                  .getOrDefault(responseVariable, 0) 
                                                  + 1);
                }
            }

            for (Map.Entry<C, Integer> entry : characterCountMap.entrySet()) {
                score += entry.getValue() *
                         Math.log(((double) entry.getValue()) / count);
            }

            return score;
        }

        private double findSingleChildScore(int depth, List<DataRow<C>> dataRows) {
            int count = dataRows.size();
            double score = -k;
            characterCountMap.clear();

            for (DataRow<C> dataRow : dataRows) {
                C responseVariable = dataRow.getResponseVariable();
                characterCountMap.put(responseVariable, 
                                      characterCountMap
                                              .getOrDefault(responseVariable, 0)
                                              + 1);
            }

            for (Map.Entry<C, Integer> entry : characterCountMap.entrySet()) {
                score += entry.getValue() *
                         Math.log(((double) entry.getValue()) / count);
            }

            return score;
        }
    }
    
    public static void main(String[] args) {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
        dataRows.add(new DataRow<>(1, 1, 2, 3, 3));
        dataRows.add(new DataRow<>(1, 2, 2, 1, 1));
        dataRows.add(new DataRow<>(2, 3, 1, 2, 2));
        dataRows.add(new DataRow<>(3, 1, 1, 2, 2));
        dataRows.add(new DataRow<>(3, 3, 3, 3, 1));
        
        HybridHeuristicParsimoniousContextTreeLearnerV1<Integer> learner = 
                new HybridHeuristicParsimoniousContextTreeLearnerV1<>();
        
        ParsimoniousContextTree<Integer> tree = learner.learn(dataRows);
        
        System.out.println(tree);
    }
}

// Creating the children for a node takes: 
