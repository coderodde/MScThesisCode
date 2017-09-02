package net.coderodde.msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import net.coderodde.msc.support.BasicParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IndependenceModelParsimoniousContextTreeLearner;
import net.coderodde.msc.support.IterativeRandomParsimoniousContextTreeLearner;
import net.coderodde.msc.support.RandomParsimoniousContextTreeLearner;

public class Main {

    public static void main(String[] args) {
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
        
        try (Scanner scanner = new Scanner(file, "UTF-8")) {
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
        
        AbstractParsimoniousContextTreeLearner<Character> learner = 
                new BasicParsimoniousContextTreeLearner<>();
        
        RandomParsimoniousContextTreeLearner<Character> learner2 = 
                new RandomParsimoniousContextTreeLearner<>();
        
        AbstractParsimoniousContextTreeLearner<Character> learner3 = 
                new IndependenceModelParsimoniousContextTreeLearner<>();
        
        IterativeRandomParsimoniousContextTreeLearner<Character> learner4 =
                new IterativeRandomParsimoniousContextTreeLearner<>();
        
        learner2.setRandom(new Random());
        learner4.setIterations(100);
        learner4.setRandom(new Random());
        
        System.out.println("BasicParsimoniousContextTreeLearner:");
        
        long startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree = 
                learner.learn(dataRows);
    
        long endTime = System.currentTimeMillis();
        
        System.out.println(tree);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        
        System.out.println();
        System.out.println("RandomParsimoniousContextTreeLearner:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree2 = learner2.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree2);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        
        System.out.println();
        System.out.println("IndependenceModelParsimoniousContextTreeLearner:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree3 = learner3.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree3);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
        
        System.out.println();
        System.out.println("IterativeRandomParsimoniousContextTreeLearner:");
        
        startTime = System.currentTimeMillis();
        
        ParsimoniousContextTree<Character> tree4 = learner4.learn(dataRows);
        
        endTime = System.currentTimeMillis();
        
        System.out.println(tree4);
        System.out.println("Time: " + (endTime - startTime) + " milliseconds.");
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
}
