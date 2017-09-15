package net.coderodde.msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class TextWordsProvider {

    public List<String> getWordsFromFile(File file) {
        List<String> words = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(file);
            String line = scanner.nextLine();
            String[] tokens = line.split("\\s+");
            
            for (String token : tokens) {
                words.add(token.toLowerCase());
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        
        return words;
    }
}
