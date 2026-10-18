package net.gekidolukas.peakclient.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

final class PeakEdgeFade {
    private PeakEdgeFade() {}

    static void finish(Minecraft minecraft, GuiGraphics graphics, int absX, int absY, float absScale, float scissorUnitFactor,
                       int contentX, int contentY, int contentWidth, int contentHeight,
                       int fadeStartPixels, int fadeEndPixels, boolean horizontal) {
        float unit = absScale * scissorUnitFactor;
        float x1 = (absX + contentX * absScale) * scissorUnitFactor;
        float y1 = (absY + contentY * absScale) * scissorUnitFactor;
        float x2 = x1 + contentWidth * unit;
        float y2 = y1 + contentHeight * unit;
        PeakUiRenderer.ClipBounds clip = PeakUiRenderer.currentClip();
        float maskX1 = Math.max(x1, clip.left());
        float maskY1 = Math.max(y1, clip.top());
        float maskX2 = Math.min(x2, clip.right());
        float maskY2 = Math.min(y2, clip.bottom());
        float fadeStart = fadeStartPixels * unit - (horizontal ? maskX1 - x1 : maskY1 - y1);
        float fadeEnd = fadeEndPixels * unit - (horizontal ? x2 - maskX2 : y2 - maskY2);
        PeakOffscreenOpacity.endMasked(minecraft, graphics, maskX1, maskY1, maskX2, maskY2,
                Math.max(0f, fadeStart), Math.max(0f, fadeEnd), !horizontal);
    }
}
