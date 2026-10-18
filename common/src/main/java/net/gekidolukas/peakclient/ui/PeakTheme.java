package net.gekidolukas.peakclient.ui;

public final class PeakTheme {
    private PeakTheme() {}

    public static PeakPaint focusColor(PeakMenuSettings settings, PeakInteraction interaction) {
        if (interaction != null && !interaction.getFocusColor().isNone()) return interaction.getFocusColor();
        return settings == null ? PeakPaint.solid(PeakFocusStyle.DEFAULT_COLOR) : settings.getFocus().getColor();
    }

    public static int focusWidth(PeakMenuSettings settings, PeakInteraction interaction) {
        if (interaction != null && interaction.getFocusWidth() > 0) return interaction.getFocusWidth();
        return settings == null ? PeakFocusStyle.DEFAULT_WIDTH : settings.getFocus().getWidth();
    }

    public static boolean focusVisible(PeakMenuSettings settings, PeakInteraction interaction) {
        return !focusColor(settings, interaction).isNone() && focusWidth(settings, interaction) > 0;
    }

    public static PeakTooltipStyle tooltip(PeakMenuSettings settings, PeakInteraction interaction) {
        if (interaction != null && interaction.getTooltipStyle() != null) return interaction.getTooltipStyle();
        return settings == null ? new PeakTooltipStyle() : settings.getTooltip();
    }

    public static PeakSoundSpec clickSound(PeakMenuSettings settings, PeakInteraction interaction) {
        if (interaction != null && interaction.isSilent()) return null;
        PeakSoundSpec spec;
        if (interaction != null && interaction.getClickSound() != null) spec = interaction.getClickSound();
        else spec = settings == null ? new PeakSoundSpec() : settings.getClickSound();
        return spec.isNone() ? null : spec;
    }

    public static PeakPaint invalidColor(PeakMenuSettings settings, PeakInteraction interaction) {
        if (interaction != null && !interaction.getInvalidColor().isNone()) return interaction.getInvalidColor();
        return settings == null ? PeakPaint.solid(0xFFFF5555) : settings.getInvalidColor();
    }

    public static PeakScrollbarStyle scrollbar(PeakMenuSettings settings, PeakScrollbarStyle own) {
        return PeakScrollbarStyle.resolve(own, settings == null ? null : settings.getScrollbar());
    }

    public static PeakRect backdropRect(PeakMenuSettings settings, int screenWidth, int screenHeight) {
        int sourceWidth = settings.getBackdropTextureWidth();
        int sourceHeight = settings.getBackdropTextureHeight();
        if (settings.getBackdropFit() == PeakObjectFit.FILL || sourceWidth <= 0 || sourceHeight <= 0) {
            return new PeakRect(0, 0, screenWidth, screenHeight);
        }
        float sourceRatio = (float) sourceWidth / sourceHeight;
        float boxRatio = (float) screenWidth / Math.max(1, screenHeight);
        boolean fitByWidth = settings.getBackdropFit() == PeakObjectFit.CONTAIN
                ? sourceRatio >= boxRatio
                : sourceRatio <= boxRatio;
        int drawWidth;
        int drawHeight;
        if (fitByWidth) {
            drawWidth = screenWidth;
            drawHeight = Math.round(screenWidth / sourceRatio);
        } else {
            drawHeight = screenHeight;
            drawWidth = Math.round(screenHeight * sourceRatio);
        }
        return new PeakRect((screenWidth - drawWidth) / 2, (screenHeight - drawHeight) / 2, drawWidth, drawHeight);
    }

    public static PeakSoundSpec hoverSound(PeakMenuSettings settings, PeakInteraction interaction) {
        if (interaction != null && interaction.isSilent()) return null;
        PeakSoundSpec spec = null;
        if (interaction != null) spec = interaction.getHoverSound();
        if (spec == null && settings != null) spec = settings.getHoverSound();
        return spec == null || spec.isNone() ? null : spec;
    }
}
