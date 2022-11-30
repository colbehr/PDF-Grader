package com.gradeflow.pdfgrader;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for creating regex filters for various UI TextFields
 */
public class TextFilters {
    public static final String intRegex = "(\\d*)";
    public static final String doubleRegex = "(\\d+)\\.?(\\d)*";
    public static final String pointsRegex = "([+-]?)((\\d+)\\.?(\\d)*)?";
    public static final String anyRegex = ".*";

    public static TextFormatter GetIntFilter() {
        return MakeFilter(intRegex);
    }
    public static TextFormatter GetDoubleFilter() {
        return MakeFilter(doubleRegex);
    }

    /**
     * Creates a new custom filter
     * @param regex - the regular expression which the input will be limited to
     * @return the custom TextFormatter
     */
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
