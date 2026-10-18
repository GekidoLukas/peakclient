package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.client.PeakClientMenus;
import net.gekidolukas.peakclient.ui.serialize.PeakItemStackAdapter;
import net.minecraft.client.Minecraft;

public class PeakClientTickCounter {
    private static volatile long ticks = 0;
    private static double lastExact = 0;

    static {
        PeakItemStackAdapter.useClientProvider(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft == null || minecraft.level == null ? null : minecraft.level.registryAccess();
        });
    }

    private PeakClientTickCounter() {}

    public static void tick() {
        ticks++;
        PeakClientMenus.tick();
    }
    public static long current() { return ticks; }

    public static double exact() {
        Minecraft minecraft = Minecraft.getInstance();
        double partial = minecraft == null ? 0.0 : minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        double value = ticks + Math.max(0.0, Math.min(1.0, partial));
        if (value < lastExact) value = lastExact;
        lastExact = value;
        return value;
    }

    public static void reset() {
        ticks = 0;
        lastExact = 0;
    }
}
