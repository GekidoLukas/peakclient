package net.gekidolukas.peakclient.ui;

public record PeakRect(int x, int y, int width, int height) {
    public boolean contains(int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    public int right() { return x + width; }

    public int bottom() { return y + height; }
}
