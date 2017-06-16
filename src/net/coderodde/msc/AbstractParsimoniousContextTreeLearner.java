package net.coderodde.msc;

import java.util.List;

/**
 * This abstract class defines the API for parsimonious context tree learners.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 15, 2017)
 * @param <C> the character type.
 */
public abstract class AbstractParsimoniousContextTreeLearner<C> {
    
    /**
     * Learns a parsimonious context tree from the list of data rows.
     * 
     * @param alphabet the alphabet.
     * @param listOfDataRows the list of data rows.
     * @return a parsimonious context tree.
     */
    public abstract ParsimoniousContextTree<C> 
        learn(Alphabet<C> alphabet, List<DataRow<C>> listOfDataRows);
}
