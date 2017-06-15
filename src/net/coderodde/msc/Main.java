package net.coderodde.msc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<DataRow<Character>> dataRowList = new ArrayList<>();
        DNAAlphabet alphabet = new DNAAlphabet();
        
        dataRowList.add(new DataRow<Character>('A', 'A'));
        dataRowList.add(new DataRow<Character>('T', 'A'));
        dataRowList.add(new DataRow<Character>('A', 'A'));
        dataRowList.add(new DataRow<Character>('C', 'G'));
        dataRowList.add(new DataRow<Character>('C', 'T'));
        dataRowList.add(new DataRow<Character>('C', 'A'));
        dataRowList.add(new DataRow<Character>('T', 'A'));
        dataRowList.add(new DataRow<Character>('T', 'A'));
        dataRowList.add(new DataRow<Character>('T', 'C'));
        dataRowList.add(new DataRow<Character>('C', 'T'));

//        dataRowList.add(new DataRow<Character>('A', 'A'));
//        dataRowList.add(new DataRow<Character>('A', 'T'));
//        dataRowList.add(new DataRow<Character>('A', 'A'));
//        dataRowList.add(new DataRow<Character>('G', 'C'));
//        dataRowList.add(new DataRow<Character>('T', 'C'));
//        dataRowList.add(new DataRow<Character>('A', 'C'));
//        dataRowList.add(new DataRow<Character>('A', 'T'));
//        dataRowList.add(new DataRow<Character>('A', 'T'));
//        dataRowList.add(new DataRow<Character>('C', 'T'));
//        dataRowList.add(new DataRow<Character>('T', 'C'));
//        // ---x
//        dataRowList.add(new DataRow<Character>('C', 'A'));
//        dataRowList.add(new DataRow<Character>('A', 'C'));
//        dataRowList.add(new DataRow<Character>('T', 'G'));
//        dataRowList.add(new DataRow<Character>('C', 'G'));
//        dataRowList.add(new DataRow<Character>('C', 'T'));
//        dataRowList.add(new DataRow<Character>('C', 'A'));
//        dataRowList.add(new DataRow<Character>('T', 'T'));
//        dataRowList.add(new DataRow<Character>('T', 'A'));
//        dataRowList.add(new DataRow<Character>('A', 'C'));
//        dataRowList.add(new DataRow<Character>('C', 'G'));
        
        
//        dataRowList.add(new DataRow('A', 'A', 'C'));
//        dataRowList.add(new DataRow('A', 'C', 'C'));
//        dataRowList.add(new DataRow('A', 'T', 'C'));
//        dataRowList.add(new DataRow('T', 'A', 'C'));
//        dataRowList.add(new DataRow('T', 'T', 'G'));
//        dataRowList.add(new DataRow('G', 'A', 'C'));

//                dataRowList.add(new DataRow<Character>('A', 'A', 'C'));
//                dataRowList.add(new DataRow<Character>('A', 'C', 'C'));
//                dataRowList.add(new DataRow<Character>('A', 'T', 'C'));
//                dataRowList.add(new DataRow<Character>('T', 'A', 'C'));
//                dataRowList.add(new DataRow<Character>('T', 'T', 'G'));
//                dataRowList.add(new DataRow<Character>('G', 'A', 'C'));
//                dataRowList.add(new DataRow<Character>('C', 'C', 'T'));
//                dataRowList.add(new DataRow<Character>('C', 'A', 'T'));
//                dataRowList.add(new DataRow<Character>('C', 'G', 'G'));
//                dataRowList.add(new DataRow<Character>('T', 'C', 'A'));
//                dataRowList.add(new DataRow<Character>('C', 'G', 'T'));
//                dataRowList.add(new DataRow<Character>('C', 'C', 'A'));

        long startTime = System.currentTimeMillis();
        ParsimoniousContextTree<Character> tree =
            new ParsimoniousContextTree<>(alphabet, dataRowList);
        long endTime = System.currentTimeMillis();
        
        System.out.println("Computed in " + (endTime - startTime) + " ms.");
        System.out.println(tree);
    }
}
