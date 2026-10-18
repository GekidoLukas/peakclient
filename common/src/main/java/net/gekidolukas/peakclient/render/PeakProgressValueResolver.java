package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakAbstractProgress;
import net.gekidolukas.peakclient.ui.PeakIconBar;
import net.gekidolukas.peakclient.ui.PeakProgressSource;
import net.minecraft.client.Minecraft;

public final class PeakProgressValueResolver {
    private PeakProgressValueResolver() {}

    public static PeakProgressValues vanillaValues(PeakProgressSource source, Minecraft mc) {
        if (mc.player == null) return new PeakProgressValues(0, 1);
        return switch (source) {
            case HEALTH -> new PeakProgressValues(mc.player.getHealth(), mc.player.getMaxHealth());
            case ARMOR -> new PeakProgressValues(mc.player.getArmorValue(), 20);
            case HUNGER -> new PeakProgressValues(mc.player.getFoodData().getFoodLevel(), 20);
            case SATURATION -> new PeakProgressValues(mc.player.getFoodData().getSaturationLevel(), 20);
            case EXPERIENCE_PROGRESS -> new PeakProgressValues(mc.player.experienceProgress, 1);
            case EXPERIENCE_LEVEL -> new PeakProgressValues(mc.player.experienceLevel, 100);
            case AIR -> new PeakProgressValues(mc.player.getAirSupply(), mc.player.getMaxAirSupply());
            default -> new PeakProgressValues(0, 1);
        };
    }

    public static PeakProgressValues resolve(PeakAbstractProgress<?> progress) {
        if (progress.getSource() == PeakProgressSource.MANUAL) {
            return new PeakProgressValues(progress.manualCurrentValue(), progress.getMaxValue());
        }
        return vanillaValues(progress.getSource(), Minecraft.getInstance());
    }

    public static PeakProgressValues resolveIconBar(PeakIconBar bar, Minecraft mc) {
        if (bar.getSource() != PeakProgressSource.MANUAL) return vanillaValues(bar.getSource(), mc);
        return new PeakProgressValues(bar.getValue(), bar.getMaxValue());
    }
}
