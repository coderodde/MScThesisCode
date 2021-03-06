package net.coderodde.msc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DataRow<C> {

    private final List<C> explanatoryVariableList;
    private final C responseVariable;
    
    /**
     * Constructs a data row.
     * 
     * @param variables a string of {@code C}s. The last character will be 
     *                  considered a response variable, whereas all other 
     *                  characters will be considered explanatory characters.
     */
    public DataRow(C... variables) {
        Objects.requireNonNull(variables, "The list of variables is null.");
        checkHasLengthAtLeastTwo(variables);
        
        this.explanatoryVariableList = 
                new ArrayList<>(Arrays.asList(variables));
        // Remove the last char because it is supposed to be the response char.
        this.explanatoryVariableList.remove(
                this.explanatoryVariableList.size() - 1);
        this.responseVariable = variables[variables.length - 1];
    }
    
    public DataRow(C[] variables, int start, int depth) {
        this(Arrays.copyOfRange(variables,
                                start - depth, 
                                start + 1));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        
        for (C c : explanatoryVariableList) {
            sb.append(c);
        }
        
        sb.append(" -> ").append(responseVariable);
        return sb.append("]").toString();
    }
    
    public int getNumberOfExplanatoryVariables() {
        return this.explanatoryVariableList.size();
    }
    
    public C getExplanatoryVariable(int index) {
        return this.explanatoryVariableList.get(
               this.explanatoryVariableList.size() - 1 - index);
    }
    
    public List<C> getAllExplantoryVariables() {
        return Collections.<C>unmodifiableList(explanatoryVariableList);
    }
    
    public C getResponseVariable() {
        return responseVariable;
    }
    
    private void checkHasLengthAtLeastTwo(C[] variables) {
        if (variables.length < 2) {
            throw new IllegalArgumentException(
                    "The data row is too short (" + variables.length + ")");
        }
    }
}
