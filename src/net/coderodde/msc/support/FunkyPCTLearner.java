package net.coderodde.msc.support;

import java.util.List;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

/**
 * My Funky PCT learner.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Dec 19, 2017)
 * @param <C> the character type.
 */
public final class FunkyPCTLearner<C> 
extends AbstractParsimoniousContextTreeLearner<C>{

    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        
        // Compute the alphabet first:
        Alphabet<C> alphabet = getAlphabet(listOfDataRows);
        
        // addsaffdsf
        // fdsfdfdsaa
        // afaadfafds
        // affdsafdsa
        
        
        
        return null;
    }
}
