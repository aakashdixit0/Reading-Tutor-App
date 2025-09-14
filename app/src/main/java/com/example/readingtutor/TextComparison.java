package com.example.readingtutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextComparison {
    
    public static class ComparisonResult {
        public List<String> wrongWords;
        public int wrongWordPosition;
        public boolean hasErrors;
        public String remainingText;
        
        public ComparisonResult() {
            wrongWords = new ArrayList<>();
            wrongWordPosition = -1;
            hasErrors = false;
            remainingText = "";
        }
    }

    public static Map<String, String> findWrongWords(String original, String spoken) {
        ComparisonResult result = findWrongWordsWithPosition(original, spoken);
        Map<String, String> wrong = new HashMap<>();
        
        if (result.hasErrors && !result.wrongWords.isEmpty()) {
            // For backward compatibility, return the first wrong word
            wrong.put(result.wrongWords.get(0), "गलत उच्चारण");
        }
        
        return wrong;
    }
    
    public static ComparisonResult findWrongWordsWithPosition(String original, String spoken) {
        ComparisonResult result = new ComparisonResult();
        
        if (original == null) original = "";
        if (spoken == null) spoken = "";
        
        // simple normalization: remove punctuation, lower case
        String normOrig = original.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        String normSpoken = spoken.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        
        if (normOrig.isEmpty() || normSpoken.isEmpty()) {
            return result;
        }

        String[] origWords = normOrig.split("\\s+");
        String[] spokenWords = normSpoken.split("\\s+");

        // Compare words using a simple loop
        int i = 0, j = 0;
        while (i < origWords.length && j < spokenWords.length) {
            if (origWords[i].equals(spokenWords[j])) {
                // Words match, move to the next pair
                i++;
                j++;
            } else {
                // Words do not match, identify the wrong word
                result.wrongWords.add(origWords[i]);
                result.wrongWordPosition = i;
                result.hasErrors = true;
                
                // Calculate remaining text from the wrong word position
                StringBuilder remainingBuilder = new StringBuilder();
                for (int k = i; k < origWords.length; k++) {
                    if (k > i) remainingBuilder.append(" ");
                    remainingBuilder.append(origWords[k]);
                }
                result.remainingText = remainingBuilder.toString();
                
                return result; // Stop after the first wrong word
            }
        }

        // If original text is longer, add the next missed word
        if (i < origWords.length) {
            result.wrongWords.add(origWords[i]);
            result.wrongWordPosition = i;
            result.hasErrors = true;
            
            // Calculate remaining text from the missed word position
            StringBuilder remainingBuilder = new StringBuilder();
            for (int k = i; k < origWords.length; k++) {
                if (k > i) remainingBuilder.append(" ");
                remainingBuilder.append(origWords[k]);
            }
            result.remainingText = remainingBuilder.toString();
        }

        // If spoken text is longer, mark the extra words
        if (j < spokenWords.length) {
            result.wrongWords.add("अतिरिक्त शब्द: " + spokenWords[j]);
            result.hasErrors = true;
            // For extra words, remaining text is from current position
            if (i < origWords.length) {
                StringBuilder remainingBuilder = new StringBuilder();
                for (int k = i; k < origWords.length; k++) {
                    if (k > i) remainingBuilder.append(" ");
                    remainingBuilder.append(origWords[k]);
                }
                result.remainingText = remainingBuilder.toString();
            }
        }

        return result;
    }
    
    // Helper method to get text from a specific word position
    public static String getTextFromPosition(String originalText, int wordPosition) {
        if (originalText == null || originalText.isEmpty() || wordPosition < 0) {
            return originalText;
        }
        
        String normOrig = originalText.replaceAll("[^a-zA-Z0-9\s]", "").toLowerCase().trim();
        String[] origWords = normOrig.split("\\s+");
        
        if (wordPosition >= origWords.length) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = wordPosition; i < origWords.length; i++) {
            if (i > wordPosition) result.append(" ");
            result.append(origWords[i]);
        }
        
        return result.toString();
    }
}
