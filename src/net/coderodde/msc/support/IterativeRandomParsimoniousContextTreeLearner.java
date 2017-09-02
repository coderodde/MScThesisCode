package net.coderodde.msc.support;

import java.util.List;
import java.util.Random;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

public final class IterativeRandomParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C>{

    private int iterations;
    private Random random;
    
    public void setRandom(Random random) {
        this.random = random;
    }
    
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        RandomParsimoniousContextTreeLearner<C> learner = 
                new RandomParsimoniousContextTreeLearner<>();
        learner.setRandom(random);
        
        double bestScore = Double.NEGATIVE_INFINITY;
        ParsimoniousContextTree<C> bestTree = null;
        
        for (int i = 0; i < iterations; ++i) {
            ParsimoniousContextTree<C> tree = learner.learn(listOfDataRows);
            
            if (bestScore < tree.getScore()) {
                bestScore = tree.getScore();
                bestTree = tree;
            }
        }
        
        return bestTree;
    }
}
