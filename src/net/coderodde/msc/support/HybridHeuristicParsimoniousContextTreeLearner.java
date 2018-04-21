package net.coderodde.msc.support;

import java.util.List;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;

/**
 * This class implements a hybrid heuristic PCT learner.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 21, 2018)
 */
public final class HybridHeuristicParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    /**
     * Specifies the maximum alphabet size at which the algorithm can switch to
     * optimal search.
     */
    private static final int MAXIMUM_ALPHABET_SIZE = 13;
    
    /**
     * Maps an index to a corresponding Bell number.
     */
    private static final int[] BELL_NUMBERS = 
            new int[MAXIMUM_ALPHABET_SIZE + 1];
    
    /**
     * This constant specifies the maximum allowed work we can perform via 
     * optimal learner. 
     */
    private static final long MAXIMUM_WORK_ESTIMATE = 1_300_000_000_000L;
                             
    /**
     * The heuristic search for learning over too large alphabets.
     */
    private final AbstractParsimoniousContextTreeLearner<C> 
            heuristicLearner = 
            new HeuristicParsimoniousContextTreeLearner<>();
    
    static {
        BELL_NUMBERS[0]  = 1;
        BELL_NUMBERS[1]  = 1;
        BELL_NUMBERS[2]  = 2;
        BELL_NUMBERS[3]  = 5;
        BELL_NUMBERS[4]  = 15;
        BELL_NUMBERS[5]  = 52;
        BELL_NUMBERS[6]  = 203;
        BELL_NUMBERS[7]  = 877;
        BELL_NUMBERS[8]  = 4140;
        BELL_NUMBERS[9]  = 21147;
        BELL_NUMBERS[10] = 115975;
        BELL_NUMBERS[11] = 678570;
        BELL_NUMBERS[12] = 4213597;
        BELL_NUMBERS[13] = 27644437;
    }
    
    private Alphabet<C> alphabet;
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        this.alphabet = getAlphabet(listOfDataRows);
        
        if  (alphabet.size() > MAXIMUM_ALPHABET_SIZE) {
            return heuristicLearner.learn(listOfDataRows);
        }
        
        int depthOfOptimalLearner = getDepthOfOptimalLearner();
        
        if (depthOfOptimalLearner < 1) {
            //TODO: Find out how to do.
            return heuristicLearner.learn(listOfDataRows);
        }
        
        // Once here, we may benefit from optimal learning.
        return buildRoot();
    }
    
    private ParsimoniousContextTreeNode<C> root;
    private int alphabetSize;
    
    private ParsimoniousContextTree<C> buildRoot() {
        root = new ParsimoniousContextTreeNode<>();
        return new ParsimoniousContextTree<>(root);
    }
    
    private int getDepthOfOptimalLearner() {
        int trialDepth = 0;
        
        while (true) {
            long work = getTotalWorkEstimate(trialDepth, alphabetSize);
            
            if (work > MAXIMUM_WORK_ESTIMATE) {
                return trialDepth - 1;
            }
            
            trialDepth++;
        }
    }
    
    private static long getTotalWorkEstimate(int depth, int alphabetSize) {
        long nodes = getNumberOfNodesInTree(depth, alphabetSize);
        long workPerNode = getNodeWorkEstimate(alphabetSize);
        return nodes * workPerNode;
    }
    
    private static long getNumberOfNodesInTree(int depth, int alphabetSize) {
        long numberOfAlphabetCombinations = myPow(2, alphabetSize) - 1;
        long tmp = myPow(numberOfAlphabetCombinations, depth + 1);
        return (1 - tmp) / (1 - numberOfAlphabetCombinations);
    }
    
    private static long getNodeWorkEstimate(int alphabetSize) {
        return alphabetSize * BELL_NUMBERS[alphabetSize] + 
               alphabetSize * myPow(2, alphabetSize) +
               myPow(2, alphabetSize);
    }
    
    private static long myPow(long base, int exponent) {
        int result = 1;
        
        for (int i = 0; i < exponent; i++)  {
            result *= base;
        }
        
        return result;
    }
}

// Creating the children for a node takes: 
