package net.gekidolukas.peakclient.ui;

public final class PeakEffects {
    private static final double TAU = Math.PI * 2.0;
    private static final double SHINE_ACTIVE_SHARE = 0.45;

    private PeakEffects() {}

    public static float amplitudeOf(PeakAnimationEffect effect, float amplitude) {
        return amplitude > 0f ? amplitude : effect.getDefaultAmplitude();
    }

    public static int defaultColor(PeakAnimationEffect effect) {
        return effect == PeakAnimationEffect.GLOW ? 0xFFFFD700 : 0xFFFFFFFF;
    }

    public static PeakEffectTransform loop(PeakAnimationEffect effect, double ticks, double periodTicks, float amplitude) {
        return loop(effect, ticks, periodTicks, amplitude, 0);
    }

    public static PeakEffectTransform loop(PeakAnimationEffect effect, double ticks, double periodTicks, float amplitude, int color) {
        if (effect == PeakAnimationEffect.NONE) return PeakEffectTransform.IDENTITY;
        float amp = amplitudeOf(effect, amplitude);
        double cycles = periodTicks <= 0.0 ? 0.0 : ticks / periodTicks;
        double phase = cycles - Math.floor(cycles);
        double wave = Math.sin(TAU * phase);
        double rise = 0.5 - 0.5 * Math.cos(TAU * phase);
        return switch (effect) {
            case PULSE -> new PeakEffectTransform(0f, 0f, (float) (1.0 + amp * rise), 0f, 1f);
            case BOB -> new PeakEffectTransform(0f, (float) (-amp * wave), 1f, 0f, 1f);
            case SWAY -> new PeakEffectTransform((float) (amp * wave), 0f, 1f, 0f, 1f);
            case WOBBLE -> new PeakEffectTransform(0f, 0f, 1f, (float) (amp * wave), 1f);
            case SPIN -> new PeakEffectTransform(0f, 0f, 1f, (float) (360.0 * phase), 1f);
            case BLINK -> new PeakEffectTransform(0f, 0f, 1f, 0f, (float) (1.0 - Math.min(1f, amp) * rise));
            case SHAKE -> shake(ticks, amp, 1f);
            case POP -> new PeakEffectTransform(0f, 0f, (float) (1.0 + amp * bump(phase)), 0f, 1f);
            case FLASH -> overlay(PeakEffectOverlay.Kind.FLASH, effect, color, (float) (amp * rise), (float) phase);
            case GLOW -> overlay(PeakEffectOverlay.Kind.GLOW, effect, color, (float) (amp * rise), (float) phase);
            case OUTLINE -> overlay(PeakEffectOverlay.Kind.OUTLINE, effect, color, (float) (amp * rise), (float) phase);
            case SHINE -> phase < SHINE_ACTIVE_SHARE
                    ? overlay(PeakEffectOverlay.Kind.SHINE, effect, color, amp, (float) (phase / SHINE_ACTIVE_SHARE))
                    : PeakEffectTransform.IDENTITY;
            case NONE -> PeakEffectTransform.IDENTITY;
        };
    }

    public static PeakEffectTransform oneShot(PeakAnimationEffect effect, float progress, float amplitude, float intensity) {
        return oneShot(effect, progress, amplitude, intensity, 0);
    }

    public static PeakEffectTransform oneShot(PeakAnimationEffect effect, float progress, float amplitude, float intensity, int color) {
        if (effect == PeakAnimationEffect.NONE) return PeakEffectTransform.IDENTITY;
        float p = Math.max(0f, Math.min(1f, progress));
        float amp = amplitudeOf(effect, amplitude) * Math.max(0f, intensity);
        float decay = 1f - p;
        return switch (effect) {
            case PULSE -> new PeakEffectTransform(0f, 0f, (float) (1.0 + amp * Math.sin(Math.PI * p)), 0f, 1f);
            case BOB -> new PeakEffectTransform(0f, (float) (-amp * Math.sin(Math.PI * p)), 1f, 0f, 1f);
            case SWAY -> new PeakEffectTransform((float) (amp * Math.sin(TAU * p)) * decay, 0f, 1f, 0f, 1f);
            case WOBBLE -> new PeakEffectTransform(0f, 0f, 1f, (float) (amp * Math.sin(2.0 * TAU * p)) * decay, 1f);
            case SPIN -> new PeakEffectTransform(0f, 0f, 1f, 360f * ease(p), 1f);
            case BLINK -> new PeakEffectTransform(0f, 0f, 1f, 0f, (float) (1.0 - Math.min(1f, amp) * Math.sin(Math.PI * p)));
            case SHAKE -> shake(p * 40.0, amp, decay * Math.min(1f, p * 6f));
            case POP -> new PeakEffectTransform(0f, 0f, (float) (1.0 + amp * bump(p)), 0f, 1f);
            case FLASH -> overlay(PeakEffectOverlay.Kind.FLASH, effect, color, (float) (amp * Math.pow(decay, 1.5)), p);
            case GLOW -> overlay(PeakEffectOverlay.Kind.GLOW, effect, color, (float) (amp * Math.sin(Math.PI * p)), p);
            case OUTLINE -> overlay(PeakEffectOverlay.Kind.OUTLINE, effect, color, amp * decay, p);
            case SHINE -> overlay(PeakEffectOverlay.Kind.SHINE, effect, color, p < 1f ? amp : 0f, p);
            case NONE -> PeakEffectTransform.IDENTITY;
        };
    }

    private static PeakEffectTransform overlay(PeakEffectOverlay.Kind kind, PeakAnimationEffect effect, int color,
                                               float strength, float progress) {
        int resolved = color == 0 ? defaultColor(effect) : PeakArgb.normalize(color);
        PeakEffectOverlay overlay = new PeakEffectOverlay(kind, resolved, Math.max(0f, Math.min(1f, strength)), progress);
        return PeakEffectTransform.IDENTITY.withOverlay(overlay);
    }

    private static double bump(double progress) {
        return Math.sin(Math.PI * Math.pow(Math.max(0.0, Math.min(1.0, progress)), 0.55));
    }

    private static PeakEffectTransform shake(double ticks, float amplitude, float envelope) {
        double x = Math.sin(ticks * 2.1) + 0.6 * Math.sin(ticks * 3.7 + 1.3);
        double y = Math.cos(ticks * 2.9 + 0.4) + 0.6 * Math.sin(ticks * 4.3 + 2.1);
        float scale = amplitude * envelope / 1.6f;
        return new PeakEffectTransform((float) x * scale, (float) y * scale, 1f, 0f, 1f);
    }

    private static float ease(float t) {
        return t * t * (3f - 2f * t);
    }
}
