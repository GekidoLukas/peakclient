package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakFormat;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class PeakRichTextToolbar {
    public static final int ACTIVE_BACKGROUND = 0xFF3C78C8;

    private static final List<PeakFormat> DEFAULT_BUTTONS = List.of(
            PeakFormat.BOLD, PeakFormat.ITALIC, PeakFormat.UNDERLINE, PeakFormat.STRIKE,
            PeakFormat.COLOR_PREVIOUS, PeakFormat.COLOR_NEXT, PeakFormat.CLEAR);

    private final String id;
    private final String boxId;
    private final List<PeakFormat> buttons = new ArrayList<>(DEFAULT_BUTTONS);
    private int gap = 2;
    private boolean vertical;
    private PeakPaint activeBackground = PeakPaint.solid(ACTIVE_BACKGROUND);
    private Consumer<PeakButton> buttonStyle;
    private boolean withPicker;
    private Consumer<PeakColorPicker> pickerCustomizer;

    private PeakRichTextToolbar(String id, String boxId) {
        this.id = PeakId.require(id, "Toolbar");
        this.boxId = PeakId.require(boxId, "Element");
    }

    public static PeakRichTextToolbar create(String id, String boxId) {
        return new PeakRichTextToolbar(id, boxId);
    }

    public static String buttonId(String toolbarId, PeakFormat format) {
        return toolbarId + "/" + switch (format) {
            case BOLD -> "bold";
            case ITALIC -> "italic";
            case UNDERLINE -> "underline";
            case STRIKE -> "strike";
            case COLOR_NEXT -> "color-next";
            case COLOR_PREVIOUS -> "color-previous";
            case COLOR -> "color";
            case CLEAR -> "clear";
        };
    }

    public PeakRichTextToolbar buttons(PeakFormat... formats) {
        buttons.clear();
        buttons.addAll(Arrays.asList(formats));
        return this;
    }

    public PeakRichTextToolbar gap(int gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public PeakRichTextToolbar vertical() {
        this.vertical = true;
        return this;
    }

    public PeakRichTextToolbar activeBackground(PeakPaint paint) {
        this.activeBackground = paint;
        return this;
    }

    public PeakRichTextToolbar activeBackground(int argb) {
        return activeBackground(PeakPaint.solid(argb));
    }

    public PeakRichTextToolbar buttonStyle(Consumer<PeakButton> customizer) {
        this.buttonStyle = customizer;
        return this;
    }

    public PeakRichTextToolbar colorPicker() {
        this.withPicker = true;
        return this;
    }

    public PeakRichTextToolbar colorPicker(Consumer<PeakColorPicker> customizer) {
        this.withPicker = true;
        this.pickerCustomizer = customizer;
        return this;
    }

    private static String label(PeakFormat format) {
        return switch (format) {
            case BOLD -> "§lB";
            case ITALIC -> "§oI";
            case UNDERLINE -> "§nU";
            case STRIKE -> "§mS";
            case COLOR_PREVIOUS -> "«A";
            case COLOR_NEXT -> "A»";
            case COLOR -> "C";
            case CLEAR -> "Tx";
        };
    }

    private static boolean togglesState(PeakFormat format) {
        return format == PeakFormat.BOLD || format == PeakFormat.ITALIC
                || format == PeakFormat.UNDERLINE || format == PeakFormat.STRIKE;
    }

    public PeakPanel build() {
        PeakPanel row = PeakPanel.create(withPicker ? id + "/buttons" : id).gap(gap).padding(0).background(0);
        if (vertical) row.column(); else row.row();

        List<PeakFormat> formats = new ArrayList<>(buttons);
        if (withPicker && !formats.contains(PeakFormat.COLOR)) formats.add(PeakFormat.COLOR);

        for (PeakFormat format : formats) {
            PeakButton button;
            if (format == PeakFormat.COLOR) {
                if (!withPicker) continue;
                button = PeakButton.text(buttonId(id, format), label(format))
                        .padding(3, 6)
                        .onClick(PeakAction.toggle(id + "/picker"))
                        .keepFocus(true);
            } else {
                button = PeakButton.text(buttonId(id, format), label(format))
                        .padding(3, 6)
                        .onClick(PeakAction.format(boxId, format))
                        .keepFocus(true);
                if (togglesState(format)) {
                    button.activeStyle(PeakStyle.create().background(activeBackground));
                }
            }
            if (buttonStyle != null) buttonStyle.accept(button);
            row.add(button);
        }
        if (!withPicker) return row;

        PeakColorPicker picker = PeakColorPicker.create(id + "/picker").size(150, 100).target(boxId).hidden(true);
        if (pickerCustomizer != null) pickerCustomizer.accept(picker);
        PeakPanel root = PeakPanel.create(id).column().gap(gap + 2).padding(0).background(0);
        root.add(row, picker);
        return root;
    }
}
