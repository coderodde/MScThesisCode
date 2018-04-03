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
            benchmarkDepthImpl(depth, learner);
        }
    }
    
    /**
     * Implements the actual depth benchmark for the input learner using a 
     * particular depth.
     * 
     * @param depth   the target PCT depth.
     * @param learner the target learner to benchmark.
     */
    private static void benchmarkDepthImpl(
            int depth, 
            AbstractParsimoniousContextTreeLearner<Character> learner) {
        
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
    }
}
