package net.coderodde.msc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.DoubleStream;
import net.coderodde.msc.support.PartitionIterable;
import de.jstacs.utils.random.DirichletMRG;
import de.jstacs.utils.random.DirichletMRGParams;

/**
 * This class implements a data generating PCT of a prespecified depth
 * <code>d</code>. When sampling this PCT, a string of length <code>d</code> is
 * required. Then, this PCT is traversed according to the input string which 
 * will lead to a leaf node. There, a single character is sampled according to
 * the (Dirichlet) distribution of that leaf.
 */
public final class DataGeneratingPCT {
    
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
    
    /**
     * Maps a number of blocks {@code B} in the alphabet to a list of alphabet 
     * partitions consisting of exactly {@code B} blocks. Other names for blocks
     * are <i>parts</i> or <i>cells</i>.
     */
    private final Map<Integer, List<List<Set<Character>>>> partitionMap = 
            new HashMap<>();
    
    /**
     * The depth of this PCT. Each instance of this class requires a 
     * <code>depth</code>-string in order to sample a new character.
     */
    private final int depth;
    
    /**
     * The root node of this tree.
     */
    private PCTNode root;
    private final double[] weights;
    private final Random random = new Random();
    
    public DataGeneratingPCT(int depth, int alphabetSize, double[] weights) {
        if (depth < 1) {
            throw new IllegalArgumentException("Bad depth: " + depth);
        }
        
        if (weights.length != alphabetSize) {
            throw new IllegalArgumentException("Bad weight array.");
        }
        
        double sum = DoubleStream.of(weights).sum();
        
        // Normalize weights:
        for (int i = 0; i < weights.length; ++i) {
            weights[i] /= sum;
        }
        
        this.depth = depth;
        this.weights = weights;
        
        Character[] chars = new Character[alphabetSize];
        char c = 'a';
        
        for (int i = 0; i < alphabetSize; ++i) {
            chars[i] = c++;
        }
        
        this.alphabet = new Alphabet<>(chars);
        this.params = new DirichletMRGParams(DIRICHLET_CONCENTRATION_PARAMETER, 
                                             this.alphabet.size());
        
        for (int blocks = 1; blocks <= alphabet.size(); ++blocks) {
            PartitionIterable<Character> partitionIterable = 
                    new PartitionIterable<>(alphabet.getCharacters(), blocks);
            
            partitionMap.put(blocks, new ArrayList<>());
            
            for (List<Set<Character>> partition : partitionIterable) {
                partitionMap.get(blocks).add(partition);
            }
        }
        
        buildTree();
    }
    
    public List<Character> getAlphabetCharacters() {
        return alphabet.getCharacters();
    }
    
    // Builds the entire data sampling PCT.
    private void buildTree() {
        // Build the root:
        this.root = new PCTNode();
        int blocks = getNumberOfBlocks();
        
        List<List<Set<Character>>> all = partitionMap.get(blocks);
        List<Set<Character>> childNodeLabels = choose(all);
        
        this.root.label = Collections.emptySet();
        this.root.children = new HashSet<>(childNodeLabels.size());
        // Here, the root is set up.
        
        // Recur to build the children:
        for (Set<Character> label : childNodeLabels) {
            PCTNode childNode = new PCTNode();
            childNode.label = label;
            buildTree(childNode, depth - 1);
            this.root.children.add(childNode);
        }
    }
    
    // This method is responsible for building the PCT. An input node is 
    // continued if it is not a leaf node. Otherwise, we attach to a leaf node
    // a Dirichlet distribution with all concentration parameters set to 0.5.
    private void buildTree(PCTNode node, int depth) {
        if (depth == 0) {
            // Deal with a leaf:
            node.characters = alphabet.getCharacters();
            node.characterWeights = new double[node.characters.size()];
            DirichletMRG.DEFAULT_INSTANCE.generate(node.characterWeights, 
                                                   0, 
                                                   node.characterWeights.length, 
                                                   params);
            return;
        }
        
        // Not a leaf, keep building:
        int blocks = getNumberOfBlocks();
        
        List<List<Set<Character>>> all = partitionMap.get(blocks);
        List<Set<Character>> childNodeLabels = choose(all);
        
        node.children = new HashSet<>(childNodeLabels.size());
        
        for (Set<Character> label : childNodeLabels) {
            // Deal with the children:
            PCTNode childNode = new PCTNode();
            childNode.label = label;
            buildTree(childNode, depth - 1);
            node.children.add(childNode);
        }
    }
    
    // Randomly and uniformly selects a number of blocks. (TODO: What blocks?)
    private int getNumberOfBlocks() {
        double coin = random.nextDouble();
        int i = 0;
        
        while (coin >= weights[i]) {
            coin -= weights[i++];
        }
        
        return i + 1;
    }

    /**
     * Given an input string {@code string}, traverses this PCT starting from
     * the root node and going to a leaf node according to the input string. 
     * When a leaf node is reached, that node is sampled and a single character
     * is returned. The idea is that, the input string is continued according to
     * distribution.
     * 
     * @param string the input string
     * @return 
     */
    public Character sampleNext(String string) {
        if (string.length() != depth) {
            throw new IllegalArgumentException(
                    "Bad length: " + string.length() + ". Must be " + depth);
        }
        
        PCTNode currentNode = root;
        
        outer:
        for (char c : string.toCharArray()) {
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
    
    /**
     * Randomly and uniformly chooses an element in {@code list} and returns it.
     * 
     * @param <T>  the list element type.
     * @param list the list to choose from.
     * @return a randomly chosen list element.
     */
    private <T> T choose(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * Returns a textual representation of this data generating PCT.
     * 
     * @return a textual representation of this PCT.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.root.label);
        sb.append("\n");
        
        for (PCTNode node : this.root.children) {
            toString(node, sb, "  ");
        }
        
        return sb.toString();
    }
    
    // Implements the conversion from a data-generating PCT to textual 
    // represenation.
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
    
    public static void mainOld(String[] args) {
        double[] weights = { 2.0, 2, 2.0, 2.0, 2, 2.0 };
        
        DataGeneratingPCT pct = new DataGeneratingPCT(2, 6, weights);
        System.out.println(pct);
    }
}
