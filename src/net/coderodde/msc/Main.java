package net.coderodde.msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import net.coderodde.msc.support.BasicParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IndependenceModelParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IterativeRadixParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner2;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner3;
import net.coderodde.msc.support.RadixParsimoniousContextTreeLearner;
import net.coderodde.msc.support.RadixRandomParsimoniousContextTreeLearner;
import net.coderodde.msc.support.RandomParsimoniousContextTreeLearner;
import net.coderodde.msc.support.RandomParsimoniousContextTreeLearner2;
import net.coderodde.msc.support.RandomParsimoniousContextTreeLearner3;

public class Main {

    // Commands for generating MC-data:
    // MC: datagen-mc /Users/rodionefremov/Desktop/WarAndPeace.txt /Users/rodionefremov/Desktop/ProGradu/alphabetReduction6 3 908 19
    // Running as Ralf's learner: /Users/rodionefremov/Desktop/ProGradu/CTCF.txt 18 3  
    // PCT: datagen-pct /Users/rodionefremov/Desktop/ProGradu/weightsAlphabet5 2 908 19
    // PCT2: datagen-pct2 <alphabetSize> <order> <lines> <lineLength>
    // PCT3: datagen-pct3 <alphabetSize> <order> <lines> <lineLength> <beta>
    // Example: datagen-pct2 26 2 908 19
    public static void main(String[] args) {
        if (args.length == 0) {
            benchmark();
            return;
        }
        
        if (args.length == 2 && args[0].equals("check")) {
            File file = new File(args[1]);
            
            try (Scanner scanner = new Scanner(file, "UTF-8")) {
                while (scanner.hasNextLine()) {
                    System.out.println(scanner.nextLine().length());
                }
            } catch (FileNotFoundException ex) {
                
            }
            
            return;
        }
        
        if (args.length == 6) {
            if (args[0].equals("datagen-mc")) {
                generateDataViaMC(args[1], // text file name
                                  args[2], // alphabet file name
                                  Integer.parseInt(args[3]),  // order
                                  Integer.parseInt(args[4]),  // lines
                                  Integer.parseInt(args[5])); // line length
                return;
            } else if (args[0].equals("datagen-pct3")) {
                generateDataViaPCT3(Integer.parseInt(args[1]), // alphabet size,
                                    Integer.parseInt(args[2]), // depth/order,
                                    Integer.parseInt(args[3]), // lines,
                                    Integer.parseInt(args[4]), // line length,
                                    Double.parseDouble(args[5])); // beta.
                return;
            }
        } else if (args.length == 5) {
            if (args[0].equals("datagen-mc")) {
                generateDataViaMC(args[1], // text file name
                                  Integer.parseInt(args[2]),  // order
                                  Integer.parseInt(args[3]),  // lines
                                  Integer.parseInt(args[4])); // line length
                return;
            } else if (args[0].equals("datagen-pct")) {
                System.out.println("This shit is old.");
                System.exit(0);
//                int order = Integer.parseInt(args[2]);
//                int lines = Integer.parseInt(args[3]);
//                int lineLength = Integer.parseInt(args[4]);
//                
//                System.out.println(
//                        "=== Generating data via a PCT: order = " + order +
//                        ", lines = " + lines + ", lineLength = " + lineLength);
//                
//                generateDataViaPCT(args[1], // weight file,
//                                   order,
//                                   lines,
//                                   lineLength);
//                return;
            } else if (args[0].equals("datagen-pct2")) {
                int alphabetSize = Integer.parseInt(args[1]);
                int order        = Integer.parseInt(args[2]);
                int lines        = Integer.parseInt(args[3]);
                int lineLength   = Integer.parseInt(args[4]);
//                
//                System.out.println(
//                        "=== Generating data via a PCT2: " +
//                        "alphabet size = " + alphabetSize + ", " +
//                        "order = " + order + ", " +
//                        "lines = " + lines + ", " +
//                        "lineLength = " + lineLength);
                
                generateDataViaPCT2(alphabetSize,
                                    order,
                                    lines,
                                    lineLength);
                return;
            }
        } 
        
        long startTime;
        long endTime;
        
        if (args.length != 3) {
            printHelpMessage(System.out);
            return;
        }
        
        int depth = Integer.MIN_VALUE;
        
        try {
            depth = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            System.err.println(args[2] + ": not an integer.");
            System.exit(1);
        }
        
        int start = Integer.MIN_VALUE;
        
        try {
            start = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            System.err.println(args[1] + ": not an integer.");
            System.exit(1);
        }
        
        File file = new File(args[0]);
        checkFile(file);
        List<DataRow<Character>> dataRows = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(file, "ISO-8859-1")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim().toLowerCase();
                Character[] arr = new Character[line.length()];
                
                int i = 0;

                for (char c : line.toCharArray()) {
                    arr[i++] = c;
                }
                
                dataRows.add(new DataRow<Character>(arr, start, depth));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            System.err.println("File not found!");
            System.exit(0);
        }
        
