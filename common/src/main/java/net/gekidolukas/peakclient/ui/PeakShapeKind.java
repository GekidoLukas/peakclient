package net.gekidolukas.peakclient.ui;

public enum PeakShapeKind {
    ROUNDED, CIRCLE, TRIANGLE, DIAMOND, PENTAGON, HEXAGON, HEXAGON_FLAT, OCTAGON, STAR;

    public boolean isPolygon() {
        return this != ROUNDED && this != CIRCLE;
    }
}
