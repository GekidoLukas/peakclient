package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakFlow;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.gui.Font;

public final class PeakFlowRenderer extends PeakContainerRenderer<PeakFlow> {
    @Override
    public PeakPixelSize measureContent(Font font, PeakFlow flow) {
        return PeakFlowLayout.measure(font, flow);
    }
}
