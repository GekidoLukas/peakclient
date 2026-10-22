package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PeakRadioGroup extends PeakElement<PeakRadioGroup> implements PeakInput {
    private List<PeakOption> options = new ArrayList<>();
    private String selected = "";
    private PeakLayoutDirection direction = PeakLayoutDirection.COLUMN;
    private int gap = 4;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;

    public PeakRadioGroup() { this(PeakId.random()); }

    public PeakRadioGroup(String id) {
        super(id);
        interaction = PeakInteraction.create();
    }

    public static PeakRadioGroup create() { return new PeakRadioGroup(); }
    public static PeakRadioGroup create(String id) { return new PeakRadioGroup(id); }

    public List<PeakOption> getOptions() { return options == null ? List.of() : options; }
    public PeakRadioGroup option(String value, String text) { return option(PeakOption.of(value, text)); }
    public PeakRadioGroup option(String value, String text, boolean enabled) { return option(PeakOption.of(value, text, enabled)); }
    public PeakRadioGroup option(String value, Component text) { return option(PeakOption.of(value, text)); }
    public PeakRadioGroup option(String value, Component text, boolean enabled) { return option(PeakOption.of(value, text, enabled)); }
    public PeakRadioGroup option(PeakOption option) {
        if (options == null) options = new ArrayList<>();
        options.add(option);
        return this;
    }
    public PeakRadioGroup options(PeakOption... values) {
        options = new ArrayList<>(Arrays.asList(values));
        return this;
    }

    public String getSelected() { return selected == null ? "" : selected; }
    public PeakRadioGroup selected(String value) { this.selected = value == null ? "" : value; return this; }

    public int selectedIndex() {
        List<PeakOption> list = getOptions();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getValue().equals(getSelected())) return i;
        }
        return -1;
    }

    public PeakLayoutDirection getDirection() { return direction == null ? PeakLayoutDirection.COLUMN : direction; }
    public PeakRadioGroup direction(PeakLayoutDirection direction) { this.direction = direction; return this; }
    public PeakRadioGroup row() { return direction(PeakLayoutDirection.ROW); }
    public PeakRadioGroup column() { return direction(PeakLayoutDirection.COLUMN); }

    public int getGap() { return Math.max(0, gap); }
    public PeakRadioGroup gap(int gap) { this.gap = Math.max(0, gap); return this; }

    public PeakPaint getColor() { return color; }
    public PeakRadioGroup color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakRadioGroup color(int argb) { return color(PeakPaint.solid(argb)); }

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
    public String elementType() { return "radio_group"; }
}
