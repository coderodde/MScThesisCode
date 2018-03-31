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
        private static final double[] WEIGHTS = { 1.5, 2.0, 2.0, 1.0 };
        private static final double BETA = 0.9132432;
    }
    
    private static final class AlphabetSizeData {
        private static final int DATA_ROWS = 1000;
        private static final int MINIMUM_ALPHABET_SIZE = 2;
        private static final int MAXIMUM_ALPHABET_SIZE = 6;
        private static final int PCT_DEPTH = 3;
    }
    
    private static final class DataSetSizeData {
        private static final int STARTING_NUMBER_OF_DATA_ROWS = 1000;
        private static final int DATA_ROWS_INCREMENT = 1000;
        private static final int DATA_SETS = 10;
        private static final int PCT_DEPTH = 3;
        private static final int ALPHABET_SIZE = 4;
    }
    
    private static final Random RANDOM = new Random();
    
    public static List<DataRow<Character>> generateDepthData() {
        List<DataRow<Character>> dataSet = new ArrayList<>(DepthData.DATA_ROWS);
        DataGeneratingPCT2 dataGeneratingPCT = 
                new DataGeneratingPCT2(DepthData.PCT_DEPTH,
                                       DepthData.ALPHABET_SIZE);
        
        for (int i = 0; i < DepthData.DATA_ROWS; i++) {
            DataRow<Character> dataRow = 
                    generateDataRow(dataGeneratingPCT,
                                    DepthData.ALPHABET_SIZE,
                                    DepthData.PCT_DEPTH);
            dataSet.add(dataRow);
        }
        
        return dataSet;
    }
    
    public static List<List<DataRow<Character>>> generateAlphabetSizeData() {
        List<List<DataRow<Character>>> dataSets = new ArrayList<>();
        
        for (int alphabetSize = AlphabetSizeData.MINIMUM_ALPHABET_SIZE; 
                alphabetSize <= AlphabetSizeData.MAXIMUM_ALPHABET_SIZE;
                alphabetSize++) {
            List<DataRow<Character>> dataSet = 
                    new ArrayList<>(AlphabetSizeData.DATA_ROWS);
            
            DataGeneratingPCT2 dataGeneratingPCT = 
                    new DataGeneratingPCT2(AlphabetSizeData.PCT_DEPTH,
                                           alphabetSize);
            
            for (int i = 0; i < AlphabetSizeData.DATA_ROWS; i++) {
                DataRow<Character> dataRow = 
                        generateDataRow(dataGeneratingPCT,
                                        alphabetSize,
                                        AlphabetSizeData.PCT_DEPTH);
                dataSet.add(dataRow);
            }
            
            dataSets.add(dataSet);
        }
        
        return dataSets;
    }
    
    public static List<List<DataRow<Character>>> generateDataSetSizeData() {
        List<List<DataRow<Character>>> dataSets = new ArrayList<>();
        
        for (int dataSetSize = DataSetSizeData.STARTING_NUMBER_OF_DATA_ROWS,
                 dataSetIndex = 0;
                dataSetIndex < DataSetSizeData.DATA_SETS;
                dataSetIndex++,
                dataSetSize += DataSetSizeData.DATA_ROWS_INCREMENT) {
            List<DataRow<Character>> dataSet = new ArrayList<>(dataSetSize);
            
            DataGeneratingPCT2 dataGeneratingPCT = 
                    new DataGeneratingPCT2(DataSetSizeData.PCT_DEPTH,
                                           DataSetSizeData.ALPHABET_SIZE);
            
            for (int i = 0; i < dataSetSize; i++) {
                DataRow<Character> dataRow= 
                        generateDataRow(dataGeneratingPCT,
                                        DataSetSizeData.ALPHABET_SIZE,
                                        DataSetSizeData.PCT_DEPTH);
                dataSet.add(dataRow);
            }
            
            dataSets.add(dataSet);
        }
        
        return dataSets;
    }
    
    /**
     * Generates a data row.
     * 
     * @param dataGeneratingPCT the data-generating PCT used for sampling.
     * @return a data row.
     */
    private static DataRow<Character> 
        generateDataRow(DataGeneratingPCT2 dataGeneratingPCT,
                        int alphabetSize,
                        int pctDepth) {
        String string = getInitialString(alphabetSize, pctDepth);
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
    private static String getInitialString(int alphabetSize,
                                           int pctDepth) {
        StringBuilder stringBuilder = new StringBuilder(pctDepth);
        
        for (int i = 0; i < pctDepth; i++) {
            char character = getRandomCharacter(alphabetSize);
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
    private static char getRandomCharacter(int alphabetSize) {
        return (char)('a' + RANDOM.nextInt(alphabetSize));
    }
}
