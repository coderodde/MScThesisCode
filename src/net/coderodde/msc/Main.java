package net.coderodde.msc;

import net.coderodde.msc.support.DNAAlphabet;
import java.util.ArrayList;
import java.util.List;
import net.coderodde.msc.support.BasicParsimoniousContextTreeLearner;

public class Main {

    public static void main(String[] args) {
        List<DataRow<Character>> dataRowList = new ArrayList<>();
        DNAAlphabet alphabet = new DNAAlphabet();
        
//        dataRowList.add(new DataRow<Character>('A', 'A'));
//        dataRowList.add(new DataRow<Character>('T', 'A'));
//        dataRowList.add(new DataRow<Character>('A', 'A'));
//        dataRowList.add(new DataRow<Character>('C', 'G'));
//        dataRowList.add(new DataRow<Character>('C', 'T'));
//        dataRowList.add(new DataRow<Character>('C', 'A'));
//        dataRowList.add(new DataRow<Character>('T', 'A'));
//        dataRowList.add(new DataRow<Character>('T', 'A'));
//        dataRowList.add(new DataRow<Character>('T', 'C'));
//        dataRowList.add(new DataRow<Character>('C', 'T'));
       
        AbstractParsimoniousContextTreeLearner<Character> learner1 =
                new BasicParsimoniousContextTreeLearner<>();

//        dataRowList.add(new DataRow<Character>('A', 'A', 'C'));
//        dataRowList.add(new DataRow<Character>('A', 'C', 'C'));
//        dataRowList.add(new DataRow<Character>('A', 'T', 'C'));
//        dataRowList.add(new DataRow<Character>('T', 'A', 'C'));
//        dataRowList.add(new DataRow<Character>('T', 'T', 'G'));
//        dataRowList.add(new DataRow<Character>('G', 'A', 'C'));
//        dataRowList.add(new DataRow<Character>('C', 'C', 'T'));
//        dataRowList.add(new DataRow<Character>('C', 'A', 'T'));
//        dataRowList.add(new DataRow<Character>('C', 'G', 'G'));
//        dataRowList.add(new DataRow<Character>('T', 'C', 'A'));
//        dataRowList.add(new DataRow<Character>('C', 'G', 'T'));
//        dataRowList.add(new DataRow<Character>('C', 'C', 'A'));

//        dataRowList.add(new DataRow<Character>('C', 'T', 'T', 'A'));
//        dataRowList.add(new DataRow<Character>('G', 'T', 'C', 'C'));
//        dataRowList.add(new DataRow<Character>('A', 'T', 'C', 'T'));
//        dataRowList.add(new DataRow<Character>('G', 'G', 'A', 'T'));
//        dataRowList.add(new DataRow<Character>('T', 'T', 'C', 'T'));
//        dataRowList.add(new DataRow<Character>('A', 'A', 'G', 'A'));

        // test_file_3b.txt
//        dataRowList.add(new DataRow<Character>('C', 'T', 'T', 'A'));
//        dataRowList.add(new DataRow<Character>('G', 'T', 'C', 'C'));
//        dataRowList.add(new DataRow<Character>('G', 'T', 'C', 'T'));
//        dataRowList.add(new DataRow<Character>('G', 'G', 'C', 'T'));
//        dataRowList.add(new DataRow<Character>('T', 'T', 'C', 'T'));
//        dataRowList.add(new DataRow<Character>('A', 'A', 'G', 'A'));

        // test_file_3c.txt
//        dataRowList.add(new DataRow<Character>('A', 'C', 'C', 'A'));
//        dataRowList.add(new DataRow<Character>('T', 'A', 'A', 'G'));
//        dataRowList.add(new DataRow<Character>('G', 'G', 'T', 'T'));
//        dataRowList.add(new DataRow<Character>('T', 'C', 'G', 'T'));
//        dataRowList.add(new DataRow<Character>('A', 'T', 'G', 'C'));

        // test_file_4.txt
        dataRowList.add(new DataRow<Character>('A', 'C', 'T'));
        dataRowList.add(new DataRow<Character>('T', 'A', 'A'));
        dataRowList.add(new DataRow<Character>('G', 'C', 'C'));
        dataRowList.add(new DataRow<Character>('T', 'C', 'G'));
        dataRowList.add(new DataRow<Character>('C', 'C', 'T'));
        dataRowList.add(new DataRow<Character>('C', 'A', 'A'));
        
        // test_file_4b.txt
//        dataRowList.add(new DataRow<Character>('A', 'C', 'C', 'G', 'T'));
//        dataRowList.add(new DataRow<Character>('T', 'T', 'C', 'G', 'A'));
//        dataRowList.add(new DataRow<Character>('G', 'C', 'C', 'A', 'C'));
//        dataRowList.add(new DataRow<Character>('T', 'T', 'C', 'G', 'G'));
//        dataRowList.add(new DataRow<Character>('C', 'A', 'C', 'C', 'T'));
//        dataRowList.add(new DataRow<Character>('C', 'G', 'A', 'T', 'A'));

        long startTime = System.currentTimeMillis();
        ParsimoniousContextTree<Character> tree =
                learner1.learn(alphabet, dataRowList);
        long endTime = System.currentTimeMillis();
        
        System.out.println("Computed in " + (endTime - startTime) + " ms.");
        System.out.println(tree);
    }
}
