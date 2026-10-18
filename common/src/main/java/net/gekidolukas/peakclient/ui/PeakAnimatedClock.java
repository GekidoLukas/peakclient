package net.gekidolukas.peakclient.ui;

public final class PeakAnimatedClock {
    public record Config(PeakAnimationEffect effect, double periodTicks, float amplitude, double delayTicks, boolean playing,
                         int color) {
        public Config(PeakAnimationEffect effect, double periodTicks, float amplitude, double delayTicks, boolean playing) {
            this(effect, periodTicks, amplitude, delayTicks, playing, 0);
        }
    }

    private boolean started;
    private double origin;
    private boolean paused;
    private double pausedAt;

    private boolean queued;
    private PeakAnimationEffect queuedEffect = PeakAnimationEffect.NONE;
    private double queuedTicks;
    private float queuedIntensity;
    private int queuedColor;

    private PeakAnimationEffect shotEffect = PeakAnimationEffect.NONE;
    private double shotStart;
    private double shotTicks;
    private float shotIntensity;
    private int shotColor;

    public void reset() {
        started = false;
        paused = false;
        queued = false;
        shotEffect = PeakAnimationEffect.NONE;
    }

    public void trigger(PeakAnimationEffect effect, double ticks, float intensity) {
        trigger(effect, ticks, intensity, 0);
    }

    public void trigger(PeakAnimationEffect effect, double ticks, float intensity, int color) {
        queued = true;
        queuedEffect = effect;
        queuedTicks = Math.max(1.0, ticks);
        queuedIntensity = intensity;
        queuedColor = color;
    }

    public PeakEffectTransform transform(Config config, double now) {
        if (!started) {
            started = true;
            origin = now;
        }

        double effective;
        if (!config.playing()) {
            if (!paused) {
                paused = true;
                pausedAt = now;
            }
            effective = pausedAt - origin;
        } else {
            if (paused) {
                origin += now - pausedAt;
                paused = false;
            }
            effective = now - origin;
        }

        PeakEffectTransform result = PeakEffectTransform.IDENTITY;
        if (config.effect() != PeakAnimationEffect.NONE && effective >= config.delayTicks()) {
            result = PeakEffects.loop(config.effect(), effective - config.delayTicks(), config.periodTicks(), config.amplitude(),
                    config.color());
        }

        if (queued) {
            queued = false;
            shotEffect = queuedEffect;
            shotStart = now;
            shotTicks = queuedTicks;
            shotIntensity = queuedIntensity;
            shotColor = queuedColor;
        }
        if (shotEffect != PeakAnimationEffect.NONE) {
            double progress = (now - shotStart) / shotTicks;
            if (progress >= 1.0) {
                shotEffect = PeakAnimationEffect.NONE;
            } else {
                int color = shotColor != 0 ? shotColor : config.color();
                result = result.then(PeakEffects.oneShot(shotEffect, (float) progress, config.amplitude(), shotIntensity, color));
            }
        }
        return result;
    }
}
