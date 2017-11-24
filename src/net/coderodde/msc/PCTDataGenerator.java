package net.coderodde.msc;

import java.util.List;
import java.util.Random;

public final class PCTDataGenerator {

    private final int order;
    private final DataGeneratingPCT2 pct;
    private final List<Character> characterList;
    private final Random random = new Random();
    
    public PCTDataGenerator(int order, int alphabetSize) {
        this.order = order;
//        this.pct = new DataGeneratingPCT(order, alphabetSize, weights);
        this.pct = new DataGeneratingPCT2(order, alphabetSize);
        this.characterList = this.pct.getAlphabet().getCharacters();
        
//        System.out.println(this.pct);
    }
    
    public String[] generate(int rows, int length) {
        String[] data = new String[rows];
        
        for (int i = 0; i < rows; ++i) {
            data[i] = generateDataRow(length);
        }
        
        return data;
    }
    
    private String generateDataRow(int length) {
        StringBuilder sb = new StringBuilder(length);
        String string = getRandomString();
        sb.append(string);
        
        while (sb.length() < length) {
            char nextChar = pct.sampleNext(string);
            sb.append(nextChar);
            string = string.substring(1) + nextChar;
        }
        
        return sb.toString();
    }
    
    private String getRandomString() {
        StringBuilder sb = new StringBuilder(order);
        
        for (int i = 0; i < order; ++i) {
            sb.append(choose(this.characterList, random));
        }
        
        return sb.toString();
    }
    
    private static <T> T choose(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }
    
    private static final int ORDER = 2;
    private static final int ALPHABET_SIZE = 6;
    
    public static void main(String[] args) {
        System.out.println("--- PCTDataGenerator ---");
        double[] weights = { 2.0, 2.0, 2.0, 2.0, 2.0, 2.0 };
        PCTDataGenerator generator = new PCTDataGenerator(ORDER,
                                                          ALPHABET_SIZE);
        
        String[] data = generator.generate(908, 20);
        
        for (String dataRow : data) {
            System.out.println(dataRow);
        }
    }
}
