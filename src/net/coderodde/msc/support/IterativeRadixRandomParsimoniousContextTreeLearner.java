package net.coderodde.msc.support;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 12, 2018)
 */
public final class IterativeRadixRandomParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    private Random random = new Random();
    private int globalIterations = 10;
    private int minimumBucketSize = 10;
    private Comparator<C> comparator;
    
    public void setRandom(Random random) {
        this.random = random;
    }
    
    public void setGlobalIterations(int globalIterations) {
        this.globalIterations = globalIterations;
    }
    
    public void setMinimumBucketSize(int minimumBucketSize) {
        this.minimumBucketSize = minimumBucketSize;
    }
    
    public IterativeRadixRandomParsimoniousContextTreeLearner(Comparator<C> comparator,
                                                              int minimumBucketSize) {
        this.comparator = comparator;
        setMinimumBucketSize(minimumBucketSize);
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        ParsimoniousContextTree<C> bestTree = null;
        double bestTreeScore = Double.NEGATIVE_INFINITY;
        RadixRandomParsimoniousContextTreeLearner<C> learner = 
                new RadixRandomParsimoniousContextTreeLearner<>(
                        comparator, minimumBucketSize);
        
        for (int i = 0; i < globalIterations; ++i) {
            for (int bucketSize = 1; bucketSize < listOfDataRows.size() / 4; ++bucketSize) {
                learner.setMinimumLabelSize(bucketSize);
                ParsimoniousContextTree<C> tree = 
                        learner.learn(listOfDataRows);
                
                if (bestTreeScore < tree.getScore()) {
                    bestTreeScore = tree.getScore();
                    bestTree = tree;
                }
            }
        }
        
        
        return bestTree;
    }
    
}
