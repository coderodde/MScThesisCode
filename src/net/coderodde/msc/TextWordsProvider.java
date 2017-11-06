package net.coderodde.msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class TextWordsProvider {

    public List<String> getWordsFromFile(File file) {
        List<String> wordList = new ArrayList<>();
        
        try {
            Scanner scanner = new Scanner(file);
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] words = line.split("\\s+");

                for (String word : words) {
                    word = filterToken(word.trim().toLowerCase());
                    wordList.add(word);
                }
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        
        return wordList;
    }
    
    private String filterToken(String token) {
        StringBuilder sb = new StringBuilder(token.length());
        
        for (int i = 0; i < token.length(); ++i) {
            char c = token.charAt(i);
            
            if (c >= 'a' && c <= 'z') {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
}
