package net.coderodde.msc.support;

import java.util.List;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.Alphabet;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

public class DynamicProgrammingParsimoniousContextTreeLearner<C>
extends AbstractParsimoniousContextTreeLearner<C> {

    @Override
    public ParsimoniousContextTree<C> learn(Alphabet<C> alphabet, List<DataRow<C>> listOfDataRows) {
        return null;
    }
}
