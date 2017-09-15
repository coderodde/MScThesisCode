package net.coderodde.msc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AlphabetReducer {

    public List<String> reduceAlphabet(List<String> words,
                                       Map<Character, Character> reductionMap) {
        List<String> reducedWords = new ArrayList<>();
        
        for (String word : words) {
            reducedWords.add(reduce(word, reductionMap));
        }
        
        return reducedWords;
    }
    
    private String reduce(String word, Map<Character, Character> reductionMap) {
        StringBuilder sb = new StringBuilder(word.length());
        
        for (char c : word.toCharArray()) {
            // Possibly omit unknown characters.
            if (reductionMap.containsKey(c)) {
                sb.append(reductionMap.get(c));
            }
        }
        
        return sb.toString();
    }
}
