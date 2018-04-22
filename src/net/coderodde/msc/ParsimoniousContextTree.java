package net.coderodde.msc;

import java.util.Objects;

public final class ParsimoniousContextTree<C> {

    /**
     * The root node of this parsimonious context tree.
     */
    private final ParsimoniousContextTreeNode<C> root;
    
    public ParsimoniousContextTree(ParsimoniousContextTreeNode<C> root) {
        this.root = Objects.requireNonNull(root, "The root node is null.");
    }
    
    public double getScore() {
        return root.getScore();
    }
    
    public ParsimoniousContextTreeNode<C> getRoot() {
        return root;
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        root.convertToTextualRepresentation(stringBuilder, "", "  ");
        return stringBuilder.toString();
    }
    
    public int getNumberOfRootChildren() {
        return root.getChildren().size();
    }
}