        for (DataRow<Character> dataRow : dataRows) {
            System.out.println(dataRow);
        }
        
        RadixRandomParsimoniousContextTreeLearner<Character> testLearner = 
                new RadixRandomParsimoniousContextTreeLearner<>(Character::compareTo, 10);
        
        long ta = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> testTree = testLearner.learn(dataRows);
        
        long tb = System.currentTimeMillis();
        
        System.out.println("Time: " + (tb - ta) + " milliseconds.");
        System.out.println(testTree);
        
        System.exit(0);
        
        AbstractParsimoniousContextTreeLearner<Character> learner = 
                new BasicParsimoniousContextTreeLearner<>();
        
        RandomParsimoniousContextTreeLearner<Character> learner2 = 
                new RandomParsimoniousContextTreeLearner<>();
        
        RandomParsimoniousContextTreeLearner2<Character> learner2b =
                new RandomParsimoniousContextTreeLearner2<>(0.8);
        
        RandomParsimoniousContextTreeLearner3<Character> learner2c = 
                new RandomParsimoniousContextTreeLearner3<>();
        
        learner2.setRandom(new Random());
        learner2b.setRandom(new Random());
        learner2c.setRandom(new Random());
        
        AbstractParsimoniousContextTreeLearner<Character> learner3 = 
                new IndependenceModelParsimoniousContextTreeLearner<>();
        
        IterativeRandomParsimoniousContextTreeLearner<Character> learner4 =
                new IterativeRandomParsimoniousContextTreeLearner<>();
        
        IterativeRandomParsimoniousContextTreeLearner2<Character> learner4b =
                new IterativeRandomParsimoniousContextTreeLearner2<>();
        
        IterativeRandomParsimoniousContextTreeLearner3<Character> learner4c = 
                new IterativeRandomParsimoniousContextTreeLearner3<>();
        
        learner4b.setBeta(0.8);
        
        learner2.setRandom(new Random());
        learner4.setIterations(1000);
        learner4.setRandom(new Random());
        learner4b.setIterations(1000);
        learner4c.setIterations(1000);
        
        //// Optimal learner:
        System.out.println("BasicParsimoniousContextTreeLearner:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree = 
                learner.learn(dataRows);
    
        endTime = System.currentTimeMillis();
        
        System.out.println(tree);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
       
        double optimalScore = tree.getScore();
        System.out.println("Optimal score: " + optimalScore);
        
        //// Independence model learner:
        System.out.println();
        System.out.println("IndependenceModelParsimoniousContextTreeLearner:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree3 = learner3.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree3);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double independenceModelScore = tree3.getScore();
        
        //// Random PCT learner:
        System.out.println();
        System.out.println("RandomParsimoniousContextTreeLearner:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree2 = learner2.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree2);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        System.out.println("Score: " + tree2.getScore() + ", plausibility: "
            + getPlausibilityScore(optimalScore,
                                   independenceModelScore,
                                   tree2.getScore()));
        
        //// 2nd random PCT learner:
        System.out.println();
        System.out.println("RandomParsimoniousContextTreeLearner2:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> treeRandom2 = learner2b.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(treeRandom2);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        System.out.println("Score: " + treeRandom2.getScore() + ", plausibility: "
            + getPlausibilityScore(optimalScore,
                                   independenceModelScore,
                                   treeRandom2.getScore()));
        
        //// 3rd random PCT learner:
        System.out.println();
        System.out.println("RandomParsimoniousContextTreeLearner3:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> treeRandom3 = learner2c.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(treeRandom3);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        System.out.println("Score: " + treeRandom3.getScore() + ", plausibility: " 
            + getPlausibilityScore(optimalScore, 
                                   independenceModelScore, 
                                   treeRandom3.getScore()));
        
        //// Iterative random PCT learner:
        System.out.println();
        System.out.println("IterativeRandomParsimoniousContextTreeLearner:");
        
        startTime = System.currentTimeMillis();
        learner4.setIterations(1000);
        ParsimoniousContextTree<Character> tree4 = learner4.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree4);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double irpctScore = tree4.getScore();
        
        System.out.println("Score: " + irpctScore + ", plausibility: " +
                getPlausibilityScore(optimalScore, 
                                     independenceModelScore, 
                                     irpctScore));
        
        //// Iterative random PCT learner 2:
        System.out.println();
        System.out.println("IterativeRandomParsimoniousContextTreeLearner2:");
        
        startTime = System.currentTimeMillis();
        learner4b.setRandom(new Random());
        learner4b.setIterations(1000);
        
        ParsimoniousContextTree<Character> tree4b = learner4b.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree4b);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double tree4bScore = tree4b.getScore();
        
