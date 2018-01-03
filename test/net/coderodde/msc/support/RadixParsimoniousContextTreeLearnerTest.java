package net.coderodde.msc.support;

import java.util.ArrayList;
import java.util.List;
import net.coderodde.msc.DataRow;
import net.coderodde.msc.ParsimoniousContextTree;
import org.junit.Test;

public class RadixParsimoniousContextTreeLearnerTest {
    
//    @Test
    public void test1() {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
        
        for (int i = 1; i < 4; ++i) {
            for (int j = 1; j < 4; ++j) {
                for (int k = 1; k < 4; ++k) {
                    for (int w = 1; w < 4; ++w) {
                        dataRows.add(new DataRow<>(i, j, k, w));
                    }
                }
            }
        }
        
        RadixParsimoniousContextTreeLearner<Integer> learner = 
                new RadixParsimoniousContextTreeLearner<>(Integer::compareTo, 3);
        
        ParsimoniousContextTree<Integer> tree = 
                learner.learn(dataRows);
        
        System.out.println(tree);
    }
    
    @Test
    public void test2() {
        List<DataRow<Integer>> dataRows = new ArrayList<>();
        dataRows.add(new DataRow<>(3, 2, 1));
        dataRows.add(new DataRow<>(2, 1, 1));
        dataRows.add(new DataRow<>(3, 3, 3));
        dataRows.add(new DataRow<>(2, 3, 1));
        dataRows.add(new DataRow<>(2, 1, 1));
        dataRows.add(new DataRow<>(1, 2, 3));
        dataRows.add(new DataRow<>(1, 3, 1));
        dataRows.add(new DataRow<>(3, 1, 2));
        
        RadixParsimoniousContextTreeLearner<Integer> learner = 
                new RadixParsimoniousContextTreeLearner<>(Integer::compareTo,
                                                          3);
        
        ParsimoniousContextTree<Integer> tree = learner.learn(dataRows);
        
        System.out.println(tree);
    }
}
