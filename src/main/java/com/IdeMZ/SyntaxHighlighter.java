package com.IdeMZ;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fxmisc.richtext.StyleClassedTextArea;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private final Map<String, String> keywords;
    private final Map<String, String> symbols;
    private final Pattern integerPattern = Pattern.compile("\\b\\d+\\b");

    public SyntaxHighlighter() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/syntax/syntax.json")) {
            Map<String, Map<String, String>> syntax = mapper.readValue(is, new TypeReference<>() {});
            keywords = syntax.get("keywords");
            symbols = syntax.get("symbols");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load syntax.json", e);
        }
    }

    public void highlight(StyleClassedTextArea textArea) {
        // Clear previous styles
        textArea.clearStyle(0, textArea.getLength());

        // Apply the "non-keyword" style class to all text initially
        textArea.setStyleClass(0, textArea.getLength(), "non-keyword");

        // Apply keyword, symbol, comment, and string styles
        StringBuilder text = new StringBuilder(textArea.getText());
        boolean inComment = false;
        boolean inMultiLineComment = false;
        boolean inString = false;
        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            char nextChar = i < text.length() - 1 ? text.charAt(i + 1) : '\0';

            if (currentChar == '@' && nextChar == '@') {
                textArea.setStyleClass(i, i+1, "symbol-comment"); // Apply style to the first '@'
                inMultiLineComment = !inMultiLineComment;
                i++; // Skip the next '@'
            } else if (currentChar == '@' && !inMultiLineComment) {
                inComment = true;
            } else if (currentChar == '\n' && inComment && !inString) {
                inComment = false;
            } else if (currentChar == '\"' && !inString) {
                inString = true;
            } else if ((currentChar == '\"' || currentChar == '\n') && inString) {
                inString = false;
            }

            if (inComment || inMultiLineComment) {
                textArea.setStyleClass(i, i+1, "symbol-comment");
            } else if (inString) {
                textArea.setStyleClass(i, i+1, "symbol-quote");
            } else {
                i = applyKeywordOrSymbolStyle(textArea, text.toString(), i);
                // Highlight integers only if not in comment or string
                Matcher matcher = integerPattern.matcher(text.substring(i));
                if (matcher.find() && matcher.start() == 0) {
                    textArea.setStyleClass(i, i + matcher.end(), "integer");
                    i += matcher.end() - 1; // -1 because the for loop will increment i
                }
            }
        }
    }

    private int applyKeywordOrSymbolStyle(StyleClassedTextArea textArea, String text, int i) {
        for (String keyword : keywords.keySet()) {
            if (text.startsWith(keyword, i)) {
                textArea.setStyleClass(i, i + keyword.length(), keywords.get(keyword));
                return i + keyword.length() - 1; // return updated i
            }
        }
        for (String symbol : symbols.keySet()) {
            if (text.startsWith(symbol, i)) {
                textArea.setStyleClass(i, i + symbol.length(), symbols.get(symbol));
                return i + symbol.length() - 1; // return updated i
            }
        }
        return i; // return original i if no keyword or symbol found
    }
}