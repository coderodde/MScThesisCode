package net.coderodde.msc;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
public final class ParsimoniusContextTree<C> {

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
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[{");
            String separator = "";
            
            for (C ch : label) {
                sb.append(separator);
                separator = ",";
                sb.append(ch);
            }
            
            sb.append("} -> ")
              .append(score)
              .append("]");
            
            return sb.toString();
        }
    }
    
    /**
     * The array of boolean flags for generating the children labels of a 
     * parsimonious context tree nodes. We cache this field in order to not 
     * create the flag array every time we are computing the children.
     */
    private final boolean[] combinationFlags;
    
    /**
     * The alphabet of this tree.
     */
    private final Alphabet alphabet;
    
    /**
     * The root node of this tree.
     */
    private ParsimoniousContextTreeNode<C> root;
    
    public ParsimoniusContextTree(Alphabet<C> alphabet, 
                                  List<DataRow<C>> dataRowList) {
        this.alphabet = 
                Objects.requireNonNull(
                        alphabet, 
                        "The input alphabet is null.");
        this.combinationFlags = new boolean[this.alphabet.size()];
        root = new ParsimoniousContextTreeNode<>();
        findOptimalSubtree(root);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Deque<ParsimoniousContextTreeNode<C>> queue = new ArrayDeque<>();
        ParsimoniousContextTreeNode<C> lastNodeOfCurrentLevel = root;
        queue.addLast(root);
        String step = "";
        
        while (!queue.isEmpty()) {
            ParsimoniousContextTreeNode<C> current = queue.removeFirst();
            sb.append(step).append(current.toString());
            
            if (current.children != null) {
                for (ParsimoniousContextTreeNode<C> child : current.children) {
                    queue.addLast(child);
                }
            }
            
            if (lastNodeOfCurrentLevel == current) {
                lastNodeOfCurrentLevel = queue.getLast();
                step += "    ";
            }
        }
        
        return sb.toString();
    }
    
    private Set<ParsimoniousContextTreeNode<C>> getAllChildren() {
        Set<ParsimoniousContextTreeNode<C>> childSet = new HashSet<>(
                alphabet.getNumberOfNonemptyCharacterCombinations());
        final int alphabetSize = alphabet.size();
        
        Arrays.fill(combinationFlags, false);
        int iterated = 0;
        
        outer:
        while (iterated < combinationFlags.length) {
            iterated = 0;
            
            for (int i = 0; i < alphabetSize; ++i) {
                ++iterated;
                if (combinationFlags[i] == false) {
                    combinationFlags[i] = true;
                    /*
                    for (int j = 0; j < i; ++j) {
                        combinationFlags[j] = false;
                    }*/
                    
                    Set<C> childNodeLabel = 
                            loadChildNodeLabel(combinationFlags);
                    ParsimoniousContextTreeNode<C> childTree = 
                            new ParsimoniousContextTreeNode<C>();
                    childTree.label = childNodeLabel;
                    childSet.add(childTree);
                    continue outer;
                } else {
                    combinationFlags[i] = false;
                }
            }
        }
        
        return childSet;
    }
    
    private void findOptimalSubtree(ParsimoniousContextTreeNode<C> node) {
        
    }
    
    private Set<C> loadChildNodeLabel(boolean[] combinationFlags) {
        Set<C> set = new HashSet<>();
        Iterator<C> iterator = alphabet.iterator();
        
        for (int i = 0; i < combinationFlags.length; ++i) {
            if (combinationFlags[i]) {
                set.add(iterator.next());
            } else {
                // Omit the current character:
                iterator.next();
            }
        }
        
        return set;
    }
}
