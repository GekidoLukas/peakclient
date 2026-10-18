package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;

public final class PeakSeparator {
    private final String id;
    private int thickness = 1;
    private PeakPaint color = PeakPaint.solid(0xFF555555);
    private int width = 180;
    private int spacing = 3;
    private String label = "";
    private PeakPaint labelColor = PeakPaint.solid(0xAAAAAA);

    private PeakSeparator(String id) {
        this.id = PeakId.require(id, "Separator");
    }

    public static PeakSeparator create(String id) {
        return new PeakSeparator(id);
    }

    public PeakSeparator thickness(int thickness) {
        this.thickness = Math.max(1, thickness);
        return this;
    }

    public PeakSeparator color(PeakPaint paint) {
        this.color = paint;
        return this;
    }

    public PeakSeparator color(int argb) {
        return color(PeakPaint.solid(argb));
    }

    public PeakSeparator width(int width) {
        this.width = Math.max(1, width);
        return this;
    }

    public PeakSeparator spacing(int spacing) {
        this.spacing = Math.max(0, spacing);
        return this;
    }

    public PeakSeparator label(String label) {
        this.label = label == null ? "" : label;
        return this;
    }

    public PeakSeparator labelColor(PeakPaint paint) {
        this.labelColor = paint;
        return this;
    }

    public PeakSeparator labelColor(int argb) {
        return labelColor(PeakPaint.solid(argb));
    }

    public PeakPanel build() {
        PeakPanel line = PeakPanel.create(label.isEmpty() ? id : id + "/line")
                .size(width, thickness).background(color).padding(0);
        if (label.isEmpty()) {
            line.margin(spacing, 0, spacing, 0);
            return line;
        }
        PeakPanel root = PeakPanel.create(id).column().gap(2).padding(0).background(0).margin(spacing, 0, spacing, 0);
        root.add(PeakLabel.create(id + "/label", label).color(labelColor), line);
        return root;
    }
}
