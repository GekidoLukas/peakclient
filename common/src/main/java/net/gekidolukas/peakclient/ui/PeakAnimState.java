package net.gekidolukas.peakclient.ui;

public record PeakAnimState(boolean hidden, float offsetX, float offsetY, float scale, float alpha, PeakRevealState reveal) {
    public static final PeakAnimState VISIBLE = new PeakAnimState(false, 0, 0, 1f, 1f);
    public static final PeakAnimState HIDDEN = new PeakAnimState(true, 0, 0, 1f, 0f);

    public PeakAnimState {
        if (reveal == null) reveal = PeakRevealState.IDENTITY;
    }

    public PeakAnimState(boolean hidden, float offsetX, float offsetY, float scale, float alpha) {
        this(hidden, offsetX, offsetY, scale, alpha, PeakRevealState.IDENTITY);
    }
}
