package net.gekidolukas.peakclient.ui;

public final class PeakInsets {
    private int top;
    private int right;
    private int bottom;
    private int left;

    public static PeakInsets create() { return new PeakInsets(); }
    public static PeakInsets all(int value) { return new PeakInsets().set(value); }
    public static PeakInsets of(int top, int right, int bottom, int left) {
        return new PeakInsets().top(top).right(right).bottom(bottom).left(left);
    }

    public int getTop() { return top; }
    public PeakInsets top(int top) { this.top = top; return this; }
    public int getRight() { return right; }
    public PeakInsets right(int right) { this.right = right; return this; }
    public int getBottom() { return bottom; }
    public PeakInsets bottom(int bottom) { this.bottom = bottom; return this; }
    public int getLeft() { return left; }
    public PeakInsets left(int left) { this.left = left; return this; }

    public PeakInsets set(int value) { this.top = this.right = this.bottom = this.left = value; return this; }
    public PeakInsets horizontal(int value) { this.left = this.right = value; return this; }
    public PeakInsets vertical(int value) { this.top = this.bottom = value; return this; }
}