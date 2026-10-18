package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakScrollbarStyle;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakScrollbars {
    public static final int NORMAL = 0;
    public static final int HOVER = 1;
    public static final int DRAG = 2;

    public record Geometry(int trackStart, int trackLength, int thumbStart, int thumbLength) {}

    public record Bar(boolean vertical,
                      int trackX1, int trackY1, int trackX2, int trackY2,
                      int thumbX1, int thumbY1, int thumbX2, int thumbY2) {}

    private PeakScrollbars() {}

    public static Geometry geometry(int viewport, int max, int offset, int margin, int minThumb) {
        int track = Math.max(1, viewport - margin * 2);
        int content = viewport + Math.max(0, max);
        int thumb = Math.min(track, Math.max(minThumb, Math.round(track * (viewport / (float) Math.max(1, content)))));
        int travel = track - thumb;
        int clamped = Math.max(0, Math.min(offset, Math.max(0, max)));
        int position = max <= 0 ? 0 : Math.round(travel * (clamped / (float) max));
        return new Geometry(margin, track, margin + position, thumb);
    }

    public static int offsetForThumbPosition(int trackLength, int thumbLength, int max, int thumbPositionInTrack) {
        int travel = trackLength - thumbLength;
        if (travel <= 0 || max <= 0) return 0;
        float fraction = Math.max(0, Math.min(thumbPositionInTrack, travel)) / (float) travel;
        return Math.round(fraction * max);
    }

    public static Bar layout(PeakScrollbarStyle style, boolean vertical, int originX, int originY,
                             int boxWidth, int boxHeight, int offset, int max) {
        int thickness = style.getThickness();
        int margin = style.getMargin();
        int viewport = vertical ? boxHeight : boxWidth;
        Geometry geometry = geometry(viewport, max, offset, margin, style.getMinThumbSize());
        if (vertical) {
            int x1 = originX + boxWidth - thickness - 1;
            int x2 = x1 + thickness;
            return new Bar(true,
                    x1, originY + geometry.trackStart(), x2, originY + geometry.trackStart() + geometry.trackLength(),
                    x1, originY + geometry.thumbStart(), x2, originY + geometry.thumbStart() + geometry.thumbLength());
        }
        int y1 = originY + boxHeight - thickness - 1;
        int y2 = y1 + thickness;
        return new Bar(false,
                originX + geometry.trackStart(), y1, originX + geometry.trackStart() + geometry.trackLength(), y2,
                originX + geometry.thumbStart(), y1, originX + geometry.thumbStart() + geometry.thumbLength(), y2);
    }

    public static void draw(GuiGraphics graphics, PeakScrollbarStyle style, Bar bar, int state) {
        int radius = style.getRadius();
        fill(graphics, bar.trackX1(), bar.trackY1(), bar.trackX2(), bar.trackY2(), radius, style.getTrackColor());
        PeakPaint thumbColor = switch (state) {
            case DRAG -> style.getThumbDragColor();
            case HOVER -> style.getThumbHoverColor();
            default -> style.getThumbColor();
        };
        fill(graphics, bar.thumbX1(), bar.thumbY1(), bar.thumbX2(), bar.thumbY2(), radius, thumbColor);
    }

    private static void fill(GuiGraphics graphics, int x1, int y1, int x2, int y2, int radius, PeakPaint paint) {
        int limited = Math.min(radius, Math.min(x2 - x1, y2 - y1) / 2);
        PeakPaintRenderer.fillRounded(graphics, paint, x1, y1, x2, y2, Math.max(0, limited));
    }
}
