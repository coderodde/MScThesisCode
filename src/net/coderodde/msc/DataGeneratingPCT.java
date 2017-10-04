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

public final class DataGeneratingPCT {
    
    private static final class PCTNode {
        Set<Character> label;
        Set<PCTNode> children;
        ResponseVariableDistribution<Character> responseVarDistribution;
    }
    
    private final Alphabet<Character> alphabet;
    private final Map<Integer, List<List<Set<Character>>>> partitionMap = 
            new HashMap<>();
    
    private final int depth;
    
    private PCTNode root;
    private final double[] probs;
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
        
        for (int i = 0; i < weights.length; ++i) {
            weights[i] /= sum;
        }
        
        this.depth = depth;
        this.probs = new double[alphabetSize];
        this.weights = weights;
        
        Character[] chars = new Character[alphabetSize];
        char c = 'a';
        
        for (int i = 0; i < alphabetSize; ++i) {
            chars[i] = c++;
        }
        
        this.alphabet = new Alphabet<Character>(chars);
        
        for (int blocks = 1; blocks <= alphabet.size(); ++blocks) {
            PartitionIterable<Character> pi = 
                    new PartitionIterable<>(alphabet.getCharacters(), blocks);
            
            partitionMap.put(blocks, new ArrayList<>());
            
            for (List<Set<Character>> partition : pi) {
                partitionMap.get(blocks).add(partition);
            }
        }
        
        buildTree();
    }
    
    private void buildTree() {
        this.root = new PCTNode();
        int blocks = getBlocks();
        
        List<List<Set<Character>>> all = partitionMap.get(blocks);
        List<Set<Character>> childNodeLabels = choose(all);
        
        this.root.label = Collections.emptySet();
        this.root.children = new HashSet<>(childNodeLabels.size());
        
        for (Set<Character> label : childNodeLabels) {
            PCTNode childNode = new PCTNode();
            childNode.label = label;
            buildTree(childNode, depth - 1);
            this.root.children.add(childNode);
        }
    }
    
    private void buildTree(PCTNode node, int depth) {
        if (depth == 0) {
            return;
        }
        
        int blocks = getBlocks();
        
        List<List<Set<Character>>> all = partitionMap.get(blocks);
        List<Set<Character>> childNodeLabels = choose(all);
        
        node.children = new HashSet<>(childNodeLabels.size());
        
        for (Set<Character> label : childNodeLabels) {
            PCTNode childNode = new PCTNode();
            childNode.label = label;
            buildTree(childNode, depth - 1);
            node.children.add(childNode);
        }
    }
    
    private int getBlocks() {
        double coin = random.nextDouble();
        int i = 0;
        
        while (coin >= weights[i]) {
            coin -= weights[i++];
        }
        
        return i + 1;
    }

    private Character sample(ResponseVariableDistribution<Character> dist,  
                             Random random) {
        double coin = random.nextDouble();
        
        for (char c = 'a'; c < 'a' + alphabet.size(); ++c) {
            
        }
        
        return null;
    }
    
    private <T> T choose(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
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
        double[] weights = { 1.0, 1.5, 2.0, 2.0, 1.5, 1.0 };
        
        DataGeneratingPCT pct = new DataGeneratingPCT(2, 6, weights);
        System.out.println(pct);
    }
}
