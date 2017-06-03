package net.coderodde.msc;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<DataRow<Character>> dataRowList = new ArrayList<>();
        Alphabet<Character> smallAlphabet = new Alphabet<>('0', '1');
        dataRowList.add(new DataRow('0', '1', '1'));
        ParsimoniousContextTree<Character> tree =
            new ParsimoniousContextTree<Character>(smallAlphabet,
                                                   dataRowList);
        System.out.println("yo");
    }
}
