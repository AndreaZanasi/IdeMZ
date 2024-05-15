package com.IdeMZ;

import javafx.util.Callback;
import org.fxmisc.richtext.StyleClassedTextArea;

public class StyleClassedTextAreaFactory implements Callback<Class<?>, Object> {
    @Override
    public Object call(Class<?> param) {
        if (param == StyleClassedTextArea.class) {
            return new StyleClassedTextArea();
        }
        return null;
    }
}