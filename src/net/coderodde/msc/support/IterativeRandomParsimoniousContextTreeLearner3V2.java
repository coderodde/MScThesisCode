package net.coderodde.msc.support;

import java.util.List;
import java.util.Random;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

public final class IterativeRandomParsimoniousContextTreeLearner3V2<C>
extends AbstractParsimoniousContextTreeLearner<C>{

    private int iterations;
    private int maximumLabelsPerNode;
    private Random random;
    
    public void setRandom(Random random) {
        this.random = random;
    }
    
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    
    public void setMaximumLabelsPerNode(int maximumLabelsPerNode) {
        this.maximumLabelsPerNode = maximumLabelsPerNode;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        BasicIterativeRandomLearnerV2<C> learner = 
                new BasicIterativeRandomLearnerV2<>();
        learner.setRandom(random);
        learner.setMaximumLabelsPerNode(maximumLabelsPerNode);
        
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
