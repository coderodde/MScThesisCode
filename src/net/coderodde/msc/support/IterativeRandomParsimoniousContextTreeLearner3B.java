package net.coderodde.msc.support;

import java.util.List;
import java.util.Random;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

public final class IterativeRandomParsimoniousContextTreeLearner3B<C>
extends AbstractParsimoniousContextTreeLearner<C>{

    private int k;
    private Random random;
    
    public void setRandom(Random random) {
        this.random = random;
    }
    
    public void setK(int k) {
        this.k = k;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        RandomParsimoniousContextTreeLearner3<C> learner = 
                new RandomParsimoniousContextTreeLearner3<>();
        learner.setRandom(random);
        
        int lastImproved = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        ParsimoniousContextTree<C> bestTree = null;
        
        while (lastImproved <= k) {
            ParsimoniousContextTree<C> tree = learner.learn(listOfDataRows);
            
            if (bestScore < tree.getScore()) {
                bestScore = tree.getScore();
                bestTree = tree;
                lastImproved = 0;
            } else {
                lastImproved++;
            }
        }
        
        return bestTree;
    }
}
