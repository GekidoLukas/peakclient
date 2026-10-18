package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakAnimated;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;

public final class PeakAnimatedRenderer extends PeakContainerRenderer<PeakAnimated> {
    @Override
    public void renderContent(PeakRenderContext ctx, PeakAnimated animated, int width, int height) {
        PeakEffectTransform transform = animated.clock().transform(animated.clockConfig(), PeakClientTickCounter.exact());
        int radius = PeakInteractionStyles.resolve(animated).getBorderRadius();
        PeakGroupTransform.paintWithOverlay(ctx.graphics(), width, height, radius, transform,
                () -> super.renderContent(ctx, animated, width, height));
    }
}
