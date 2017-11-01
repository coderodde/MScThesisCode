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
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split("\\s+");

                for (String token : tokens) {
                    token = filterToken(token.trim().toLowerCase());
                    words.add(token);
                }
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        
        return words;
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
