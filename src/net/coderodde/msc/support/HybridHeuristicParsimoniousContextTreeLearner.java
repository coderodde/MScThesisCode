package net.coderodde.msc.support;

import java.util.List;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

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
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        
        
        return null;
    }
    
    private static int getWorkEstimate(int alphabetSize) {
        return alphabetSize * BELL_NUMBERS[alphabetSize] + 
               alphabetSize * intPow(2, alphabetSize) +
               intPow(2, alphabetSize);
    }
    
    private static int intPow(int base, int exponent) {
        int result = 1;
        
        for (int i = 0; i < exponent; i++)  {
            result *= base;
        }
        
        return result;
    }
}

// Creating the children for a node takes: 
