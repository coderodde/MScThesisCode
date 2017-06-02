package net.coderodde.msc;

import java.util.Collections;
import java.util.HashSet;
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

    private static final class ParsimoniousContextTreeNode<C> {
        
        private final Set<C> label;
        private final Set<ParsimoniousContextTreeNode<C>> children;
        private final double score;
        private final
                ResponseVariableDistribution<C> responseVariableDistribution;
        
        ParsimoniousContextTreeNode(Set<C> label,
                                    Set<ParsimoniousContextTreeNode<C>> children,
                                    double score,
                                    ResponseVariableDistribution<C> responseVariableDistribution) {
            this.label = label;
            this.children = null;
            this.responseVariableDistribution = null;
            this.score = 0.0;
        }
    }
    
    private final Alphabet alphabet;
    private ParsimoniousContextTreeNode<C> root;
    
    public ParsimoniusContextTree(Alphabet<C> alphabet, 
                                  List<DataRow<C>> dataRowList) {
        this.alphabet = 
                Objects.requireNonNull(
                        alphabet, 
                        "The input alphabet is null.");
        root = new ParsimoniousContextTreeNode<>(Collections.<C>emptySet(),
                                                 getAllChildren(),
                                                 0.0,
                                                 null);
        
    }
    
    private Set<ParsimoniousContextTreeNode<C>> getAllChildren() {
        Set<ParsimoniousContextTreeNode<C>> childSet = new HashSet<>(
                alphabet.getNumberOfNonemptyCharacterCombinations());
        
        return childSet;
    }
}
