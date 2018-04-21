package net.coderodde.msc.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.coderodde.msc.support.PartitionIterable;

public class GeneratingPartitionsDuration {
    
    public static void main(String[] args) {
        int alphabetSize = 13;
        
        List<Integer> alphabet = new ArrayList<>();
        
        for (int i = 0; i < alphabetSize; i++) {
            alphabet.add(i);
        }
        
        long count = 0L;
        long startTime = System.currentTimeMillis();
        
        for (int blocks = 1; blocks <= alphabetSize; blocks++) {
            PartitionIterable<Integer> partitionIterable = 
                    new PartitionIterable<>(alphabet, blocks);
            
            for (List<Set<Integer>> partition : partitionIterable) {
                count++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("Duration: " + (endTime - startTime) + " ms.");
    }
}
