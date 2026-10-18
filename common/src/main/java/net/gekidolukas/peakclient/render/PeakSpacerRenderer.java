package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakSpacer;
import net.minecraft.client.gui.Font;

public final class PeakSpacerRenderer implements PeakElementRenderer<PeakSpacer> {
    @Override public void renderContent(PeakRenderContext ctx, PeakSpacer element, int width, int height) {  }
    @Override public PeakPixelSize measureContent(Font font, PeakSpacer element) { return PeakPixelSize.ZERO; }
}
