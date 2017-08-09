package net.coderodde.msc.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
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
        
        root =  new ParsimoniousContextTreeNode<>();
        root.setLabel(new HashSet<>()); // The root's label is empty.
        
        buildExtendedPCT();
        
        return new ParsimoniousContextTree<>(root);
    }
    
    private void buildExtendedPCT() {
        createListOfAllPossibleLabels();
        
        int depth = 
                this.listOfDataRows.get(0).getNumberOfExplanatoryVariables();
        
        buildExtendedPCT(root, depth);
    }
    
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
    
    private void createListOfAllPossibleLabels() {
        this.listOfAllPossibleNodeLabels = 
                new ArrayList<>(alphabet.getNumberOfNonemptyCharacterCombinations());
        
        boolean[] flags = new boolean[alphabet.size()];
        
        while (incrementCombinationFlags(flags)) {
            this.listOfAllPossibleNodeLabels.add(loadCharacterCombination(alphabet, flags));
        }
    }
    
    private static boolean incrementCombinationFlags(boolean[] flags) {
        int alphabetSize = flags.length;
        
        for (int i = 0; i < alphabetSize; ++i) {
            if (flags[i] == false) {
                flags[i] = true;
                return true;
            } else {
                flags[i] = false;
            }
        }
        
        return false;
    }
    
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
}
