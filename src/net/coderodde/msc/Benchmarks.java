package net.coderodde.msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class contains all the code for performing benchmarks.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 31, 2018)
 */
public final class Benchmarks {
    
    /**
     * This static method runs all benchmark for the input learner. The input
     * learner is compared to the optimal baseline learner and the independence
     * model learner.
     * 
     * @param learner the PCT learner to benchmark.
     */
    public static void benchmark(
            AbstractParsimoniousContextTreeLearner<Character> learner) {
        warmup();
        benchmarkDepth(learner);
        benchmarkAlphabetSize(learner);
        benchmarkDataSetSize(learner);
    }
    
    /**
     * Warms up all the algorithms.
     */
    private static void warmup() {
        System.out.println("*** WARMING UP ***");
        
        
        
        System.out.println("*** WARMED UP! ***");
        System.out.println();
    }
    
    /**
     * This static method runs the benchmark where the PCT depth grows larger.
     * The input learner is compared against the optimal baseline learner and 
     * the independence model learner.
     * 
     * @param learner the PCT learner to benchmark.
     */
    private static void benchmarkDepth(
            AbstractParsimoniousContextTreeLearner<Character> learner) {
        // The benchmarks for 'learner' begin:
        System.out.println(
                "*** BENCHMARKING: " + learner.getClass().getSimpleName() +
                " ***");
        
        List<String> rawDataRows = 
                DepthBenchmarkConfiguration.readRawDataRows();
        
        for (int depth = DepthBenchmarkConfiguration.MINIMUM_DEPTH;
                depth <= DepthBenchmarkConfiguration.MAXIMUM_DEPTH;
                depth++) {
            DepthBenchmarkConfiguration.benchmarkDepthImpl(rawDataRows, 
                                                           depth, 
                                                           learner);
        }
    }
    
    /**
     * This static method runs the benchmark where the alphabet size grows 
     * larger. The input learner is compared against the optimal baseline 
     * learner and the independence model learner.
     * 
     * @param learner the PCT learner to benchmark.
     */
    private static void benchmarkAlphabetSize(
            AbstractParsimoniousContextTreeLearner<Character> learner) {
        
    }
    
    /**
     * This static method runs the benchmark where the data set size grows 
     * larger. The input learner is compared against the optimal baseline
     * learner and the independence model learner.
     * 
     * @param learner the PCT learner to benchmark.
     */
    private static void benchmarkDataSetSize(
            AbstractParsimoniousContextTreeLearner<Character> learner) {
        
    }
    
    /**
     * This class groups all the constants for the depth benchmark.
     */
    private static final class DepthBenchmarkConfiguration {
        /**
         * The full path to the data file used for PCT depth benchmarks.
         */
        private static final String DEPTH_DATA_FILE_PATH = 
                "C:\\Users\\rodde\\Documents\\ProGradu\\BenchmarkData\\" +
                "DepthData.txt";
        
        /**
         * The minimum depth of the benchmarked PCT learner.
         */
        private static final int MINIMUM_DEPTH = 1;
        
        /**
         * The maximum depth of the benchmarked PCT learner.
         */
        private static final int MAXIMUM_DEPTH = 4;
        
        /**
         * Reads the data file and returns its content as the list of raw data
         * rows.
         * 
         * @return the list of raw data rows.
         */
        private static List<String> readRawDataRows() {
            File file = new File(DEPTH_DATA_FILE_PATH);
            
            if (!file.exists() || !file.isFile()) {
                throw new RuntimeException(
                        "The file \"" + DEPTH_DATA_FILE_PATH + "\" does not " +
                        "exist or is not a regular file.");
            }
            
            List<String> rawDataRows = new ArrayList<>();
            
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    rawDataRows.add(scanner.nextLine());
                }
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            
            return rawDataRows;
        }
        
        /**
         * Implements the actual depth benchmark for the input learner using a 
         * particular depth.
         * 
         * @param depth   the target PCT depth.
         * @param learner the target learner to benchmark.
         */
        private static void benchmarkDepthImpl(
                List<String> rawDataRows,
                int depth, 
                AbstractParsimoniousContextTreeLearner<Character> learner) {
            List<DataRow<Character>> dataSet = 
                    extractDataSet(rawDataRows, depth);
            
            int rowNumber = 1;
            
            for (DataRow<Character> dataRow : dataSet) {
                System.out.printf("%4d: %s\n", rowNumber++, dataRow);
            }
        }
        
        /**
         * Extracts the actual data set for depth {@code depth} from the input
         * raw data rows.
         * 
         * @param rawDataRows the raw data rows.
         * @param depth       the target depth.
         * @return            the list of actual data rows.
         */
        private static List<DataRow<Character>> 
        extractDataSet(List<String> rawDataRows, int depth) {
            List<DataRow<Character>> dataRows = 
                    new ArrayList<>(rawDataRows.size());
            
            for (String rawDataRow : rawDataRows) {
                Character[] rawDataRowCharacters = 
                        convertToRawDataRowCharacters(rawDataRow.toCharArray());
                Character[] selectedRawDataRowCharacters = 
                        selectRawDataRowCharacters(rawDataRowCharacters,
                                                   depth);
                dataRows.add(new DataRow<>(selectedRawDataRowCharacters));
            }
            
            return dataRows;
        }
        
        /**
         * Selects a relevant part of the character array.
         * 
         * @param rawDataRowCharacters the array of characters.
         * @param depth                the target depth of a PCT.
         * @return                     selected characters.
         */
        private static Character[] 
        selectRawDataRowCharacters(Character[] rawDataRowCharacters,
                                   int depth) {
            Character[] selectedRawDataRowCharacters = new Character[depth + 1];
            System.arraycopy(rawDataRowCharacters,
                             0,
                             selectedRawDataRowCharacters,
                             0, 
                             depth);
            
            selectedRawDataRowCharacters[depth] = 
                    rawDataRowCharacters[rawDataRowCharacters.length - 1];
            return selectedRawDataRowCharacters;
        }
        
        /**
         * Converts a primitive character array to a boxed character array.
         * 
         * @param chars the array of primitive characters to convert.
         * @return the boxed version of the input primitive character array.
         */
        private static Character[] convertToRawDataRowCharacters(char[] chars) {
            Character[] result = new Character[chars.length];
            
            for (int i = 0; i < chars.length; i++) {
                result[i] = chars[i];
            }
            
            return result;
        }
    }
}
