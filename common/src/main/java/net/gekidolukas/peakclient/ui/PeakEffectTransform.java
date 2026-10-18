package net.gekidolukas.peakclient.ui;

public record PeakEffectTransform(float offsetX, float offsetY, float scale, float rotationDegrees, float alpha,
                                  PeakEffectOverlay overlay, PeakRevealState reveal) {
    public static final PeakEffectTransform IDENTITY = new PeakEffectTransform(0f, 0f, 1f, 0f, 1f);
    private static final float EPSILON = 0.0001f;

    public PeakEffectTransform {
        if (reveal == null) reveal = PeakRevealState.IDENTITY;
    }

    public PeakEffectTransform(float offsetX, float offsetY, float scale, float rotationDegrees, float alpha,
                               PeakEffectOverlay overlay) {
        this(offsetX, offsetY, scale, rotationDegrees, alpha, overlay, PeakRevealState.IDENTITY);
    }

    public PeakEffectTransform(float offsetX, float offsetY, float scale, float rotationDegrees, float alpha) {
        this(offsetX, offsetY, scale, rotationDegrees, alpha, null, PeakRevealState.IDENTITY);
    }

    public static PeakEffectTransform of(PeakAnimState state) {
        return new PeakEffectTransform(state.offsetX(), state.offsetY(), state.scale(), 0f, state.alpha(), null, state.reveal());
    }

    public boolean hasOverlay() {
        return overlay != null && overlay.isVisible();
    }

    public boolean isIdentity() {
        return Math.abs(offsetX) < EPSILON && Math.abs(offsetY) < EPSILON && Math.abs(scale - 1f) < EPSILON
                && Math.abs(rotationDegrees) < EPSILON && alpha >= 0.999f && !hasOverlay() && reveal.isIdentity();
    }

    public PeakEffectTransform withOverlay(PeakEffectOverlay overlay) {
        return new PeakEffectTransform(offsetX, offsetY, scale, rotationDegrees, alpha, overlay, reveal);
    }

    public PeakEffectTransform then(PeakEffectTransform other) {
        PeakEffectOverlay combined = other.hasOverlay() ? other.overlay : overlay;
        PeakRevealState combinedReveal = other.reveal.isIdentity() ? reveal : other.reveal;
        return new PeakEffectTransform(offsetX + other.offsetX, offsetY + other.offsetY, scale * other.scale,
                rotationDegrees + other.rotationDegrees, alpha * other.alpha, combined, combinedReveal);
    }
}
