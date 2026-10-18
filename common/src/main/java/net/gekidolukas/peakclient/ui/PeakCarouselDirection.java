package net.gekidolukas.peakclient.ui;

public enum PeakCarouselDirection {
    LEFT, RIGHT, UP, DOWN;

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public boolean isReversed() {
        return this == RIGHT || this == DOWN;
    }
}
