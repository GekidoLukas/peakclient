package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLabelRenderer;
import net.gekidolukas.peakclient.render.PeakPlaceholderResolver;
import net.gekidolukas.peakclient.render.PeakProgressValueResolver;
import net.gekidolukas.peakclient.ui.PeakAbstractProgress;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInput;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakReaction;
import net.gekidolukas.peakclient.ui.PeakTimer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PeakWatchValues {
    private static final Pattern LEADING_NUMBER = Pattern.compile("^[+-]?\\d+(?:\\.\\d+)?");
    private static final Pattern FORMAT_CODES = Pattern.compile("§.");

    private PeakWatchValues() {}

    public static String valueOf(PeakElement<?> element, PeakReaction reaction) {
        String watch = reaction.getWatch();
        if (!watch.isEmpty()) {
            PeakPlaceholderResolver.Result result = PeakPlaceholderResolver.resolveStrict(watch);
            return result.complete() ? result.value() : null;
        }
        return defaultValue(element);
    }

    static String defaultValue(PeakElement<?> element) {
        if (element instanceof PeakLabel label) return PeakLabelRenderer.resolvedText(label);
        if (element instanceof PeakTimer timer) return Long.toString(timer.currentValue());
        if (element instanceof PeakAbstractProgress<?> progress) {
            return format(PeakProgressValueResolver.resolve(progress).current());
        }
        if (element instanceof PeakInput input) return input.inputValue();
        return null;
    }

    public static Double number(String text) {
        if (text == null) return null;
        String plain = FORMAT_CODES.matcher(text).replaceAll("").trim();
        Matcher matcher = LEADING_NUMBER.matcher(plain);
        if (!matcher.find()) return null;
        try {
            return Double.parseDouble(matcher.group());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "0";
        BigDecimal decimal = BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
        return decimal.scale() < 0 ? decimal.setScale(0).toPlainString() : decimal.toPlainString();
    }
}
