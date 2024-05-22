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
        for (int i = 0; i < text.length(); i++) {
            System.out.println(text);
            i = applyStyle(textArea, text.toString(), i);
            Matcher matcher = integerPattern.matcher(text.substring(i));
            if (matcher.find() && matcher.start() == 0) {
                textArea.setStyleClass(i, i + matcher.end(), "integer");
                i += matcher.end() - 1;
            }
        }
    }

    private int applyStyle(StyleClassedTextArea textArea, String text, int i) {
        String style;
        for (String word : dialect.keySet()) {
            if (text.startsWith(word, i)) {
                if (dialect.get(word).equals("comment")) {
                    int end = text.indexOf("\n", i);
                    if (end == -1) {
                        end = text.length();
                    }
                    if (text.startsWith(word + word, i)) {
                        end = text.indexOf(word + word, i + 2);
                        if (end == -1) {
                            end = text.length();
                        } else {
                            end += 2;
                        }
                    }
                    style = syntax.get(dialect.get(word));
                    if (end > text.length()) {
                        end = text.length();
                    }
                    textArea.setStyleClass(i, end, style);
                    return end - 1;
                } else if (dialect.get(word).equals("quotes")) {
                    int end = i + 1;
                    while (end < text.length() && text.charAt(end) != '\n' && text.charAt(end) != '"') {
                        end++;
                    }
                    if (end < text.length() && text.charAt(end) == '"') {
                        end++;
                    }
                    style = syntax.get(dialect.get(word));
                    textArea.setStyleClass(i, end, style);
                    return end - 1;
                }
                style = syntax.get(dialect.get(word));
                if (style != null) {
                    int end = i + word.length();
                    if (end > text.length()) {
                        end = text.length();
                    }
                    textArea.setStyleClass(i, end, style);
                }
            }
        }
        return i;
    }
}