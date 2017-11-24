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
    
    private static final class PCTNode {
        Set<Character> label;
        Set<PCTNode> children;
        List<Character> characters;
        double[] characterWeights;
        
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
    
    private final DirichletMRGParams params;
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
        char c = ' ';
        
        for (int i = 0; i < alphabetSize; ++i) {
            chars[i] = c++;
        }
        
        this.depth = depth;
        this.alphabet = new Alphabet<>(chars);
        this.characterList = this.alphabet.getCharacters();
        this.params = new DirichletMRGParams(0.5, this.alphabet.size());
        
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
            PCTNode childNode = new PCTNode();
            childNode.label = label;
            buildTree(childNode, depth - 1);
            this.root.children.add(childNode);
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
        
        for (Set<Character> label : childrenLabels) {
            PCTNode childNode = new PCTNode();
            childNode.label = label;
            buildTree(childNode, depth - 1);
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
}
