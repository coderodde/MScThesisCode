package net.coderodde.msc.support;

import java.util.Comparator;
import java.util.List;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

/**
 * This class implements an iterative radix PCT learner.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 3, 2018)
 */
public class IterativeRadixParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        ParsimoniousContextTree<C> bestTree = null;
        double bestTreeScore = Double.NEGATIVE_INFINITY;
        RadixParsimoniousContextTreeLearner<C> learner = 
                new RadixParsimoniousContextTreeLearner<>(
                        new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) o1).compareTo(o2);
            }
        }, 1);
        
        for (int i = 1; i <= listOfDataRows.size(); ++i) {
            learner.setMinimumLabelSize(i);
            ParsimoniousContextTree<C> tree = 
                    learner.learn(listOfDataRows);
            
            if (bestTreeScore < tree.getScore()) {
                bestTreeScore = tree.getScore();
                bestTree = tree;
            }
        }
        
        return bestTree;
    }
}
