package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakAnimState;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakStagger;
import net.gekidolukas.peakclient.ui.PeakStaggerTimeline;
import net.gekidolukas.peakclient.ui.PeakTransitions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class PeakStaggerLayout {
    private PeakStaggerLayout() {}

    static PeakUiRenderer.ChildPainter painter(GuiGraphics graphics, Font font, PeakStagger stagger,
                                               int absX, int absY, float absScale, float scissorUnitFactor,
                                               String documentId, int depth) {
        PeakStaggerTimeline timeline = stagger.timeline();
        double now = PeakClientTickCounter.exact();
        timeline.ensureStarted(now, stagger.isAutoPlay());
        if (stagger.takePlayRequest()) timeline.play(now);
        if (stagger.takeHideRequest()) timeline.hide(now);
        PeakStaggerTimeline.Config config = stagger.timelineConfig();

        return (child, index, count, x, y) -> {
            timeline.finish(now, count, config);
            int rank = stagger.ranksFor(count)[Math.min(index, count - 1)];
            float appearance = timeline.appearance(now, rank, count, config);
            if (appearance <= 0.001f) return;

            PeakPopupAnimation animation = timeline.isExiting() ? stagger.getExitAnimation() : stagger.getEnterAnimation();
            PeakAnimState state = appearance >= 0.999f
                    ? PeakAnimState.VISIBLE
                    : PeakTransitions.sample(animation, appearance, stagger.getSlideDistance());
            if (state.scale() <= 0.001f || state.alpha() <= 0.001f) return;

            PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
            PeakGroupTransform.paint(graphics, x + footprint.width() / 2f, y + footprint.height() / 2f,
                    PeakEffectTransform.of(state),
                    new PeakGroupTransform.ClipSpace(footprint.width(), footprint.height(), absX, absY, absScale, scissorUnitFactor),
                    () -> PeakUiRenderer.renderChild(graphics, font, child, x, y, absX, absY, absScale,
                            scissorUnitFactor, documentId, depth + 1));
        };
    }
}
