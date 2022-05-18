package com.ezgrader.pdfgrader;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFilters {
    public static final String intRegex = "(\\d*)";
    public static final String doubleRegex = "(\\d+)\\.?(\\d)*";

    public static TextFormatter GetIntFilter() {
        return MakeFilter(intRegex);
    }
    public static TextFormatter GetDoubleFilter() {
        return MakeFilter(doubleRegex);
    }

    public static TextFormatter MakeFilter(String regex) {
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.getText().equals("")) return c;

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(c.getControlNewText());
            if (!m.matches()) {
                c.setText("");
            }
            return c;
        };
        return new TextFormatter<>(filter);
    }
}
