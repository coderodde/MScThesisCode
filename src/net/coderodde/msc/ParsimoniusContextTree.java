package net.coderodde.msc;

import java.util.List;

/**
 * 
 * @author rodionefremov
 * @param <C> 
 */
public final class ParsimoniusContextTree<C> {

    private static final class ParsimoniusContextTreeNode<C> {
        
        private final Set<C> label;
        private final Set<ParsimoniusContextTreeNode<C>> children;
        private final double score;
        private final ResponseVariableDistribution responseVariableDistribution;
        
        ParsimoniusContextTreeNode(Set<C> label, )
    }
    
    private ParsimoniusContextTreeNode<C> root;
    
    public ParsimoniusContextTree(List<DataRow> dataRowList) {
        
    }
}
