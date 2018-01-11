package net.coderodde.msc.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;
import net.coderodde.msc.ResponseVariableDistribution;

/**
 * This class implements a PCT learner that begins with radix based learner and
 * finishes the small buckets via random algorithm.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 11, 2018)
 */
public class RadixRandomParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    private final Comparator<C> comparator;
    private int minimumLabelSize;
    private Random random = new Random();
    
    public RadixRandomParsimoniousContextTreeLearner(Comparator<C> comparator,
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
    
    public void setMinimumLabelSize(int minimumLabelSize) {
        this.minimumLabelSize = minimumLabelSize;
    }
    
    public void setRandom(Random random) {
        this.random = random;
    }
    
    public static final class DataRowComparator<C> 
            implements Comparator<DataRow<C>> {

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
        private final int minimumLabelSize;
        private double k;
        private final List<DataRow<C>> listOfDataRows;
        private final int depth;
        private ParsimoniousContextTreeNode<C> root;
        private final DataRowComparator<C> dataRowComparator;
        private final Alphabet<C> alphabet;
        private final Set<C> alphabetAsSet;
        
        private Deque<ParsimoniousContextTreeNode<C>> queue = 
                new ArrayDeque<>();
        
        private Map<ParsimoniousContextTreeNode<C>, Integer> depthMap =
                new HashMap<>();
        
        State(List<DataRow<C>> listOfDataRows, int minimumLabelSize) {
            this.listOfDataRows = new ArrayList<>(listOfDataRows);
            this.alphabet = getAlphabet(listOfDataRows);
            this.alphabetAsSet = new HashSet<>(this.alphabet.getCharacters());
            checkDataRowListNotEmpty(listOfDataRows);
            checkDataRowListHasConstantNumberOfExplanatoryVariables(
                    listOfDataRows);
            this.depth = listOfDataRows.get(0)
                                       .getNumberOfExplanatoryVariables();
            this.dataRowComparator = new DataRowComparator<>(comparator);
            this.minimumLabelSize = minimumLabelSize;
            this.k = 0.5 * (this.alphabet.size() - 1) * 
                            Math.log(listOfDataRows.size());
            this.root = new ParsimoniousContextTreeNode<>();
            this.root.setLabel(Collections.emptySet());
        }
        
        ParsimoniousContextTree<C> buildTree() {
            buildTree(this.root, this.listOfDataRows, this.depth);
            return new ParsimoniousContextTree<>(this.root);
        }
        
        private void buildTree(ParsimoniousContextTreeNode<C> node,
                               List<DataRow<C>> subList,
                               int depth) {
            if (depth == 0) {
                node.setScore(computeBayesianInformationCriterion(node));
                return;
            }
            
            if (subList.size() <= minimumLabelSize) {
                // TODO
                return;
            }
            
            dataRowComparator.setVariableIndex(this.depth - depth);
            Collections.sort(subList, dataRowComparator);
            
            Map<C, List<DataRow<C>>> m = new HashMap<>();
            
            for (int rowIndex = 0;
                    rowIndex < subList.size();
                    rowIndex++) {
                C character = subList
                        .get(rowIndex)
                        .getExplanatoryVariable(this.depth - depth);
                
                if (!m.containsKey(character)) {
                    m.put(character, new ArrayList<>());
                }
                
                m.get(character).add(subList.get(rowIndex));
            }
            
            Set<ParsimoniousContextTreeNode<C>> children = 
                    new HashSet<>(m.size());
            
            node.setChildren(children);
            double score = 0.0;
            
            for (Map.Entry<C, List<DataRow<C>>> e : m.entrySet()) {
                Set<C> label = new HashSet<>(1);
                label.add(e.getKey());
                ParsimoniousContextTreeNode<C> newChild = 
                        new ParsimoniousContextTreeNode<>();
                newChild.setLabel(label);
                children.add(newChild);
                buildTree(newChild, e.getValue(), depth - 1);
                score += newChild.getScore();
            }
            
            node.setScore(score);
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
        
        private Map<C, Integer> characterCountMap = new HashMap<>();

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
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
