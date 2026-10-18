package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PeakDropdown extends PeakElement<PeakDropdown> implements PeakInput {
    private List<PeakOption> options = new ArrayList<>();
    private String selected = "";
    private String placeholder = "";
    private int maxVisible = 6;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;

    public PeakDropdown() { this(PeakId.random()); }

    public PeakDropdown(String id) {
        super(id);
        size = PeakSize.pixels(120, 16);
        interaction = PeakInteraction.create();
    }

    public static PeakDropdown create() { return new PeakDropdown(); }
    public static PeakDropdown create(String id) { return new PeakDropdown(id); }

    public List<PeakOption> getOptions() { return options == null ? List.of() : options; }
    public PeakDropdown option(String value, String text) { return option(PeakOption.of(value, text)); }
    public PeakDropdown option(String value, String text, boolean enabled) { return option(PeakOption.of(value, text, enabled)); }
    public PeakDropdown option(PeakOption option) {
        if (options == null) options = new ArrayList<>();
        options.add(option);
        return this;
    }
    public PeakDropdown options(PeakOption... values) {
        options = new ArrayList<>(Arrays.asList(values));
        return this;
    }

    public String getSelected() { return selected == null ? "" : selected; }
    public PeakDropdown selected(String value) { this.selected = value == null ? "" : value; return this; }

    public int selectedIndex() {
        List<PeakOption> list = getOptions();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getValue().equals(getSelected())) return i;
        }
        return -1;
    }

    public String getPlaceholder() { return placeholder == null ? "" : placeholder; }
    public PeakDropdown placeholder(String placeholder) { this.placeholder = placeholder == null ? "" : placeholder; return this; }

    public int getMaxVisible() { return Math.max(1, Math.min(maxVisible, 32)); }
    public PeakDropdown maxVisible(int rows) { this.maxVisible = Math.max(1, Math.min(rows, 32)); return this; }

    public PeakPaint getColor() { return color; }
    public PeakDropdown color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakDropdown color(int argb) { return color(PeakPaint.solid(argb)); }

    public String selectedText() {
        int index = selectedIndex();
        return index < 0 ? "" : getOptions().get(index).getText();
    }

    public int nextEnabledIndex(int from, int step) {
        List<PeakOption> list = getOptions();
        int count = list.size();
        if (count == 0) return -1;
        int index = from;
        for (int i = 0; i < count; i++) {
            index = Math.floorMod(index + step, count);
            if (list.get(index).isEnabled()) return index;
        }
        return -1;
    }

    public int firstEnabledIndex() {
        List<PeakOption> list = getOptions();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isEnabled()) return i;
        }
        return -1;
    }

    public int lastEnabledIndex() {
        List<PeakOption> list = getOptions();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).isEnabled()) return i;
        }
        return -1;
    }

    @Override
    public String inputValue() { return getSelected(); }

    @Override
    public String elementType() { return "dropdown"; }
}