        System.out.println("Score: " + tree4bScore + ", plausibility: " + 
                getPlausibilityScore(optimalScore,
                                     independenceModelScore, 
                                     tree4bScore));
        
        //// Iterative random PCT learner 3:
        System.out.println();
        System.out.println("IterativeRandomParsimoniousContextTreeLearner3:");
        learner4c.setRandom(new Random());
        learner4c.setIterations(1000);
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree4c = learner4c.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree4c);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double tree4cScore = tree4c.getScore();
        
        System.out.println("Score: " + tree4cScore + ", plausibility: " +
                getPlausibilityScore(optimalScore, 
                                     independenceModelScore, 
                                     tree4cScore));
        
        //// Radix PCT learner:
        System.out.println();
        System.out.println("RadixParsimoniousContextTreeLearner:");
        RadixParsimoniousContextTreeLearner<Character> radixLearner = 
                new RadixParsimoniousContextTreeLearner<>(Character::compareTo, 
                                                          3);
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree5 = radixLearner.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree5);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double radixLearnerScore = tree5.getScore();
        
        System.out.println("Score: " + radixLearnerScore + ", plausibility: " +
                getPlausibilityScore(optimalScore, 
                                     independenceModelScore,
                                     radixLearnerScore));
        
        //// Iterative radix PCT learner:
        System.out.println();
        System.out.println("IterativeRadixParsimoniousContextTreeLearner:");
        IterativeRadixParsimoniousContextTreeLearner<Character> iterativeRadixLearner = 
                new IterativeRadixParsimoniousContextTreeLearner<>();
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree6 = iterativeRadixLearner.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree6);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        
        System.out.println("Score: " + tree6.getScore() + ", plausibility: " +
                getPlausibilityScore(optimalScore, 
                                     independenceModelScore, 
                                     tree6.getScore()));
        
        
        System.exit(0);
        ///////
        List<DataRow<Integer>> data = new ArrayList<>();
        
//        data.add(new DataRow<>(1, 1, 2, 2, 1, 2, 2, 1, 1, 1, 1, 2, 1));
//        data.add(new DataRow<>(1, 1, 1, 2, 2, 1, 1, 2, 1, 1, 1, 2, 2));
//        data.add(new DataRow<>(2, 1, 1, 1, 2, 1, 1, 1, 1, 2, 2, 1, 1));
//
//        data.add(new DataRow<>(2, 1, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 2));
//        data.add(new DataRow<>(1, 2, 1, 1, 1, 2, 2, 2, 2, 1, 2, 1, 1));
//        data.add(new DataRow<>(2, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 2));
//
//        data.add(new DataRow<>(1, 1, 2, 2, 2, 1, 2, 2, 1, 1, 2, 1, 1));
//        data.add(new DataRow<>(2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 1, 1));
//        data.add(new DataRow<>(1, 1, 2, 2, 1, 2, 1, 1, 1, 2, 2, 2, 2));
        
//        data.add(new DataRow<>(2, 3, 1, 2));
//        data.add(new DataRow<>(0, 1, 3, 2));
//        data.add(new DataRow<>(1, 1, 3, 3));
//        
//        data.add(new DataRow<>(0, 0, 2, 1));
//        data.add(new DataRow<>(3, 2, 0, 1));
//        data.add(new DataRow<>(3, 1, 1, 1));
//       
//        data.add(new DataRow<>(3, 0, 1, 2));
//        data.add(new DataRow<>(2, 1, 2, 1));
//        data.add(new DataRow<>(0, 0, 0, 0));

        data.add(new DataRow<>(0, 1, 0, 3));
        data.add(new DataRow<>(2, 0, 6, 5));
        data.add(new DataRow<>(3, 1, 3, 4));
        data.add(new DataRow<>(1, 6, 2, 0));
        data.add(new DataRow<>(2, 4, 5, 1));
        data.add(new DataRow<>(3, 0, 2, 2));
