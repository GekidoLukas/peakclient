package net.gekidolukas.peakclient.ui;

public final class PeakNumberFieldLayout {
    public static final int MIN_BUTTON = 12;

    public enum Part { NONE, MINUS, PLUS, TEXT }

    private final int buttonWidth;
    private final int textX;
    private final int textWidth;
    private final int width;
    private final int height;
    private final boolean buttons;

    private PeakNumberFieldLayout(int width, int height, boolean buttons) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.buttons = buttons;
        int button = buttons ? Math.max(MIN_BUTTON, Math.min(this.height, this.width / 3)) : 0;
        this.buttonWidth = button;
        this.textX = button;
        this.textWidth = Math.max(1, this.width - 2 * button);
    }

    public static PeakNumberFieldLayout of(int width, int height, boolean showButtons) {
        return new PeakNumberFieldLayout(width, height, showButtons);
    }

    public boolean hasButtons() { return buttons; }
    public int buttonWidth() { return buttonWidth; }
    public int textX() { return textX; }
    public int textWidth() { return textWidth; }
    public int plusX() { return width - buttonWidth; }
    public int width() { return width; }
    public int height() { return height; }

    public Part partAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return Part.NONE;
        if (buttons && x < buttonWidth) return Part.MINUS;
        if (buttons && x >= width - buttonWidth) return Part.PLUS;
        return Part.TEXT;
    }
}
