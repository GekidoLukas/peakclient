package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.gui.Font;

public interface PeakElementRenderer<T extends PeakElement<?>> {

    void renderContent(PeakRenderContext ctx, T element, int width, int height);

    PeakPixelSize measureContent(Font font, T element);

    default boolean writesDepth() { return false; }
}
