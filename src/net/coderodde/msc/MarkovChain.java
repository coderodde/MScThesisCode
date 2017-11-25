package net.coderodde.msc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class MarkovChain {

    private final Map<String, Map<String, Double>> probabilityMap = 
            new HashMap<>();
    
    private final Map<String, Integer> countMap = new HashMap<>();
    private final List<String> tokenList;
    private final Set<String> tokenSet = new HashSet<>();
    private final Random random = new Random(1);
    private final int order;
    
    public MarkovChain(int order, List<String> words) {
        this.order = order;
        
        for (String word : words) {
            if (word.length() <= order) {
                continue;
            }
            
            String[] tokens = new String[word.length() - order + 1];
            
            for (int i = 0; i <= word.length() - order; ++i) {
                String token = word.substring(i, i + order);
                tokens[i] = token;
            }
            
            for (int i = 0; i < tokens.length - 1; ++i) {
                String from = tokens[i];
                String to   = tokens[i + 1];
                tokenSet.add(from);
                countMap.put(from, countMap.getOrDefault(from, 0) + 1);
                
                if (!probabilityMap.containsKey(from)) {
                    probabilityMap.put(from, new HashMap<>());
                }
                
                if (!probabilityMap.get(from).containsKey(to)) {
                    probabilityMap.get(from).put(to, 1.0);
                } else {
                    probabilityMap.get(from).put(to,
                            probabilityMap.get(from).get(to) + 1);
                }
            }
        }
        
        for (Map.Entry<String, Map<String, Double>> e : probabilityMap.entrySet()) {
            for (Map.Entry<String, Double> e2 : e.getValue().entrySet()) {
                double count = countMap.get(e.getKey());
                e2.setValue(e2.getValue() / count);
            }
        }
        
        this.tokenList = new ArrayList<>(tokenSet);
    }
    
    public String generate(int length) {
        if (length < order) {
            throw new IllegalArgumentException("The length is too small.");
        }
        
        StringBuilder sb = new StringBuilder(length);
        String currentToken = chooseFirstToken();
        sb.append(currentToken);
        
        while (sb.length() < length) {
            currentToken = getRandomMove(currentToken);
            
            if (currentToken == null) {
                return null;
            }
            
            sb.append(currentToken.charAt(currentToken.length() - 1));
        }
        
        return sb.toString();
    }
    
    private String chooseFirstToken() {
        return tokenList.get(random.nextInt(tokenList.size()));
    }
    
    private String getRandomMove(String token) {
        Map<String, Double> m = probabilityMap.get(token);
        
        if (m == null) {
            return null;
        }
        
        double coin = random.nextDouble();
        
        for (Map.Entry<String, Double> e : m.entrySet()) {
            if (coin < e.getValue()) {
                return e.getKey();
            }
            
            coin -= e.getValue();
        }
        
        throw new IllegalStateException("Should not get here.");
    }
    
    public static void main(String[] args) {
        List<String> words = new ArrayList<>(Arrays.asList("abcb", 
                                                           "bacca",
                                                           "ccba",
                                                           "baac",
                                                           "ccabc",
                                                           "bbab"));
        MarkovChain mc = new MarkovChain(3, words);
        
        for (int i = 0; i < 10; ++i) {
            System.out.println(mc.generate(5));
        }
        
        List<String> words2 = new ArrayList<>(Arrays.asList("1101",
                                                            "00110",
                                                            "101110",
                                                            "1",
                                                            "101",
                                                            "00",
                                                            "0001",
                                                            "100"));
        
        MarkovChain mc2 = new MarkovChain(2, words2);
        
        System.out.println("Oh yeah.");
    }
}
