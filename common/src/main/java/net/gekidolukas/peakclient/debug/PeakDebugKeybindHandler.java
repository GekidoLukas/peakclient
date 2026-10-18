package net.gekidolukas.peakclient.debug;

import net.minecraft.client.Minecraft;

public final class PeakDebugKeybindHandler {
    private PeakDebugKeybindHandler() {}

    public static void onDebugKeyPressed() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PeakDebugScreen) {
            mc.setScreen(null);
            return;
        }
        if (mc.screen != null) return;
        PeakDebugState.setEnabled(true);
        mc.setScreen(new PeakDebugScreen());
    }
}
