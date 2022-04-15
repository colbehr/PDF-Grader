package com.ezgrader.pdfgrader;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFilters {
    public static TextFormatter GetIntFilter() {
        return MakeFilter("(\\d*)");
    }
    public static TextFormatter GetDoubleFilter() {
        return MakeFilter("(\\d+)\\.?(\\d)*");
    }

    private static TextFormatter MakeFilter(String regex) {
        UnaryOperator<TextFormatter.Change> numericFilter = c -> {
            if (c.getText().equals("")) return c;

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(c.getControlNewText());
            if (!m.matches()) {
                c.setText("");
            }
            return c;
        };
        return new TextFormatter<>(numericFilter);
    }
}
