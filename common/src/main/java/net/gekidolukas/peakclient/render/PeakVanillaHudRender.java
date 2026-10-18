package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import net.minecraft.client.gui.GuiGraphics;

import java.util.EnumSet;
import java.util.Set;

public final class PeakVanillaHudRender {
    private static final Set<PeakVanillaElement> PUSHED = EnumSet.noneOf(PeakVanillaElement.class);

    private PeakVanillaHudRender() {}

    public static boolean begin(PeakVanillaElement element, GuiGraphics graphics) {
        PUSHED.remove(element);
        if (!PeakClientVanillaHudState.isEnabled(element)) return false;
        if (!element.offsettable()) return true;
        int dx = PeakClientVanillaHudState.offsetX(element);
        int dy = PeakClientVanillaHudState.offsetY(element);
        if (dx != 0 || dy != 0) {
            graphics.pose().pushPose();
            graphics.pose().translate(dx, dy, 0);
            PUSHED.add(element);
        }
        return true;
    }

    public static void end(PeakVanillaElement element, GuiGraphics graphics) {
        if (PUSHED.remove(element)) graphics.pose().popPose();
    }
}
