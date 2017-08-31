package net.coderodde.msc.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import net.coderodde.msc.ParsimoniousContextTreeNode;

/**
 * This class implements a simple parsimonious context tree learner that always
 * constructs an <i>independence model</i>, or namely, a minimal tree.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 31, 2017)
 * @param <C> the character type.
 */
public final class IndependenceModelParsimoniousContextTreeLearner<C> 
extends AbstractParsimoniousContextTreeLearner<C> {

    private Alphabet<C> alphabet;
    
    private ParsimoniousContextTreeNode<C> root;
    
    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        Objects.requireNonNull(listOfDataRows, "The data row list is null.");
        checkDataRowListNotEmpty(listOfDataRows);
        checkDataRowListHasConstantNumberOfExplanatoryVariables(listOfDataRows);
        IndependenceModelParsimoniousContextTreeLearner<C> state = 
                new IndependenceModelParsimoniousContextTreeLearner<>();
        
        state.alphabet = getAlphabet(listOfDataRows);
        Set<C> label = new HashSet<>(state.alphabet.getCharacters());
        
        state.root = new ParsimoniousContextTreeNode<>();
        state.root.setLabel(new HashSet<>());
        int depth = listOfDataRows.get(0).getNumberOfExplanatoryVariables();
        ParsimoniousContextTreeNode<C> parentNode = state.root;
        
        for (int d = 0; d < depth; ++d) {
            ParsimoniousContextTreeNode<C> newNode = 
                    new ParsimoniousContextTreeNode<>();
            newNode.setLabel(label);
            parentNode.setChildren(new HashSet<>(Arrays.asList(newNode)));
            parentNode = newNode;
        }
        
        double score = -0.5 * (state.alphabet.size() - 1) *
                               Math.log(listOfDataRows.size());
        
        Map<C, Integer> map = new HashMap<>();
        final int n = listOfDataRows.size();
        
        for (DataRow<C> dataRow : listOfDataRows) {
            C responseVariable = dataRow.getResponseVariable();
            map.put(responseVariable, 
                    map.getOrDefault(responseVariable, 0) + 1);
        }
        
        for (Map.Entry<C, Integer> e : map.entrySet()) {
            score += e.getValue() * Math.log((1.0 * e.getValue()) / n);
        }
        
        ParsimoniousContextTreeNode<C> node = 
                state.root.getChildren().iterator().next();
        
        while (node != null) {
            node.setScore(score);
            
            if (node.getChildren() == null) {
                node = null;
            } else {
                node = node.getChildren().iterator().next();
            }
        }
        
        state.root.setScore(score);
        return new ParsimoniousContextTree<>(state.root);
    }
}