//
//        data.add(new DataRow<>(0, 1, 2, 3));
//        data.add(new DataRow<>(2, 0, 1, 5));
//        data.add(new DataRow<>(3, 1, 1, 4));
//        data.add(new DataRow<>(1, 4, 5, 0));
//        data.add(new DataRow<>(2, 4, 3, 1));
//        data.add(new DataRow<>(3, 0, 4, 2));
//        
//        IterativeRandomParsimoniousContextTreeLearner<Integer> 
//                learnerIterative =
//                new IterativeRandomParsimoniousContextTreeLearner<>();
//        
//        learnerIterative.setRandom(new Random());
//        learnerIterative.setIterations(50);
//        
//        startTime = System.currentTimeMillis();
//        
//        ParsimoniousContextTree<Integer> myTree1 = learnerIterative.learn(data);
//        
//        endTime = System.currentTimeMillis();
//        System.out.println(
//                "IterativeRandomParsimoniousContextTreeLearner: " +
//                myTree1.getScore() + ": " + (endTime - startTime) +
//                " milliseconds.");
//        
//        BasicParsimoniousContextTreeLearner<Integer> learnerBasic =
//                new BasicParsimoniousContextTreeLearner<>();
//        
//        startTime = System.currentTimeMillis();
//        
//        ParsimoniousContextTree<Integer> myTree2 = learnerBasic.learn(data);
//        
//        endTime = System.currentTimeMillis();
//        System.out.println("BasicParsimoniousContextTreeLearner: " + 
//                           myTree2.getScore() + ": " + (endTime - startTime) +
//                           " milliseconds.");
//        
//        AbstractParsimoniousContextTreeLearner<Integer> learner3 = 
//                new IndependenceModelParsimoniousContextTreeLearner<>();
//        
//        System.out.println("IM: " + learner3.learn(data).getScore());
    }
    
    private static void checkFile(File file) {
        if (!file.exists()) {
            System.err.println("The file \"" + file.getAbsolutePath() + "\" " +
                               "does not exist.");
            System.exit(1);
        }
        
        if (!file.isFile()) {
            System.err.println("The file \"" + file.getAbsolutePath() + "\" " +
                               "is not a regular file.");
            System.exit(1);
        }
    }
    
    private static void printHelpMessage(PrintStream out) {
        out.println("usage: java -jar YourJar.jar FILE START DEPTH");
        out.println("Where:");
        out.println("  FILE  " + 
                    "is the name of the file containing the input data.");
        out.println("  START " +
                    "is the starting index of the explanatory variable.");
        out.println("  DEPTH is the number of explanatory variables/" +
                    "the depth of the PCT tree.");
    }
    
    private static void generateDataViaMC(String textFileName,
                                     int order,
                                     int lines,
                                     int length) {
        File textFile = new File(textFileName);
        List<String> words = new TextWordsProvider().getWordsFromFile(textFile);
        MarkovChain mc = new MarkovChain(order, words);
        
        for (int i = 0; i < lines; ++i) {
            String string = mc.generate(length);
            
            while (string == null) {
                string = mc.generate(length);
            }
            
            System.out.println(string);
        }
    }
    
    private static void generateDataViaMC(String textFileName, 
                                     String alphabetFileName,
                                     int order,
                                     int lines,
                                     int length) {
        File textFile      = new File(textFileName);
        File alphabetFile  = new File(alphabetFileName);
        List<String> words = new TextWordsProvider().getWordsFromFile(textFile);
        Map<Character, Character> reductionMap = getReductionMap(alphabetFile);
        List<String> reducedWords =
                new AlphabetReducer().reduceAlphabet(words, reductionMap);
        MarkovChain mc = new MarkovChain(order, reducedWords);
        
        for (int i = 0; i < lines; ++i) {
            String string;
            
            do {
                string = mc.generate(length);
            } while (string == null);
            
            System.out.println(string);
        }
    }
    
    private static void generateDataViaPCT(String weightFileName,
                                           int order,
                                           int lines,
                                           int lineLength) {
//        File weightFile = new File(weightFileName);
//        List<Double> weightList = new ArrayList<>();
//        
//        try (Scanner scanner = new Scanner(weightFile)) {
//            while (scanner.hasNextLine()) {
//                weightList.add(scanner.nextDouble());
//            }
//        } catch (FileNotFoundException ex) {
//            throw new RuntimeException(
//                    "Weight file \"" + weightFileName + "\" not found.", ex);
//        }
//        
//        double[] weights = new double[weightList.size()];
//        
//        for (int i = 0; i < weights.length; ++i) {
//            weights[i] = weightList.get(i);
//        }
//        
//        PCTDataGenerator generator = new PCTDataGenerator(order, 
//                                                          weights.length, 
//                                                          weights);
//        
//        String[] data = generator.generate(lines, lineLength);
//        
//        for (String s : data) {
//            System.out.println(s);
//        }
    }
    
    private static void generateDataViaPCT2(int alphabetSize,
                                            int order,
                                            int lines,
                                            int lineLength) {
        PCTDataGenerator generator = new PCTDataGenerator(order, alphabetSize);
        String[] data = generator.generate(lines, lineLength);
        
        for (String s : data) {
            System.out.println(s);
        }
    }
    
    private static void generateDataViaPCT3(int alphabetSize,
                                            int order,
                                            int lines,
                                            int lineLength,
                                            double beta) {
        PCTDataGenerator3 generator = 
                new PCTDataGenerator3(order, 
                                      alphabetSize, 
                                      beta);
        
        DataGeneratingPCT3 pct = generator.getPCT();
        
//        System.out.println("--- PCT ----");
//        System.out.println(pct);
//        
//        System.out.println("--- Data ---");
        String[] data = generator.generate(lines, lineLength);
        
        for (String s : data) {
            System.out.println(s);
        }
    }
    
    private static Map<Character, Character> getReductionMap(File file) {
        Map<Character, Character> reductionMap = new HashMap<>();
        
        try {
            Scanner scanner = new Scanner(file);
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                char[] chars = line.toCharArray();
                char character = chars[0];
                
                for (int i = 1; i < chars.length; ++i) {
                    reductionMap.put(chars[i], character);
                }
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        
        return reductionMap;
    }
    
    private static final int ROWS = 10;
    
    private static void benchmark() {
        warmup();
        
        Random random = new Random();
        AbstractParsimoniousContextTreeLearner<Integer> learner = 
                new BasicParsimoniousContextTreeLearner<>();
        
        for (int depth = 1; depth <= 3; ++depth) {
            for (int alphabetSize = 2; alphabetSize <= 7; ++alphabetSize) {
                List<DataRow<Integer>> data = generateData(depth, 
                                                           alphabetSize, 
                                                           ROWS,
                                                           random);
                
                long start = System.currentTimeMillis();
                learner.learn(data);
                long end = System.currentTimeMillis();
                
                System.out.println(
                        "Depth = " + depth + ", alphabet size = " +
                        alphabetSize + ": " + (end - start) + " milliseconds.");
            }
        }
    }
    
    private static List<DataRow<Integer>> generateData(int depth,
                                                       int alphabetSize,
                                                       int rows,
                                                       Random random) {
        List<DataRow<Integer>> dataRows = new ArrayList<>(rows);
        
        for (int row = 0; row < rows; ++row) {
            dataRows.add(generateDataRow(depth, alphabetSize, random));
        }
        
        return dataRows;
    }
    
    private static DataRow<Integer> generateDataRow(int depth,
                                                    int alphabetSize,
                                                    Random random) {
        Integer[] data = new Integer[depth + 1];
        
        for (int i = 0; i < data.length; ++i) {
            data[i] = random.nextInt(alphabetSize);
        }
        
        return new DataRow<>(data);
    }
    
    private static void warmup() {
        long start = System.currentTimeMillis();
        List<DataRow<Integer>> data = new ArrayList<>();
        
        data.add(new DataRow<>(0, 1, 2, 6));
        data.add(new DataRow<>(3, 1, 4, 4));
        data.add(new DataRow<>(2, 6, 3, 5));
        data.add(new DataRow<>(2, 3, 2, 6));
        data.add(new DataRow<>(1, 4, 2, 6));
        
        AbstractParsimoniousContextTreeLearner<Integer> learner = 
                new BasicParsimoniousContextTreeLearner<>();
        
        learner.learn(data);
        long end = System.currentTimeMillis();
        System.out.println("Warmed up in " + (end - start) + " milliseconds.");
    }   
    
    private static double getPlausibilityScore(double optimalScore,
                                               double independenceModelScore,
                                               double targetScore) {
        double enumerator = targetScore - independenceModelScore;
        double denominator = optimalScore - independenceModelScore;
        return enumerator / denominator;
    }
}
