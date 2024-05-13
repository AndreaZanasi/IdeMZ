package com.IdeMZ;

import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private final Map<String, String> keywords;
    private final Map<String, String> symbols;

    public SyntaxHighlighter() {
        keywords = new HashMap<>();
        symbols = new HashMap<>();
        keywords.put("exit", "keyword-exit");
        keywords.put("let", "keyword-let");
        keywords.put("if", "keyword-if");
        keywords.put("elif", "keyword-elif");
        keywords.put("else", "keyword-else");
        keywords.put("while", "keyword-while");
        keywords.put("true", "keyword-true");
        keywords.put("false", "keyword-false");
        keywords.put("print", "keyword-print");
        symbols.put("@", "symbol-comment");
        symbols.put("(", "symbol-open-paren");
        symbols.put(")", "symbol-close-paren");
        symbols.put("=", "symbol-equals");
        symbols.put("+", "symbol-plus");
        symbols.put("-", "symbol-minus");
        symbols.put("*", "symbol-asterisk");
        symbols.put("/", "symbol-slash");
        symbols.put("%", "symbol-percent");
        symbols.put(";", "symbol-semicolon");
        symbols.put("{", "symbol-open-brace");
        symbols.put("}", "symbol-close-brace");
        symbols.put("!", "symbol-exclamation");
        symbols.put(">", "symbol-greater");
        symbols.put(">=", "symbol-greater-equals");
        symbols.put("<", "symbol-less");
        symbols.put("<=", "symbol-less-equals");
        symbols.put("&", "symbol-ampersand");
        symbols.put("|", "symbol-vertical-bar");
        symbols.put("\"", "symbol-quote");
    }

    public boolean isKeywordOrSymbol(String text) {
        return keywords.containsKey(text) || symbols.containsKey(text);
    }

    public void highlight(StyleClassedTextArea textArea) {
        // Clear previous styles
        textArea.clearStyle(0, textArea.getLength());

        // Apply the "non-keyword" style class to all text initially
        textArea.setStyleClass(0, textArea.getLength(), "non-keyword");

        // Apply keyword, symbol, comment, and string styles
        String text = textArea.getText();
        boolean inComment = false;
        boolean inMultiLineComment = false;
        boolean inString = false;
        for (int i = 0; i < text.length(); i++) {
            if (i < text.length() - 1 && text.charAt(i) == '@' && text.charAt(i + 1) == '@') {
                textArea.setStyleClass(i, i+1, "symbol-comment"); // Apply style to the first '@'
                inMultiLineComment = !inMultiLineComment;
                i++; // Skip the next '@'
            } else if (text.charAt(i) == '@' && !inMultiLineComment) {
                inComment = true;
            } else if (text.charAt(i) == '\n' && inComment && !inString) {
                inComment = false;
            } else if (text.charAt(i) == '\"' && !inString) {
                inString = true;
            } else if ((text.charAt(i) == '\"' || text.charAt(i) == '\n') && inString) {
                inString = false;
            }

            if (inComment || inMultiLineComment) {
                textArea.setStyleClass(i, i+1, "symbol-comment");
            } else if (inString) {
                textArea.setStyleClass(i, i+1, "symbol-quote");
            } else {
                for (String keyword : keywords.keySet()) {
                    if (text.startsWith(keyword, i)) {
                        textArea.setStyleClass(i, i + keyword.length(), keywords.get(keyword));
                        i += keyword.length() - 1; // -1 because the for loop will increment i
                        break;
                    }
                }
                for (String symbol : symbols.keySet()) {
                    if (text.startsWith(symbol, i)) {
                        textArea.setStyleClass(i, i + symbol.length(), symbols.get(symbol));
                        i += symbol.length() - 1; // -1 because the for loop will increment i
                        break;
                    }
                }
                // Highlight integers only if not in comment or string
                Pattern pattern = Pattern.compile("\\b\\d+\\b");
                Matcher matcher = pattern.matcher(text.substring(i));
                if (matcher.find() && matcher.start() == 0) {
                    textArea.setStyleClass(i, i + matcher.end(), "integer");
                    i += matcher.end() - 1; // -1 because the for loop will increment i
                }
            }
        }
    }
}