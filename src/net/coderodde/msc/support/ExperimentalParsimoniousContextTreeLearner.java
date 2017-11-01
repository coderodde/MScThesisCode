package net.coderodde.msc.support;

import java.util.List;
import net.coderodde.msc.AbstractParsimoniousContextTreeLearner;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;

public final class ExperimentalParsimoniousContextTreeLearner<C> 
extends AbstractParsimoniousContextTreeLearner<C> {

    @Override
    public ParsimoniousContextTree<C> learn(List<DataRow<C>> listOfDataRows) {
        return null;
    }
}
