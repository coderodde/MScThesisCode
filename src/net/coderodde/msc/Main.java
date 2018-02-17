package net.coderodde.msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import net.coderodde.msc.support.BasicParsimoniousContextTreeLearner;
import net.coderodde.msc.support.BasicParsimoniousContextTreeLearnerV2;
import net.coderodde.msc.support.HeuristicParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IndependenceModelParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner2;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner3;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner3B;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner3BV2;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner3V2;
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
                        Integer.parseInt(args[3]), // order
                        Integer.parseInt(args[4]), // lines
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
                        Integer.parseInt(args[2]), // order
                        Integer.parseInt(args[3]), // lines
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
                int order = Integer.parseInt(args[2]);
                int lines = Integer.parseInt(args[3]);
                int lineLength = Integer.parseInt(args[4]);
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

//        benchmark1(dataRows, getLargeAlphabetRows(start, depth));
//        System.exit(0);

        BasicParsimoniousContextTreeLearner<Character> basicPCTLearner
                = new BasicParsimoniousContextTreeLearner<>();
        
        BasicParsimoniousContextTreeLearnerV2<Character> basicPCTLearnerV2 = 
                new BasicParsimoniousContextTreeLearnerV2<>();

        IndependenceModelParsimoniousContextTreeLearner<Character> 
                indepenendenceModelPCTLearner
                = new IndependenceModelParsimoniousContextTreeLearner<>();

        RandomParsimoniousContextTreeLearner<Character> randomPCTLearner1
                = new RandomParsimoniousContextTreeLearner<>();

        RandomParsimoniousContextTreeLearner2<Character> randomPCTLearner2
                = new RandomParsimoniousContextTreeLearner2<>(0.8);

        RandomParsimoniousContextTreeLearner3<Character> randomPCTLearner3
                = new RandomParsimoniousContextTreeLearner3<>();
       
        HeuristicParsimoniousContextTreeLearner<Character> heuristicPCTLearner = 
                new HeuristicParsimoniousContextTreeLearner<>();
        
        randomPCTLearner1.setRandom(new Random());
        randomPCTLearner2.setRandom(new Random());
        randomPCTLearner3.setRandom(new Random());

        IterativeRandomParsimoniousContextTreeLearner<Character>
                iterativeRandomPCTLearner1
                = new IterativeRandomParsimoniousContextTreeLearner<>();

        IterativeRandomParsimoniousContextTreeLearner2<Character>
                iterativeRandomPCTLearner2
                = new IterativeRandomParsimoniousContextTreeLearner2<>();

        IterativeRandomParsimoniousContextTreeLearner3<Character> 
                iterativeRandomPCTLearner3
                = new IterativeRandomParsimoniousContextTreeLearner3<>();
        
        IterativeRandomParsimoniousContextTreeLearner3B<Character>
                iterativeRandomPCTLearner3B =
                new IterativeRandomParsimoniousContextTreeLearner3B<>();
        
        IterativeRandomParsimoniousContextTreeLearner3V2 
                iterativeRandomPCTLearner3V2 =
                new IterativeRandomParsimoniousContextTreeLearner3V2();
        
        IterativeRandomParsimoniousContextTreeLearner3BV2
                iterativeRandomPCTLearner3BV2 = 
                new IterativeRandomParsimoniousContextTreeLearner3BV2();

        iterativeRandomPCTLearner2.setBeta(0.9);
        randomPCTLearner1.setRandom(new Random());
        iterativeRandomPCTLearner1.setIterations(1000);
        iterativeRandomPCTLearner1.setRandom(new Random());
        iterativeRandomPCTLearner2.setIterations(1000);
        iterativeRandomPCTLearner3.setIterations(1000);

        //// Optimal learner:
        System.out.println(
                "--- " + basicPCTLearner.getClass().getSimpleName() + " ---");
        startTime = System.currentTimeMillis();

        ParsimoniousContextTree<Character> basicPCTTree
                = basicPCTLearner.learn(dataRows);

        endTime = System.currentTimeMillis();

        System.out.println(basicPCTTree);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");

        double optimalScore = basicPCTTree.getScore();
        System.out.println("Optimal score: " + optimalScore);
        System.out.println();
        
        //// Faster optimal learner:
        System.out.println("--- " + basicPCTLearnerV2.getClass().getSimpleName() + " ---");
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> basicPCTTree2 = 
                basicPCTLearnerV2.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(basicPCTTree2);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        
        double optimalScoreV2 = basicPCTTree2.getScore();
        System.out.println("Score: " + optimalScoreV2);
        System.out.println();

        //// Independence model learner:
        System.out.println("--- " + indepenendenceModelPCTLearner.getClass().getSimpleName() + " ---");
        startTime = System.currentTimeMillis();

        ParsimoniousContextTree<Character> independenceModel = 
                indepenendenceModelPCTLearner.learn(dataRows);

        endTime = System.currentTimeMillis();

        System.out.println(independenceModel);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double independenceModelScore = independenceModel.getScore();
        System.out.println();

        //// Heuristic learner:
        System.out.println("--- " + heuristicPCTLearner.getClass().getSimpleName() + " ---");
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> heuristicTree =
                heuristicPCTLearner.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(heuristicTree);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        System.out.println("Score: " + heuristicTree.getScore() + ", plausibility: " 
                + getPlausibilityScore(optimalScore, 
                                       independenceModelScore, 
                                       heuristicTree.getScore()));
        System.out.println();
        
        // INFO: the flag you are looking for is here!
        boolean omitBasicRandomStuff = true;
        
        if (!omitBasicRandomStuff) {
            //// Random PCT learner:
            System.out.println("--- " + randomPCTLearner1.getClass().getSimpleName() + " ---");
            startTime = System.currentTimeMillis();

            ParsimoniousContextTree<Character> randomPCTTree1 = 
                    randomPCTLearner1.learn(dataRows);

            endTime = System.currentTimeMillis();

            System.out.println(randomPCTTree1);
            System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
            System.out.println("Score: " + randomPCTTree1.getScore() + ", plausibility: "
                    + getPlausibilityScore(optimalScore,
                                           independenceModelScore,
                                           randomPCTTree1.getScore()));
            System.out.println();

            //// 2nd random PCT learner:
            System.out.println("--- " + randomPCTLearner2.getClass().getSimpleName() + " ---");
            startTime = System.currentTimeMillis();

            ParsimoniousContextTree<Character> randomPCTTree2 = 
                    randomPCTLearner2.learn(dataRows);

            endTime = System.currentTimeMillis();

            System.out.println(randomPCTTree2);
            System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
            System.out.println("Score: " + randomPCTTree2.getScore() + ", plausibility: "
                    + getPlausibilityScore(optimalScore,
                                           independenceModelScore,
                                           randomPCTTree2.getScore()));
            System.out.println();

            //// 3rd random PCT learner:
            System.out.println("--- " + randomPCTLearner3.getClass().getSimpleName() + " ---");
            startTime = System.currentTimeMillis();

            ParsimoniousContextTree<Character> randomPCTTree3 = 
                    randomPCTLearner3.learn(dataRows);

            endTime = System.currentTimeMillis();

            System.out.println(randomPCTTree3);
            System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
            System.out.println("Score: " + randomPCTTree3.getScore() + ", plausibility: "
                    + getPlausibilityScore(optimalScore,
                                           independenceModelScore,
                                           randomPCTTree3.getScore()));
            System.out.println();

            //// Iterative random PCT learner:
            System.out.println("--- " + iterativeRandomPCTLearner1.getClass().getSimpleName() + " ---");
            iterativeRandomPCTLearner1.setIterations(1000);
            startTime = System.currentTimeMillis();
            ParsimoniousContextTree<Character> iterativeRandomPCTTree1 = 
                    iterativeRandomPCTLearner1.learn(dataRows);
            endTime = System.currentTimeMillis();

            System.out.println(iterativeRandomPCTTree1);
            System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
            double iterativeRandomPCTTree1Score = 
                    iterativeRandomPCTTree1.getScore();

            System.out.println("Score: " + iterativeRandomPCTTree1Score + 
                    ", plausibility: "
                    + getPlausibilityScore(optimalScore,
                                           independenceModelScore,
                                           iterativeRandomPCTTree1Score));
            System.out.println();

            //// Iterative random PCT learner 2:
            System.out.println("--- " + iterativeRandomPCTLearner2.getClass().getSimpleName() + " ---");
            iterativeRandomPCTLearner2.setRandom(new Random());
            iterativeRandomPCTLearner2.setIterations(1000);
            startTime = System.currentTimeMillis();

            ParsimoniousContextTree<Character> iterativeRandomPCTTree2 = 
                    iterativeRandomPCTLearner2.learn(dataRows);

            endTime = System.currentTimeMillis();

            System.out.println(iterativeRandomPCTTree2);
            System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
            double iterativeRandomPCTTree2Score = 
                    iterativeRandomPCTTree2.getScore();

            System.out.println("Score: " + iterativeRandomPCTTree2Score +
                    ", plausibility: "
                    + getPlausibilityScore(optimalScore,
                                           independenceModelScore,
                                           iterativeRandomPCTTree2Score));
            System.out.println();

            //// Iterative random PCT learner 3:
            System.out.println("--- " + iterativeRandomPCTLearner3.getClass().getSimpleName() + " ---");
            iterativeRandomPCTLearner3.setRandom(new Random());
            iterativeRandomPCTLearner3.setIterations(1000);
            iterativeRandomPCTLearner3.setMaximumLabelsPerNode(5);

            startTime = System.currentTimeMillis();

            ParsimoniousContextTree<Character> iterativeRandomPCTTree3 = 
                    iterativeRandomPCTLearner3.learn(dataRows);

            endTime = System.currentTimeMillis();

            System.out.println(iterativeRandomPCTTree3);
            System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
            double iterativeRandomPCTTree3Score = iterativeRandomPCTTree3.getScore();

            System.out.println("Score: " + iterativeRandomPCTTree3Score + 
                    ", plausibility: "
                    + getPlausibilityScore(optimalScore,
                                           independenceModelScore,
                                           iterativeRandomPCTTree3Score));
            System.out.println();

            //// Iterative random PCT learner 3B:
            System.out.println("--- " + iterativeRandomPCTLearner3B.getClass().getSimpleName() + " ---");
            iterativeRandomPCTLearner3B.setRandom(new Random());
            iterativeRandomPCTLearner3B.setK(1000);
            iterativeRandomPCTLearner3B.setMaximumLabelsPerNode(5);

            startTime = System.currentTimeMillis();

            ParsimoniousContextTree<Character> iterativeRandomPCTTree3B = 
                    iterativeRandomPCTLearner3B.learn(dataRows);

            endTime = System.currentTimeMillis();

            System.out.println(iterativeRandomPCTTree3B);
            System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
            double iterativeRandomPCTTree3BScore = 
                    iterativeRandomPCTTree3B.getScore();

            System.out.println("Score: " + iterativeRandomPCTTree3Score + 
                    ", plausibility: "
                    + getPlausibilityScore(optimalScore,
                                           independenceModelScore,
                                           iterativeRandomPCTTree3BScore));
            System.out.println();
        }
        //// Iterative random PCT learner 3V2:
        System.out.println("--- " + iterativeRandomPCTLearner3V2.getClass().getSimpleName() + " ---");
        iterativeRandomPCTLearner3V2.setRandom(new Random());
        iterativeRandomPCTLearner3V2.setIterations(1000);
        iterativeRandomPCTLearner3V2.setMaximumLabelsPerNode(5);
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> iterativeRandomPCTTree3V2 =
                iterativeRandomPCTLearner3V2.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(iterativeRandomPCTTree3V2);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double iterativeRandomPCTTree3V2Score = 
                iterativeRandomPCTTree3V2.getScore();
        
        System.out.println("Score: " + iterativeRandomPCTTree3V2Score + 
                ", plausibility: " +
                getPlausibilityScore(optimalScore, 
                                     independenceModelScore, 
                                     iterativeRandomPCTTree3V2Score));
        System.out.println();
        
        //// Iterative random PCT learner 3BV2:
        System.out.println("--- " + iterativeRandomPCTLearner3BV2.getClass().getSimpleName() + " ---");
        iterativeRandomPCTLearner3BV2.setRandom(new Random());
        iterativeRandomPCTLearner3BV2.setK(1000);
        iterativeRandomPCTLearner3BV2.setMaximumLabelsPerNode(5);
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> iterativeRandomPCTTree3BV2 =
                iterativeRandomPCTLearner3BV2.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(iterativeRandomPCTTree3V2);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        double iterativeRandomPCTTree3BV2Score = 
                iterativeRandomPCTTree3BV2.getScore();
        
        System.out.println("Score: " + iterativeRandomPCTTree3V2Score + 
                ", plausibility: " +
                getPlausibilityScore(optimalScore, 
                                     independenceModelScore, 
                                     iterativeRandomPCTTree3BV2Score));
    }

    private static void checkFile(File file) {
        if (!file.exists()) {
            System.err.println("The file \"" + file.getAbsolutePath() + "\" "
                    + "does not exist.");
            System.exit(1);
        }

        if (!file.isFile()) {
            System.err.println("The file \"" + file.getAbsolutePath() + "\" "
                    + "is not a regular file.");
            System.exit(1);
        }
    }

    private static void printHelpMessage(PrintStream out) {
        out.println("usage: java -jar YourJar.jar FILE START DEPTH");
        out.println("Where:");
        out.println("  FILE  "
                + "is the name of the file containing the input data.");
        out.println("  START "
                + "is the starting index of the explanatory variable.");
        out.println("  DEPTH is the number of explanatory variables/"
                + "the depth of the PCT tree.");
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
        File textFile = new File(textFileName);
        File alphabetFile = new File(alphabetFileName);
        List<String> words = new TextWordsProvider().getWordsFromFile(textFile);
        Map<Character, Character> reductionMap = getReductionMap(alphabetFile);
        List<String> reducedWords
                = new AlphabetReducer().reduceAlphabet(words, reductionMap);
        MarkovChain mc = new MarkovChain(order, reducedWords);

        for (int i = 0; i < lines; ++i) {
            String string;

            do {
                string = mc.generate(length);
            } while (string == null);

            System.out.println(string);
        }
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
        PCTDataGenerator3 generator
                = new PCTDataGenerator3(order,
                        alphabetSize,
                        beta);

        DataGeneratingPCT3 pct = generator.getPCT();
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
        AbstractParsimoniousContextTreeLearner<Integer> learner
                = new BasicParsimoniousContextTreeLearner<>();

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
                        "Depth = " + depth + ", alphabet size = "
                        + alphabetSize + ": " + (end - start) + " milliseconds.");
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

        AbstractParsimoniousContextTreeLearner<Integer> learner
                = new BasicParsimoniousContextTreeLearner<>();

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

    private static List<DataRow<Character>> getLargeAlphabetRows(int start,
            int depth) {
        File file = new File("/Users/rodionefremov/Desktop/ProGradu/"
                + "WarAndPeaceDataAlphabet26.txt");
        List<DataRow<Character>> dataRows = new ArrayList<>();

        try (Scanner scanner = new Scanner(file, "ISO-8859-1")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim().toLowerCase();
                Character[] arr = new Character[line.length()];

                int i = 0;

                for (char c : line.toCharArray()) {
                    arr[i++] = c;
                }

                dataRows.add(new DataRow<>(arr, start, depth));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            System.err.println("File not found!");
            System.exit(0);
        }
        System.out.println("Rows read: " + dataRows.size());
        return dataRows;
    }

    private static void benchmark1(List<DataRow<Character>> dataRows,
            List<DataRow<Character>> hugeAlphabetDataRows) {
        BasicParsimoniousContextTreeLearner<Character> basicLearner
                = new BasicParsimoniousContextTreeLearner<>();

        IndependenceModelParsimoniousContextTreeLearner<Character> independenceModelLearner
                = new IndependenceModelParsimoniousContextTreeLearner<>();

        IterativeRandomParsimoniousContextTreeLearner3<Character> iterativeRandomLearner
                = new IterativeRandomParsimoniousContextTreeLearner3<>();

        IterativeRandomParsimoniousContextTreeLearner3B<Character> iterativeRandomLearnerB
                = new IterativeRandomParsimoniousContextTreeLearner3B<>();

        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        System.out.println("Seed = " + seed);

        iterativeRandomLearner.setRandom(random);
        iterativeRandomLearner.setIterations(1000);
        iterativeRandomLearner.setMaximumLabelsPerNode(10);
        iterativeRandomLearnerB.setRandom(new Random(seed));
        iterativeRandomLearnerB.setK(1000);
        iterativeRandomLearnerB.setMaximumLabelsPerNode(10);

        boolean turnOnComparison = true;
        long start;
        long end;

        if (turnOnComparison) {
            System.out.println();
            System.out.println("--- COMPARING ---");

            start = System.currentTimeMillis();
            ParsimoniousContextTree<Character> tree1 = basicLearner.learn(dataRows);
            end = System.currentTimeMillis();

            System.out.println(basicLearner.getClass().getSimpleName() + " in "
                    + (end - start) + " milliseconds.");
            System.out.println("Optimal score: " + tree1.getScore());

            start = System.currentTimeMillis();
            ParsimoniousContextTree<Character> tree2
                    = independenceModelLearner.learn(dataRows);
            end = System.currentTimeMillis();

            System.out.println();
            System.out.println(independenceModelLearner.getClass().getSimpleName()
                    + " in " + (end - start) + " milliseconds.");
            System.out.println("Baseline score: " + tree2.getScore());

            final double optimalScore = tree1.getScore();
            final double baselineScore = tree2.getScore();

            start = System.currentTimeMillis();
            ParsimoniousContextTree<Character> tree3
                    = iterativeRandomLearner.learn(dataRows);
            end = System.currentTimeMillis();

            System.out.println();
            System.out.println(iterativeRandomLearner.getClass().getSimpleName()
                    + " in " + (end - start) + " milliseconds.");
            System.out.println("Score: " + tree3.getScore());
            System.out.println(
                    "Plausibility: " + getPlausibilityScore(optimalScore,
                            baselineScore,
                            tree3.getScore()));

            start = System.currentTimeMillis();
            ParsimoniousContextTree<Character> tree4
                    = iterativeRandomLearnerB.learn(dataRows);
            end = System.currentTimeMillis();

            System.out.println();
            System.out.println(iterativeRandomLearnerB.getClass().getSimpleName()
                    + " in " + (end - start) + " milliseconds.");
            System.out.println("Score: " + tree4.getScore());
            System.out.println(
                    "Plausibility: " + getPlausibilityScore(optimalScore,
                            baselineScore,
                            tree4.getScore()));
        }

        System.out.println();
        System.out.println("--- MEASURING ---");
        iterativeRandomLearner.setRandom(new Random(seed));
        iterativeRandomLearnerB.setRandom(new Random(seed));
        iterativeRandomLearner.setMaximumLabelsPerNode(5);  
        iterativeRandomLearnerB.setMaximumLabelsPerNode(5);

        start = System.currentTimeMillis();
        ParsimoniousContextTree<Character> largeTree1
                = iterativeRandomLearner.learn(hugeAlphabetDataRows);
        end = System.currentTimeMillis();

        System.out.println(iterativeRandomLearner.getClass().getSimpleName()
                + " in " + (end - start) + " milliseconds.");
        System.out.println("Score: " + largeTree1.getScore());

        System.out.println();

        start = System.currentTimeMillis();
        ParsimoniousContextTree<Character> largeTree2
                = iterativeRandomLearnerB.learn(hugeAlphabetDataRows);
        end = System.currentTimeMillis();

        System.out.println(iterativeRandomLearnerB.getClass().getSimpleName()
                + " in " + (end - start) + " milliseconds.");
        System.out.println("Score: " + largeTree2.getScore());
        
        System.out.println("Large tree 1:");
        System.out.println(largeTree1);
        
        System.out.println("------");
        System.out.println("Large tree 2:");
        System.out.println(largeTree2);
    }
}
