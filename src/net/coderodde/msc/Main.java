package net.coderodde.msc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<DataRow<Character>> dataRowList = new ArrayList<>();
        dataRowList.add(new DataRow('A', 'C'));
        new ParsimoniousContextTree<Character>(new DNAAlphabet(),
                                               dataRowList);
    }
}
