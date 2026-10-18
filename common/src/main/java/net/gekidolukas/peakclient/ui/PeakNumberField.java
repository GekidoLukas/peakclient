package net.gekidolukas.peakclient.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class PeakNumberField extends PeakElement<PeakNumberField> implements PeakInput {
    private double value = 0;
    private double min = 0;
    private double max = 100;
    private double step = 1;
    private int decimals = -1;
    private boolean showButtons = true;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;

    public PeakNumberField() { this(PeakId.random()); }

    public PeakNumberField(String id) {
        super(id);
        size = PeakSize.pixels(100, 16);
        interaction = PeakInteraction.create();
    }

    public static PeakNumberField create() { return new PeakNumberField(); }
    public static PeakNumberField create(String id) { return new PeakNumberField(id); }

    public static PeakNumberField create(String id, double min, double max, double value) {
        return new PeakNumberField(id).range(min, max).value(value);
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public PeakNumberField range(double min, double max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        return this;
    }

    public double getStep() { return step; }
    public PeakNumberField step(double step) { this.step = Math.max(0, step); return this; }

    public int getDecimals() { return decimals; }
    public PeakNumberField decimals(int decimals) { this.decimals = Math.max(-1, Math.min(decimals, 8)); return this; }

    public double getValue() { return normalize(value); }
    public PeakNumberField value(double value) { this.value = value; return this; }

    public boolean isShowButtons() { return showButtons; }
    public PeakNumberField showButtons(boolean showButtons) { this.showButtons = showButtons; return this; }

    public PeakPaint getColor() { return color; }
    public PeakNumberField color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakNumberField color(int argb) { return color(PeakPaint.solid(argb)); }

    public double normalize(double raw) {
        if (Double.isNaN(raw) || Double.isInfinite(raw)) return Math.max(min, Math.min(max, 0));
        double result = raw;
        if (step > 0) result = min + Math.round((raw - min) / step) * step;
        result = Math.max(min, Math.min(max, result));
        int digits = decimals >= 0 ? decimals : 6;
        return BigDecimal.valueOf(result).setScale(digits, RoundingMode.HALF_UP).doubleValue();
    }

    public double stepped(double from, int steps) {
        double unit = step > 0 ? step : Math.max(1e-6, (max - min) / 20.0);
        return normalize(from + unit * steps);
    }

    public String format(double number) {
        if (decimals >= 0) return String.format(Locale.ROOT, "%." + decimals + "f", number);
        String text = String.format(Locale.ROOT, "%.4f", number);
        text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        return text.equals("-0") ? "0" : text;
    }

    public static Double parse(String text) {
        if (text == null) return null;
        String cleaned = text.trim().replace(',', '.');
        if (cleaned.isEmpty() || cleaned.equals("-") || cleaned.equals(".") || cleaned.equals("-.")) return null;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String inputValue() { return format(getValue()); }

    @Override
    public String elementType() { return "number_field"; }
}
