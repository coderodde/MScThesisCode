package net.coderodde.msc;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
     * Learns a parsimonious context tree from the list of data rows.
     *
     * @param alphabet the alphabet.
     * @param listOfDataRows the list of data rows.
     * @return a parsimonious context tree.
     */
    public abstract ParsimoniousContextTree<C>
            learn(List<DataRow<C>> listOfDataRows);

    /**
     * Checks that the data row list is not empty.
     *
     * @param dataRowList the data row list to check.
     */
    protected void checkDataRowListNotEmpty(List<DataRow<C>> dataRowList) {
        if (dataRowList.isEmpty()) {
            throw new IllegalArgumentException(
                    "There is no data rows in the list.");
        }
    }

    /**
     * Checks that all data rows contain exactly the same number of explanatory
     * variables.
     *
     * @param dataRowList the list of data rows.
     */
    protected void checkDataRowListHasConstantNumberOfExplanatoryVariables(
            List<DataRow<C>> dataRowList) {
        int expectedNumberOfExplanatoryVariables
                = dataRowList.get(0).getNumberOfExplanatoryVariables();

        for (int i = 1; i < dataRowList.size(); ++i) {
            if (dataRowList.get(i).getNumberOfExplanatoryVariables()
                    != expectedNumberOfExplanatoryVariables) {
                throw new IllegalArgumentException(
                        "The data row " + i + " does not have "
                        + expectedNumberOfExplanatoryVariables
                        + " explanatory variables.");
            }
        }
    }

    protected Alphabet<C> getAlphabet(List<DataRow<C>> dataRows) {
        Set<C> filter = new HashSet<>();

        for (DataRow<C> dataRow : dataRows) {
            filter.addAll(dataRow.getAllExplantoryVariables());
            filter.add(dataRow.getResponseVariable());
        }

        return new Alphabet<>((C[]) filter.toArray());
    }
}
