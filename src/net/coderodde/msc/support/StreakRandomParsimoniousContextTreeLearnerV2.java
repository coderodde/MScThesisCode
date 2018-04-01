package net.coderodde.msc.support;

import java.util.List;
import java.util.Random;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

/**
 * This class implements a streak version of a random PCT learner
 * {@link RandomParsimoniousContextTreeLearnerV2}. The algorithm keeps creating
 * random PCTs until there is a streak of a particular length of generated PCTs
 * that do not improve the current best known PCT.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 1, 2018)
 */
public final class StreakRandomParsimoniousContextTreeLearnerV2<C>
extends AbstractParsimoniousContextTreeLearner<C> {
    
    /**
     * The default maximum streak length.
     */
    private static final int DEFAULT_MAXIMUM_STREAK_LENGTH = 100;
    
    /**
     * The default value for the Beta parameter.
     */
    private static final double DEFAULT_BETA = 0.9;
    
    /**
     * The actual maximum streak length.
     */
    private int maximumStreakLength = DEFAULT_MAXIMUM_STREAK_LENGTH;
    
    /**
     * The actual value of the Beta parameter.
     */
    private double beta = DEFAULT_BETA;
    
    public void setMaximumStreakLength(int maximumStreakLength) {
        this.maximumStreakLength = maximumStreakLength;
    }
    
    public void setBeta(double beta) {
        this.beta = beta;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> dataRows) {
        RandomParsimoniousContextTreeLearnerV2<C> learner = 
                new RandomParsimoniousContextTreeLearnerV2<>();
        learner.setRandom(new Random());
        learner.setBeta(beta);
        
        double bestScore = Double.NEGATIVE_INFINITY;
        ParsimoniousContextTree<C> bestTree = null;
        int currentStreakLength = 0;
        
        while (currentStreakLength <= maximumStreakLength) {
            ParsimoniousContextTree<C> tree = learner.learn(dataRows);
            
            if (bestScore < tree.getScore()) {
                bestScore = tree.getScore();
                bestTree = tree;
                currentStreakLength = 0;
            } else {
                currentStreakLength++;
            }
        }
        
        return bestTree;
    }
}
