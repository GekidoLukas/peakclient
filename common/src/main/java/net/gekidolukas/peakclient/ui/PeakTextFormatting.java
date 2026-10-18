package net.gekidolukas.peakclient.ui;

import java.util.Locale;

public final class PeakTextFormatting {
    private PeakTextFormatting() {}

    public static String colorCode(int rgb) {
        StringBuilder builder = new StringBuilder("§x");
        String hex = String.format(Locale.ROOT, "%06X", rgb & 0xFFFFFF);
        for (char digit : hex.toCharArray()) builder.append('§').append(digit);
        return builder.toString();
    }

    public static String reset() { return "§r"; }

    public static String colored(String text, int rgb) {
        return colorCode(rgb) + text + reset();
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final StringBuilder builder = new StringBuilder();

        public Builder append(String text) {
            builder.append(text);
            return this;
        }

        public Builder append(String text, int rgbColor) {
            builder.append(colored(text, rgbColor));
            return this;
        }

        public Builder appendFormatted(String text, char formatCode) {
            builder.append('§').append(formatCode).append(text).append(reset());
            return this;
        }

        public Builder append(String text, int rgbColor, char formatCode) {
            builder.append(colorCode(rgbColor)).append('§').append(formatCode).append(text).append(reset());
            return this;
        }

        public String build() { return builder.toString(); }

        @Override
        public String toString() { return build(); }
    }
}
