package com.IdeMZ;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    private Map<String, String> dialect;
    private final Map<String, String> syntax;
    private final Pattern integerPattern = Pattern.compile("\\b\\d+\\b");
    private String dialectName;


    public SyntaxHighlighter(String dialectName) {
        this.dialectName = dialectName;
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/dialects/" + dialectName + ".json")) {
            dialect = mapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + dialectName + ".json", e);
        }

        try (InputStream is = getClass().getResourceAsStream("/syntax/syntax.json")) {
            syntax = mapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load syntax.json", e);
        }
    }

    public void updateDialect(String dialectName) {
        this.dialectName = dialectName;
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/dialects/" + dialectName + ".json")) {
            dialect = mapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + dialectName + ".json", e);
        }
    }

    public void highlight(StyleClassedTextArea textArea, boolean isDarkMode) {
        textArea.clearStyle(0, textArea.getLength());

        if (isDarkMode) {
            textArea.setStyleClass(0, textArea.getLength(), "dark-mode-non-keyword");
        } else {
            textArea.setStyleClass(0, textArea.getLength(), "light-mode-non-keyword");
        }

        StringBuilder text = new StringBuilder(textArea.getText());
        boolean inComment = false;
        boolean inMultiLineComment = false;
        boolean inString = false;
        String commentToken = dialect.get("comment");
        String stringToken = dialect.get("quotes");

        for (int i = 0; i < text.length(); i++) {
            String currentChar = text.substring(i, i + 1);
            String nextChar = i < text.length() - 1 ? text.substring(i + 1, i + 2) : "";

            if (currentChar.equals(commentToken) && nextChar.equals(commentToken) && !inString) {
                textArea.setStyleClass(i, i + 2, "symbol-comment");
                inMultiLineComment = !inMultiLineComment;
                i++;
            } else if (currentChar.equals(commentToken) && !inMultiLineComment && !inString) {
                inComment = true;
            } else if (currentChar.equals("\n") && inComment && !inString) {
                inComment = false;
            } else if (currentChar.equals(stringToken) && !inString) {
                inString = true;
            } else if ((currentChar.equals(stringToken) || currentChar.equals("\n")) && inString) {
                inString = false;
            }

            if (inComment || inMultiLineComment) {
                textArea.setStyleClass(i, i + 1, "symbol-comment");
            } else if (inString) {
                textArea.setStyleClass(i, i + 1, "symbol-quote");
            } else {
                i = applyStyle(textArea, text.toString(), i);
                Matcher matcher = integerPattern.matcher(text.substring(i));
                if (matcher.find() && matcher.start() == 0) {
                    textArea.setStyleClass(i, i + matcher.end(), "integer");
                    i += matcher.end() - 1;
                }
            }
        }
    }

    private int applyStyle(StyleClassedTextArea textArea, String text, int i) {
        for (String word : dialect.keySet()) {
            if (text.startsWith(word, i)) {
                String style = syntax.get(dialect.get(word));
                if (style != null) {
                    textArea.setStyleClass(i, i + word.length(), style);
                }
                return i + word.length() - 1;
            }
        }
        return i;
    }
}