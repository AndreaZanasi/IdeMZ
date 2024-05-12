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

        // Initialize keywords with CSS class names
        keywords.put("exit", "keyword-exit");
        keywords.put("let", "keyword-let");
        keywords.put("if", "keyword-if");
        keywords.put("elif", "keyword-elif");
        keywords.put("else", "keyword-else");
        keywords.put("while", "keyword-while");
        keywords.put("true", "keyword-true");
        keywords.put("false", "keyword-false");
        keywords.put("print", "keyword-print");

        // Initialize symbols with CSS class names
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

    public void highlight(StyleClassedTextArea textArea) {
        // Clear previous styles
        textArea.clearStyle(0, textArea.getLength());

        // Apply the "non-keyword" style class to all text initially
        textArea.setStyleClass(0, textArea.getLength(), "non-keyword");

        // Apply keyword and symbol styles
        String text = textArea.getText();
        for (int i = 0; i < text.length(); i++) {
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
        }

        // Highlight integers
        Pattern pattern = Pattern.compile("\\b\\d+\\b");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            textArea.setStyleClass(matcher.start(), matcher.end(), "integer");
        }
    }
}