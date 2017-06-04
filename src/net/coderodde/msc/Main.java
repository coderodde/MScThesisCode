package net.coderodde.msc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<DataRow<Character>> dataRowList = new ArrayList<>();
        DNAAlphabet alphabet = new DNAAlphabet();
        
        dataRowList.add(new DataRow('A', 'A'));
        dataRowList.add(new DataRow('T', 'A'));
        dataRowList.add(new DataRow('A', 'A'));
        dataRowList.add(new DataRow('C', 'G'));
        dataRowList.add(new DataRow('C', 'T'));
        dataRowList.add(new DataRow('C', 'A'));
        dataRowList.add(new DataRow('T', 'A'));
        dataRowList.add(new DataRow('T', 'A'));
        dataRowList.add(new DataRow('T', 'C'));
        dataRowList.add(new DataRow('C', 'T'));
        
        ParsimoniousContextTree<Character> tree =
            new ParsimoniousContextTree<Character>(alphabet,
                                                   dataRowList);
    }
}
