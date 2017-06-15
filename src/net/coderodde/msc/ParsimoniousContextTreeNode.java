package net.coderodde.msc;

import java.util.Set;

/**
 * This class implements a node in parsimonious context trees.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (June 15, 2017)
 * @param <C> the character type.
 */
public final class ParsimoniousContextTreeNode<C> {

    private Set<C> label;
    
    private Set<ParsimoniousContextTreeNode<C>> children;
    
    private ResponseVariableDistribution<C> responseVariableDistribution;
    
    private double score;
    
    public void setLabel(Set<C> label) {
        this.label = label;
    }
    
    public Set<C> getLabel() {
        return this.label;
    }
    
    public void setChildren(Set<ParsimoniousContextTreeNode<C>> children) {
        this.children = children;
    }
    
    public Set<ParsimoniousContextTreeNode<C>> getChildren() {
        return this.children;
    }
    
    public void setResponseVariableDistribution
        (ResponseVariableDistribution<C> responseVariableDistribution) {
        this.responseVariableDistribution = responseVariableDistribution;
    }
        
    public ResponseVariableDistribution getResponseVariableDistribution() {
        return this.responseVariableDistribution;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public double getScore() {
        return this.score;
    }
}
