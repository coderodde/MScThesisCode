package net.coderodde.msc.support;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

/**
 * This class implements the radix parsimonious context tree learner.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 1, 2018)
 * @param <C> the character type.
 */
public class RadixParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    private final Comparator<? super C> comparator;
    
    public RadixParsimoniousContextTreeLearner(
            Comparator<? super C> comparator) {
        if (comparator == null) {
            this.comparator = new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    return ((Comparable) o1).compareTo(o2);
                }
            };
        } else {
            this.comparator = comparator;
        }
    }
    
    private final class State {
        
        private final List<DataRow<C>> listOfDataRows;
        private final int depth;
        
        State(List<DataRow<C>> listOfDataRows) {
            this.listOfDataRows = listOfDataRows;
            checkDataRowListNotEmpty(listOfDataRows);
            checkDataRowListHasConstantNumberOfExplanatoryVariables(
                    listOfDataRows);
            this.depth = listOfDataRows.get(0)
                                       .getNumberOfExplanatoryVariables();
        }
        
        ParsimoniousContextTree<C> buildTree() {
            return null;
        }
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        State state = new State(Objects.requireNonNull(listOfDataRows));
        return state.buildTree();
    }
}
