package net.gekidolukas.peakclient.ui;

public final class PeakTransitions {
    private PeakTransitions() {}

    public static PeakAnimState sample(PeakPopupAnimation animation, float progress, int slideDistance) {
        float t = Math.max(0f, Math.min(1f, progress));
        return switch (animation) {
            case SLIDE_FROM_TOP -> new PeakAnimState(false, 0, -slideDistance * (1 - t), 1f, 1f);
            case SLIDE_FROM_BOTTOM -> new PeakAnimState(false, 0, slideDistance * (1 - t), 1f, 1f);
            case SLIDE_FROM_LEFT -> new PeakAnimState(false, -slideDistance * (1 - t), 0, 1f, 1f);
            case SLIDE_FROM_RIGHT -> new PeakAnimState(false, slideDistance * (1 - t), 0, 1f, 1f);
            case SCALE -> new PeakAnimState(false, 0, 0, t, 1f);
            case FADE -> new PeakAnimState(false, 0, 0, 1f, t);
            case WIPE_FROM_LEFT -> revealed(PeakRevealState.clipped(0f, 0f, t, 1f));
            case WIPE_FROM_RIGHT -> revealed(PeakRevealState.clipped(1f - t, 0f, 1f, 1f));
            case WIPE_FROM_TOP -> revealed(PeakRevealState.clipped(0f, 0f, 1f, t));
            case WIPE_FROM_BOTTOM -> revealed(PeakRevealState.clipped(0f, 1f - t, 1f, 1f));
            case FLIP_HORIZONTAL -> revealed(PeakRevealState.scaled(t, 1f));
            case FLIP_VERTICAL -> revealed(PeakRevealState.scaled(1f, t));
            case NONE -> PeakAnimState.VISIBLE;
        };
    }

    private static PeakAnimState revealed(PeakRevealState reveal) {
        return new PeakAnimState(false, 0, 0, 1f, 1f, reveal);
    }
}
