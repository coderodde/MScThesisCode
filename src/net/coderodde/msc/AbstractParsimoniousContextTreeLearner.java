package net.coderodde.msc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This abstract class defines the API for parsimonious context tree learners.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jun 15, 2017)
 * @param <C> the character type.
 */
public abstract class AbstractParsimoniousContextTreeLearner<C> {

    /**
     * The alphabet.
     */
    protected final Alphabet<C> alphabet;
    
    /**
     * Used in for assuring that all alphabet characters are present in a label
     * partition.
     */
    protected final Set<C> characterFilterSet = new HashSet<>();
    
    /**
     * Learns a parsimonious context tree from the list of data rows.
     * 
     * @param alphabet the alphabet.
     * @param dataSet the list of data rows.
     * @return a parsimonious context tree.
     */
    public abstract ParsimoniousContextTree<C> learn(Alphabet alphabet, 
                                                     List<DataRow<C>> dataSet);
    
    protected AbstractParsimoniousContextTreeLearner(Alphabet<C> alphabet) {
        this.alphabet = alphabet;
    }
    
    /**
     * Checks that the input data row list contains at least one data row.
     * 
     * @param dataRowList the data row list to check.
     */
    protected static <C> void checkDataRowListNotEmpty(
            List<DataRow<C>> dataRowList) {
        if (dataRowList.isEmpty()) {
            throw new IllegalArgumentException(
                    "The input data row list is empty.");
        }
    }

    /**
     * Checks that all the data rows in the list contain the same number of 
     * explanatory variables.
     * 
     * @param dataRowList the data row list to check.
     */
    protected static <C> void 
        checkDataRowListHasConstantNumberOfExplanatoryVariables(
            List<DataRow<C>> dataRowList) {
        int expectedNumberOfExplanatoryVariables = 
                dataRowList.get(0).getNumberOfExplanatoryVariables();
                
        for (int i = 1; i < dataRowList.size(); ++i) {
            if (dataRowList.get(i).getNumberOfExplanatoryVariables() !=
                    expectedNumberOfExplanatoryVariables) {
                throw new IllegalArgumentException(
                        "The data row " + i + " does not have " + 
                                expectedNumberOfExplanatoryVariables +
                                " explanatory variables.");
            }
        }
    }
    
    /**
     * Computes and return a list of all possible node labels. Each node label
     * is a set of alphabet characters.
     * 
     * @param <C> the character type.
     * @param alphabet the alphabet.
     * @return a list of node labels.
     */
    protected static <C> List<Set<C>> 
        computeListOfAllPossibleLabels(Alphabet alphabet) {
        List<Set<C>> list =
                new ArrayList<>(
                        alphabet.getNumberOfNonemptyCharacterCombinations());
        
        boolean[] combinationFlags = new boolean[alphabet.size()];
        
        while (incrementCombinationFlags(combinationFlags)) {
            list.add(getCharacterCombination(combinationFlags, alphabet));
        }
        
        return list;
    }
    
    /**
     * Increments the combination flags towards the next lexicographical 
     * combination.
     * 
     * @param combinationFlags the array of boolean values each representing 
     *                         whether a character should be included in the 
     *                         label.
     * @return {@code true} if we were able to produce the next combination. 
     *         Otherwise, {@code false} is returned and we know there is no 
     *         unvisited combinations left.
     */
    protected static boolean 
        incrementCombinationFlags(boolean[] combinationFlags) {
        int alphabetSize = combinationFlags.length;
        
        for (int i = 0; i < alphabetSize; ++i) {
            if (combinationFlags[i] == false) {
                combinationFlags[i] = true;
                return true;
            } else {
                combinationFlags[i] = false;
            }
        }
        
        return false;
    }
        
    /**
     * Computes and returns the combination of characters denoted by 
     * {@code combinationFlags}.
     * 
     * @param <C> the character type.
     * @param combinationFlags the array of combination flags.
     * @param alphabet the alphabet.
     * @return a node label as a combination of characters.
     */
    private static <C> Set<C> 
        getCharacterCombination(boolean[] combinationFlags, 
                                Alphabet<C> alphabet) {
        Set<C> characterSet = new HashSet<>();
        
        for (int i = 0; i < combinationFlags.length; ++i) {
            if (combinationFlags[i] == true) {
                characterSet.add(alphabet.get(i));
            }
        }
        
        return characterSet;
    }
        
    protected boolean isPartitionOfAlphabet(
            List<Set<C>> labelCombination) {
        int labels = labelCombination.size();
        
        for (int i = 0; i < labels; ++i) {
            Set<C> label1 = labelCombination.get(i);
            
            for (int j = i + 1; j < labels; ++j) {
                Set<C> label2 = labelCombination.get(j);
                
                if (!Collections.<C>disjoint(label1, label2)) {
                    return false;
                }
            }
        }
        
        this.characterFilterSet.clear();
        
        for (Set<C> label : labelCombination) {
            this.characterFilterSet.addAll(label);
        }
        
        return this.characterFilterSet.size() == this.alphabet.size();
    }
}
