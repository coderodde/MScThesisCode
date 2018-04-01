package net.coderodde.msc.support;

import java.util.List;
import java.util.Random;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

/**
 * This class implements a streak version of a random PCT learner
 * {@link RandomParsimoniousContextTreeLearnerV3}. The algorithm keeps creating
 * random PCTs until there is a streak of a particular length of generated PCTs
 * that do not improve the current best known PCT. 
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 1, 2018)
 */
public final class StreakRandomParsimoniousContextTreeLearnerV3<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    /**
     * The default maximum streak length.
     */
    private static final int DEFAULT_MAXIMUM_STREAK_LENGTH = 100;
    
    /**
     * The default maximum number of children per a non-leaf node.
     */
    private static final int DEFAULT_MAXIMUM_CHILDREN_PER_NODE =
            Integer.MAX_VALUE;
    
    /**
     * The actual maximum streak length.
     */
    private int maximumStreakLength = DEFAULT_MAXIMUM_STREAK_LENGTH;
    
    /**
     * The maximum number of children per a non-leaf node.
     */
    private int maximumChildrenPerNode = DEFAULT_MAXIMUM_CHILDREN_PER_NODE;
    
    public void setMaximumStreakLength(int maximumStreakLength) {
        this.maximumStreakLength = maximumStreakLength;
    }
    
    public void setMaximumChildrenPerNode(int maximumChildrenPerNode) {
        this.maximumChildrenPerNode = maximumChildrenPerNode;
    }
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> dataRows) {
        RandomParsimoniousContextTreeLearnerV3<C> learner = 
                new RandomParsimoniousContextTreeLearnerV3<>();
        learner.setRandom(new Random());
        learner.setMaximumChildrenPerNode(maximumChildrenPerNode);
        
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
