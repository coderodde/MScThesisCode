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

    /**
     * The labels of this node.
     */
    private Set<C> label;
    
    /**
     * The set of child nodes.
     */
    private Set<ParsimoniousContextTreeNode<C>> children;
    
    /**
     * The conditional probability distribution of the response variable.
     */
    private ResponseVariableDistribution<C> responseVariableDistribution;
    
    /**
     * The score of this tree node.
     */
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
    
    @Override
    public String toString() {
        return getLabel().toString();
    }
    
    public void convertToTextualRepresentation(StringBuilder stringBuilder,
                                               String indentation,
                                               String separator) {
        stringBuilder.append(indentation)
                     .append("[{");
        String tokenSeparator = "";
        
        for (C ch : getLabel()) {
            stringBuilder.append(tokenSeparator)
                         .append(ch);
            tokenSeparator = ",";
        }
        
        stringBuilder.append("} score ")
                     .append(getScore())
                     .append("]\n");
        
        if (getChildren() != null) {
            for (ParsimoniousContextTreeNode<C> child : getChildren()) {
                child.convertToTextualRepresentation(stringBuilder, 
                                                     separator + indentation, 
                                                     separator);
            }
        }
    }
}
