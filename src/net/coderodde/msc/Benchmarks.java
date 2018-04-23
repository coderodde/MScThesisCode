package net.coderodde.msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import net.coderodde.msc.support.BasicParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IndependenceModelParsimoniousContextTreeLearner;

/**
 * This class contains all the code for performing benchmarks.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Mar 31, 2018)
 */
public final class Benchmarks {
    
    /**
     * If this is set to {@code true}, the benchmarks will not print the PCTs.
     */
    private static boolean dataGenerationMode = true;
    
    /**
     * This static method runs all benchmark for the input learner. The input
     * learner is compared to the optimal baseline learner and the independence
     * model learner.
     * 
     * @param learners the list of PCT learner to benchmark.
     */
    public static void benchmark(
            List<AbstractParsimoniousContextTreeLearner<Character>> learners) {
        warmup();
        benchmarkDepth(learners);
        //benchmarkAlphabetSize(learner);
        //benchmarkDataSetSize(learner);
    }
    
    /**
     * Warms up all the algorithms.
     */
    private static void warmup() {
        System.out.println("*** WARMING UP ***");
        
        
        
        System.out.println("*** WARMED UP! ***");
        System.out.println();
    }
    
    private static String getLearnersNames(
            List<AbstractParsimoniousContextTreeLearner<Character>> learners) {
        StringBuilder stringBuilder = new StringBuilder();
        String separator = "";
        
        for (AbstractParsimoniousContextTreeLearner<Character> learner 
                : learners) {
            stringBuilder.append(separator);
            stringBuilder.append(learner.getClass().getSimpleName());
            separator = ", ";
        }
        
        return stringBuilder.toString();
    }
    
    /**
     * This static method runs the benchmark where the PCT depth grows larger.
     * The input learner is compared against the optimal baseline learner and 
     * the independence model learner.
     * 
     * @param learner the PCT learner to benchmark.
     */
    private static void benchmarkDepth(
            List<AbstractParsimoniousContextTreeLearner<Character>> learners) {
        System.out.println(
                "*** BENCHMARKING: " + getLearnersNames(learners) + 
                " FOR DEPTH ***");
        
        List<String> benchmarkDataFileNames = getDepthDataFileNameList();
        
        for (String benchmarkDataFileName : benchmarkDataFileNames) {
            System.out.println("DEPTH DATA FILE: " + benchmarkDataFileName);
            
            File benchmarkDataFile = new File(benchmarkDataFileName);
            List<String> rawDataRows = loadRawDataRows(benchmarkDataFile);
            
            for (int depth = DepthBenchmarkConfiguration.MINIMUM_DEPTH;
                    depth <= DepthBenchmarkConfiguration.MAXIMUM_DEPTH;
                    depth++) {
                System.out.println("--- Depth = " + depth + " ---");
                DepthBenchmarkConfiguration.benchmarkDepthImpl(rawDataRows,
                                                               depth, 
                                                               learners);
            }
        }
        
        // The benchmarks for 'learner' begin:
        /*
        List<String> rawDataRows = 
                DepthBenchmarkConfiguration.readRawDataRows();
        
        for (int depth = DepthBenchmarkConfiguration.MINIMUM_DEPTH;
                depth <= DepthBenchmarkConfiguration.MAXIMUM_DEPTH;
                depth++) {
            System.out.println("--- Depth = " + depth + " ---");
            DepthBenchmarkConfiguration.benchmarkDepthImpl(rawDataRows, 
                                                           depth, 
                                                           learner);
        }*/
    }
    
