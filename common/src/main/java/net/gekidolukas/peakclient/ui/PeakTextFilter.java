package net.gekidolukas.peakclient.ui;

import java.util.regex.Pattern;

public enum PeakTextFilter {
    ANY(null),
    INTEGER(Pattern.compile("-?\\d*")),
    DECIMAL(Pattern.compile("-?\\d*(\\.\\d*)?")),
    ALPHANUMERIC(Pattern.compile("[A-Za-z0-9_]*"));

    private final Pattern pattern;

    PeakTextFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean isValid(String text) {
        return pattern == null || pattern.matcher(text).matches();
    }
}
