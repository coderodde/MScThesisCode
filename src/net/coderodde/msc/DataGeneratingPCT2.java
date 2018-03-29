package net.coderodde.msc;

import de.jstacs.utils.random.DirichletMRG;
import de.jstacs.utils.random.DirichletMRGParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class DataGeneratingPCT2 {
    
    /**
     * The value for all concentration parameters for the Dirichlet 
     * distribution.
     */
    private static final double DIRICHLET_CONCENTRATION_PARAMETER = 0.5;
    
    /**
     * Implements actual nodes of a data generating PCT.
     */
    private static final class PCTNode {
        
        /**
         * The label of this node.
         */
        Set<Character> label;
        
        /**
         * The set of child nodes unless this node is a leaf.
         */
        Set<PCTNode> children;
        
        /**
         * The list of alphabet characters. Used for sampling at the leafs.
         */
        List<Character> characters;
        
        /**
         * The array of character weights. Used for sampling at the leafs.
         */
        double[] characterWeights;
        
        /**
         * Performs the actual sampling of a leaf.
         * 
         * @param random the random number generator.
         * @return a randomly selected character.
         */
        Character sample(Random random) {
            double coin = random.nextDouble();
            
            for (int i = 0; i < characters.size(); ++i) {
                if (coin < characterWeights[i]) {
                    return characters.get(i);
                }
                
                coin -= characterWeights[i];
            }
            
            throw new IllegalStateException("Should not get here ever.");
        }
    }
    
    /**
     * The parameters for the Dirichelet distribution.
     */
    private final DirichletMRGParams params;
    
    /**
     * The alphabet.
     */
    private final Alphabet<Character> alphabet;
    private final List<Character> characterList;
    private final int depth;
    private PCTNode root;
    private final Random random = new Random();
    
    public DataGeneratingPCT2(int depth, int alphabetSize) {
        if (depth < 1) {
            throw new IllegalArgumentException("Bad depth: " + depth);
        }
        
        Character[] chars = new Character[alphabetSize];
        char c = 'a';
        
        for (int i = 0; i < alphabetSize; ++i) {
            chars[i] = c++;
        }
        
        this.depth = depth;
        this.alphabet = new Alphabet<>(chars);
        this.characterList = this.alphabet.getCharacters();
        this.params = new DirichletMRGParams(DIRICHLET_CONCENTRATION_PARAMETER, 
                                             this.alphabet.size());
        
        buildTree();
    }
    
    public Alphabet getAlphabet() {
        return alphabet;
    }
    
    public Character sampleNext(String string) {
        if (string.length() != depth) {
            throw new IllegalArgumentException(
                    "Bad length: " + string.length() + ". Must be " + depth);
        }
        
        PCTNode currentNode = root;
        
        outer:
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            
            for (PCTNode child : currentNode.children) {
                if (child.label.contains(c)) {
                    currentNode = child;
                    continue outer;
                }
            }
            
            throw new IllegalStateException("Should not get here ever.");
        }
        
        return currentNode.sample(random);
    }
    
    private void buildTree() {
        this.root = new PCTNode();
        this.root.label = Collections.emptySet();
        this.root.children = new HashSet<>();
        
        int children = random.nextInt(alphabet.size()) + 1;
        List<Set<Character>> childrenLabels = new ArrayList<>(children);
        
        for (int i = 0; i < children; ++i) {
            childrenLabels.add(new HashSet<>());
        }
        
        for (Character c : characterList) {
            int childIndex = random.nextInt(childrenLabels.size());
            childrenLabels.get(childIndex).add(c);
        }
        
        for (Set<Character> label : childrenLabels) {
            if (!label.isEmpty()) {
                PCTNode childNode = new PCTNode();
                childNode.label = label;
                buildTree(childNode, depth - 1);
                this.root.children.add(childNode);
            }
        }
    }
    
    private void buildTree(PCTNode node, int depth) {
        if (depth == 0) {
            node.characters = this.characterList;
            node.characterWeights = new double[node.characters.size()];
            DirichletMRG.DEFAULT_INSTANCE.generate(node.characterWeights, 
                                                   0,
                                                   node.characterWeights.length,
                                                   params);
            return;
        }
        
        int children = random.nextInt(alphabet.size()) + 1;
        List<Set<Character>> childrenLabels = new ArrayList<>(children);
        
        for (int i = 0; i < children; ++i) {
            childrenLabels.add(new HashSet<>());
        }
        
        for (Character c : characterList) {
            int childIndex = random.nextInt(childrenLabels.size());
            childrenLabels.get(childIndex).add(c);
        }
        
        node.children = new HashSet<>(childrenLabels.size());
        
        for (Set<Character> label : childrenLabels) {
            if (!label.isEmpty()) {
                PCTNode childNode = new PCTNode();
                childNode.label = label;
                buildTree(childNode, depth - 1);
                node.children.add(childNode);
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.root.label);
        sb.append("\n");
        
        for (PCTNode child : this.root.children) {
            toString(child, sb, "  ");
        }
        
        return sb.toString();
    }
    
    private void toString(PCTNode node,
                          StringBuilder sb,
                          String separator) {
        if (node == null) {
            return;
        }
        
        sb.append(separator).append(node.label).append("\n");
        
        if (node.children != null) {
            for (PCTNode child : node.children) {
                toString(child, sb, separator + "  ");
            }
        }
    }
    
    public static void main(String[] args) {
        DataGeneratingPCT2 pct = new DataGeneratingPCT2(3, 4);
        System.out.println(pct);
    }
}
