package net.coderodde.msc.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;
import net.coderodde.msc.ResponseVariableDistribution;

/**
 * This class implements the radix parsimonious context tree learner.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 1, 2018)
 * @param <C> the character type.
 */
public class RadixParsimoniousContextTreeLearner<C>
        extends AbstractParsimoniousContextTreeLearner<C> {

    private final Comparator<C> comparator;
    private final int minimumLabelSize;
    
    public RadixParsimoniousContextTreeLearner(
            Comparator<C> comparator,
            int minimumLabelSize) {
        if (comparator == null) {
            this.comparator = new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    return ((Comparable) o1).compareTo(o2);
                }
            };
        } else {
            this.comparator = comparator;
        }
        
        this.minimumLabelSize = minimumLabelSize;
    }

    static final class DataRowComparator<C> implements Comparator<DataRow<C>> {
        private final Comparator<? super C> variableComparator;
        private int variableIndex;
        
        DataRowComparator(Comparator<? super C> variableComparator) {
            this.variableComparator = variableComparator;
        }
        
        void setVariableIndex(int variableIndex) {
            this.variableIndex = variableIndex;
        }

        @Override
        public int compare(DataRow<C> dataRow1, DataRow<C> dataRow2) {
            C c1 = dataRow1.getExplanatoryVariable(variableIndex);
            C c2 = dataRow2.getExplanatoryVariable(variableIndex);
            return variableComparator.compare(c1, c2);
        }
    }
        
    private final class State {

        private double k;
        private final List<DataRow<C>> listOfDataRows;
        private final int depth;
        private ParsimoniousContextTreeNode<C> root;
        private final DataRowComparator<C> dataRowComparator;

        /**
         * The frontier queue for the breadth-first search. We need this whenever
         * asking whether a data row leads to a particular leaf node.
         */
        private Deque<ParsimoniousContextTreeNode<C>> queue;

        /**
         * Maps each node to its depth in the breadth-first search.
         */
        private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap;

        State(List<DataRow<C>> listOfDataRows,
              Comparator<C> comparator) {
            this.listOfDataRows = listOfDataRows;
            checkDataRowListNotEmpty(listOfDataRows);
            checkDataRowListHasConstantNumberOfExplanatoryVariables(
                    listOfDataRows);
            this.depth = listOfDataRows.get(0)
                    .getNumberOfExplanatoryVariables();
            this.dataRowComparator = new DataRowComparator<>(comparator);
        }

        ParsimoniousContextTree<C> buildTree() {
            this.root = new ParsimoniousContextTreeNode<>();
            buildTree(this.root, listOfDataRows, this.depth);
            return new ParsimoniousContextTree<>(this.root);
        }

        private void buildTree(ParsimoniousContextTreeNode<C> node, 
                               List<DataRow<C>> subList,
                               int depth) {
            if (depth == 0) {
                node.setScore(computeBayesianInformationCriterion(node));
                return;
            }
            
            dataRowComparator.setVariableIndex(0);
            Collections.sort(subList, dataRowComparator);
            Map<C, List<Integer>> m = new HashMap<>();
            
            for (int rowIndex = 0; 
                    rowIndex < subList.size(); 
                    rowIndex++) {
                C character = subList
                        .get(rowIndex)
                        .getExplanatoryVariable(this.depth - depth);
                if (!m.containsKey(character)) {
                    m.put(character, new ArrayList<>());
                }
                
                m.get(character).add(rowIndex);
            }
            
            Set<ParsimoniousContextTreeNode<C>> childrenSet = new HashSet<>();
            
            for (List<Integer> bucket : m.values()) {
                if (bucket.size() <= minimumLabelSize) {
                    childrenSet.add(convertToIndependenceModel(depth));
                } else {
                    
                }
            }
        }

        private ParsimoniousContextTreeNode<C> 
        convertToIndependenceModel(int depth) {
            return null;
        }
        
        private boolean dataRowMatchesLeafNode(
                DataRow<C> dataRow,
                ParsimoniousContextTreeNode<C> leafNode) {
            this.queue.clear();
            this.depthMap.clear();
            int treeDepth = this.listOfDataRows
                    .get(0).getNumberOfExplanatoryVariables();

            for (ParsimoniousContextTreeNode<C> childOfRoot
                    : root.getChildren()) {
                if (childOfRoot.getLabel()
                        .contains(dataRow.getExplanatoryVariable(0))) {
                    this.queue.addLast(childOfRoot);
                    this.depthMap.put(childOfRoot, 1);
                }
            }

            while (!this.queue.isEmpty()) {
                ParsimoniousContextTreeNode<C> currentNode
                        = this.queue.removeFirst();
                int currentNodeDepth = this.depthMap.get(currentNode);

                if (currentNodeDepth == treeDepth) {
                    if (currentNode == leafNode) {
                        return true;
                    }
                } else {
                    C targetChar = dataRow.getExplanatoryVariable(currentNodeDepth);

                    for (ParsimoniousContextTreeNode<C> child
                            : currentNode.getChildren()) {
                        if (child.getLabel().contains(targetChar)) {
                            this.queue.addLast(child);
                            this.depthMap.put(child, currentNodeDepth + 1);
                        }
                    }
                }
            }

            return false;
        }

        private double computeBayesianInformationCriterion(
                ParsimoniousContextTreeNode<C> node) {
            this.characterCountMap.clear();
            int totalCount = 0;

            for (DataRow<C> dataRow : this.listOfDataRows) {
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

            ResponseVariableDistribution<C> distribution
                    = new ResponseVariableDistribution<>();

            double score = -this.k;

            for (Map.Entry<C, Integer> e : this.characterCountMap.entrySet()) {
                score += e.getValue()
                        * Math.log((1.0 * e.getValue()) / totalCount);
                distribution.putResponseVariableProbability(
                        e.getKey(),
                        Double.valueOf(e.getValue()) / totalCount);
            }

            node.setResponseVariableDistribution(distribution);
            return score;
        }

        private Map<C, Integer> characterCountMap = new HashMap<>();
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        State state = new State(Objects.requireNonNull(listOfDataRows), 
                                comparator);
        return state.buildTree();
    }
}
