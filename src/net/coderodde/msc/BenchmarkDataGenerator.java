package net.coderodde.msc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class holds all infrastructure for generating benchmark data.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 30, 2018)
 */
public final class BenchmarkDataGenerator {
    
    private static final class DepthData {
        private static final int DATA_ROWS = 1000;
        private static final int ALPHABET_SIZE = 4;
        private static final int PCT_DEPTH = 3;
        private static final int DATA_ROW_LENGTH = 10;
    }
    
    private static final Random RANDOM = new Random();
    
    public static List<DataRow<Character>> generateDepthData() {
        List<DataRow<Character>> dataSet = new ArrayList<>(DepthData.DATA_ROWS);
        DataGeneratingPCT2 dataGeneratingPCT = 
                new DataGeneratingPCT2(DepthData.PCT_DEPTH,
                                       DepthData.ALPHABET_SIZE);
        
        for (int i = 0; i < DepthData.DATA_ROWS; i++) {
            DataRow<Character> dataRow = generateDataRow(dataGeneratingPCT);
            dataSet.add(dataRow);
        }
        
        return dataSet;
    }
    
    /**
     * Generates a data row.
     * 
     * @param dataGeneratingPCT the data-generating PCT used for sampling.
     * @return a data row.
     */
    private static DataRow<Character> 
        generateDataRow(DataGeneratingPCT2 dataGeneratingPCT) {
        String string = getInitialString();
        StringBuilder stringBuilder = 
                new StringBuilder(DepthData.DATA_ROW_LENGTH);
        stringBuilder.append(string);
        
        for (int i = DepthData.PCT_DEPTH; i < DepthData.DATA_ROW_LENGTH; i++) {
            char nextCharacter = dataGeneratingPCT.sampleNext(string);
            stringBuilder.append(nextCharacter);
            string = string.substring(1) + nextCharacter;
        }
        
        char[] dataRowCharacters = stringBuilder.toString().toCharArray();
        Character[] dataRowCharacterObjects = 
                convertCharsToObjects(dataRowCharacters);
        DataRow<Character> dataRow = new DataRow<>(dataRowCharacterObjects);
        return dataRow;
    }
        
    /**
     * Converts the array of primitive characters to the array of boxed 
     * characters.
     * 
     * @param rawCharacters the array of primitive characters to convert.
     * @return the array of boxed characters.
     */
    private static Character[] convertCharsToObjects(char[] rawCharacters) {
        Character[] result = new Character[rawCharacters.length];
        
        for (int i = 0; i < rawCharacters.length; i++) {
            result[i] = rawCharacters[i];
        }
        
        return result;
    }
       
    /**
     * Returns a randomly chosen initial string needed for starting sampling a 
     * data-generating PCT.
     * 
     * @return initial sampling string.
     */
    private static String getInitialString() {
        StringBuilder stringBuilder = 
                new StringBuilder(DepthData.PCT_DEPTH);
        
        for (int i = 0; i < DepthData.PCT_DEPTH; i++) {
            char character = getRandomCharacter();
            stringBuilder.append(character);
        }
        
        return stringBuilder.toString();
    }
    
    /**
     * Returns a randomly and uniformly chosen character from the range 
     * <code>A, B, ...</code> of length <code>DepthData.ALPHABET_SIZE</code>.
     * 
     * @return a randomly and uniformly chosen character.
     */
    private static char getRandomCharacter() {
        return (char)('A' + RANDOM.nextInt(DepthData.ALPHABET_SIZE));
    }
}
