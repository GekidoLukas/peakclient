package net.gekidolukas.peakclient.ui;

import java.util.Locale;

public final class PeakSlider extends PeakElement<PeakSlider> implements PeakInput {
    private double value = 0;
    private double min = 0;
    private double max = 100;
    private double step = 0;
    private boolean showValue = true;
    private String format = "";

    public PeakSlider() { this(PeakId.random()); }

    public PeakSlider(String id) {
        super(id);
        size = PeakSize.pixels(140, 14);
        interaction = PeakInteraction.create();
    }

    public static PeakSlider create() { return new PeakSlider(); }
    public static PeakSlider create(String id) { return new PeakSlider(id); }

    public static PeakSlider create(String id, double min, double max, double value) {
        return new PeakSlider(id).range(min, max).value(value);
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public PeakSlider range(double min, double max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        return this;
    }

    public double getStep() { return step; }
    public PeakSlider step(double step) { this.step = Math.max(0, step); return this; }

    public double getValue() { return snap(value); }
    public PeakSlider value(double value) { this.value = value; return this; }

    public boolean isShowValue() { return showValue; }
    public PeakSlider showValue(boolean showValue) { this.showValue = showValue; return this; }

    public String getFormat() { return format == null ? "" : format; }
    public PeakSlider format(String format) { this.format = format == null ? "" : format; return this; }

    public double fraction() {
        return max <= min ? 0 : (getValue() - min) / (max - min);
    }

    public double valueForFraction(double fraction) {
        double clamped = Math.max(0, Math.min(1, fraction));
        return snap(min + clamped * (max - min));
    }

    public double keyStep() {
        return step > 0 ? step : (max - min) / 20.0;
    }

    private double snap(double raw) {
        double result = raw;
        if (step > 0) result = min + Math.round((raw - min) / step) * step;
        return Math.max(min, Math.min(max, result));
    }

    public String displayText() {
        double current = getValue();
        if (!getFormat().isEmpty()) {
            try {
                return String.format(Locale.ROOT, getFormat(), current);
            } catch (RuntimeException ignored) {
                return inputValue();
            }
        }
        return inputValue();
    }

    @Override
    public String inputValue() {
        double current = getValue();
        if (current == Math.rint(current) && Math.abs(current) < 1e12) return Long.toString((long) current);
        String text = String.format(Locale.ROOT, "%.3f", current);
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    public String elementType() { return "slider"; }
}
