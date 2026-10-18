package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakScrollbars;

public record PeakScrollbarRegion(String ownerId, boolean vertical,
                                  int trackX1, int trackY1, int trackX2, int trackY2,
                                  int thumbX1, int thumbY1, int thumbX2, int thumbY2,
                                  int max, int clipLeft, int clipTop, int clipRight, int clipBottom) {
    public static final int HIT_SLOP = 2;

    public int axis(int x, int y) { return vertical ? y : x; }

    public int trackStart() { return vertical ? trackY1 : trackX1; }

    public int trackLength() { return vertical ? trackY2 - trackY1 : trackX2 - trackX1; }

    public int thumbStart() { return vertical ? thumbY1 : thumbX1; }

    public int thumbLength() { return vertical ? thumbY2 - thumbY1 : thumbX2 - thumbX1; }

    public boolean hitsTrack(int x, int y) {
        return x >= trackX1 - HIT_SLOP && x < trackX2 + HIT_SLOP && y >= trackY1 - HIT_SLOP && y < trackY2 + HIT_SLOP;
    }

    public boolean visibleAt(int x, int y) {
        return x >= clipLeft && x < clipRight && y >= clipTop && y < clipBottom;
    }

    public boolean thumbContainsAxis(int axisPosition) {
        return axisPosition >= thumbStart() && axisPosition < thumbStart() + thumbLength();
    }

    public int offsetForThumbStart(int thumbStartOnScreen) {
        return PeakScrollbars.offsetForThumbPosition(trackLength(), thumbLength(), max, thumbStartOnScreen - trackStart());
    }
}
