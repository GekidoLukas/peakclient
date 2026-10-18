package net.gekidolukas.peakclient.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public record PeakRenderContext(GuiGraphics graphics, Font font, int absX, int absY, float absScale,
                                float scissorUnitFactor, String documentId, int depth) {}