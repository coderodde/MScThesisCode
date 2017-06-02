package net.coderodde.msc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements a probability distribution of characters.
 * 
 * @author Rodion Efremov
 * @version 1.6 (Jun 1, 2017)
 * @param <C> the character type.
 */
public final class ResponseVariableDistribution<C> {

    private static final double EPSILON = 1E-5;
    
    /**
     * Maps each character {@code c} to its probability.
     */
    private final Map<C, Double> distributionMap = new HashMap<>();
    
    /**
     * Holds the sum of probabilities stored so far in this data structure.
     */
    private double probabilitySum;
    
    /**
     * Associates {@code character} with {@code probability}.
     * 
     * @param character   the character.
     * @param probability the probability of the character.
     */
    public void putResponseVariableProbability(C character, 
                                               double probability) {
        Objects.requireNonNull(character, "The input character is null.");
        validateProbability(probability);
        this.distributionMap.put(character, probability);
    }
    
    /**
     * Retrieves the probability of a given character.
     * 
     * @param character the character whose probability to return.
     * @return the requested probability.
     */
    public double getResponseVariableProbability(C character) {
        Objects.requireNonNull(character, "The input character is null.");
        checkProbabilitySumEqualsOne();
        return distributionMap.get(character);
    }
    
    private void validateProbability(double probability) {
        if (Double.isNaN(probability)) {
            throw new IllegalArgumentException("The input probability is NaN.");
        }
        
        if (probability < 0.0) {
            throw new IllegalArgumentException("The input probability < 0.0");
        }
        
        if (probability > 1.0) {
            throw new IllegalArgumentException("The input probability > 1.0");
        }
        
        probabilitySum += probability;
        
        if (probabilitySum > 1.0 + EPSILON) {
            throw new IllegalArgumentException(
                    "The input probability is fine, but makes the sum of " +
                    "probabilities too large: " + probabilitySum);
        }
    }

    private void checkProbabilitySumEqualsOne() {
        if (Math.abs(probabilitySum - 1.0) < EPSILON) {
            throw new IllegalStateException(
                    "The sum of probabilities is not sufficiently close to " +
                    "1.0");
        }
    }
}
