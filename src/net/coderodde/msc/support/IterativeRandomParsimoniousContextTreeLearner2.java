package net.coderodde.msc.support;

import java.util.List;
import java.util.Random;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

public final class IterativeRandomParsimoniousContextTreeLearner2<C>
extends AbstractParsimoniousContextTreeLearner<C>{

    private int iterations;
    private Random random;
    private double beta = 0.8;
    
    public void setRandom(Random random) {
        this.random = random;
    }
    
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    
    public void setBeta(double beta) {
        this.beta = beta;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        RandomParsimoniousContextTreeLearnerV2<C> learner = 
                new RandomParsimoniousContextTreeLearnerV2<>();
        learner.setBeta(beta);
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