    private static List<String> loadRawDataRows(File file) {
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException(
                "The file \"" + file.getAbsolutePath() + "\" does not " +
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
         * The path to the directory that contains all the depth data files.
         */
        // Lenovo:
        //private static final String DEPTH_DATA_FILE_DIRECTORY = 
        //        "C:\\Users\\rodde\\Documents\\ProGradu\\BenchmarkData";
        // Desktop:
        private static final String DEPTH_DATA_FILE_DIRECTORY = 
                "C:\\Users\\Rodion Efremov\\Documents\\ProGradu\\BenchmarkData";
        
        /**
         * The full path to the data file used for PCT depth benchmarks.
         */
        private static final String DEPTH_DATA_FILE_PATH = 
                "C:\\Users\\rodde\\Documents\\ProGradu\\BenchmarkData\\" +
                "DepthData1.txt";
        
        /**
         * The minimum depth of the benchmarked PCT learner.
         */
        private static final int MINIMUM_DEPTH = 1;
        
        /**
         * The maximum depth of the benchmarked PCT learner.
         */
        private static final int MAXIMUM_DEPTH = 6;
        
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
                List<AbstractParsimoniousContextTreeLearner<Character>> 
                        learners) {
            List<DataRow<Character>> dataSet = 
                    extractDataSet(rawDataRows, depth);
            
            AbstractParsimoniousContextTreeLearner<Character> optimalLearner =
                    new BasicParsimoniousContextTreeLearner<>();
            
            AbstractParsimoniousContextTreeLearner<Character>
                    independenceModelLearner =
                    new IndependenceModelParsimoniousContextTreeLearner<>();

            // First, learn an optimal PCT:
            long startTime = System.currentTimeMillis();
            
            ParsimoniousContextTree<Character> optimalPCT = 
                    optimalLearner.learn(dataSet);
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("Optimal PCT learner in " + 
                               (endTime - startTime) + " milliseconds.");
            System.out.println("Optimal PCT score: " + optimalPCT.getScore());
            
            // Second, learn an independence model:
            startTime = System.currentTimeMillis();
            
            ParsimoniousContextTree<Character> independenceModel =
                    independenceModelLearner.learn(dataSet);
            
            endTime = System.currentTimeMillis();
            
            System.out.println("Independence model learner in " +
                               (endTime - startTime) + " milliseconds.");
            System.out.println(
                    "Independence model score: " + 
                            independenceModel.getScore());
            
            // Benchmark all the interesting learners:
            for (AbstractParsimoniousContextTreeLearner<Character> learner
                    : learners) {
                startTime = System.currentTimeMillis();
                ParsimoniousContextTree<Character> learnerPCT =
                        learner.learn(dataSet);
                endTime = System.currentTimeMillis();
                
                System.out.println(learner.getClass().getSimpleName() + 
                                   " in " + (endTime - startTime) +
                                   " millisecons.");
                
                double plausibility = 
                        computePlausibility(optimalPCT.getScore(),
                                            independenceModel.getScore(),
                                            learnerPCT.getScore());
                
                System.out.println("Score: " + learnerPCT.getScore() + 
                                   ", plausibility: " + plausibility);
                
                if (dataGenerationMode == false) {
                    System.out.println("OPTIMAL");
                    System.out.println(optimalPCT);
                    System.out.println("HEURISTIC");
                    System.out.println(learnerPCT);
                }
                
                System.out.println("------");
            }
        }
        
        /**
         * Computes the plausibility score for the {@code targetPCTScore}.
         * 
         * @param optimalScore           the score of the optimal PCT.
         * @param independenceModelScore the score of the independence model.
         * @param targetPCTScore         the target score for which to compute
         *                               plausibility score.
         * @return                       the plausibility score.
         */
        private static double computePlausibility(
                double optimalScore,
                double independenceModelScore,
                double targetPCTScore) {
            if (optimalScore == independenceModelScore) {
                return targetPCTScore == optimalScore ? 
                        1.0 : 
                        Double.NEGATIVE_INFINITY;
            } else {
                double numerator = targetPCTScore - independenceModelScore;
                double denominator = optimalScore - independenceModelScore;
                return numerator / denominator;
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
    
    /**
     * This static method returns the list of all file names containing data for
     * depth benchmarks.
     * 
     * @return the list of file names.
     */
    private static List<String> getDepthDataFileNameList() {
        List<String> fileNameList = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            fileNameList.add(
                    DepthBenchmarkConfiguration.DEPTH_DATA_FILE_DIRECTORY + 
                            "\\DepthData" + i + ".txt");
        }
        
        return fileNameList;
    }
}
