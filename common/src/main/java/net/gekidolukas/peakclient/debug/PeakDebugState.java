package net.gekidolukas.peakclient.debug;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;

import java.util.List;

public final class PeakDebugState {
    private static volatile boolean enabled = false;
    private static PeakLayoutProbe currentProbe = new PeakLayoutProbe();
    private static PeakLayoutProbe lastProbe = new PeakLayoutProbe();
    private static PeakLayoutProbe menuProbe;
    private static String baseKey;
    private static int levelsUp;

    private PeakDebugState() {}

    public static boolean isEnabled() { return enabled; }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) {
            resetSelection();
            menuProbe = null;
        }
    }

    public static void beginFrame() {
        if (enabled) currentProbe = new PeakLayoutProbe();
    }

    public static void endFrame() {
        if (enabled) lastProbe = currentProbe;
    }

    public static PeakLayoutProbe currentProbe() {
        return currentProbe;
    }

    public static void useMenuProbe(PeakLayoutProbe probe) {
        menuProbe = probe;
    }

    private static PeakLayoutProbe shown() {
        return menuProbe != null ? menuProbe : lastProbe;
    }

    public static List<PeakLayoutEntry> getEntries() {
        return shown().entries();
    }

    public static PeakLayoutEntry findAt(int mouseX, int mouseY) {
        return shown().findAt(mouseX, mouseY);
    }

    public static PeakLayoutEntry parentOf(PeakLayoutEntry entry) {
        return shown().parentOf(entry);
    }

    public static List<PeakLayoutEntry> chainOf(PeakLayoutEntry entry) {
        return shown().chainOf(entry);
    }

    public static PeakLayoutEntry resolveSelected(int mouseX, int mouseY) {
        PeakLayoutEntry base = findAt(mouseX, mouseY);
        if (base == null) {
            resetSelection();
            return null;
        }
        if (!base.key().equals(baseKey)) {
            baseKey = base.key();
            levelsUp = 0;
        }
        int available = chainOf(base).size() - 1;
        levelsUp = Math.min(levelsUp, available);
        PeakLayoutEntry selected = base;
        for (int i = 0; i < levelsUp; i++) {
            PeakLayoutEntry parent = parentOf(selected);
            if (parent == null) break;
            selected = parent;
        }
        return selected;
    }

    public static PeakLayoutEntry baseAt(int mouseX, int mouseY) {
        return findAt(mouseX, mouseY);
    }

    public static int levelsUp() { return levelsUp; }

    public static void scroll(int mouseX, int mouseY, double amount) {
        PeakLayoutEntry base = findAt(mouseX, mouseY);
        if (base == null || amount == 0) return;
        if (!base.key().equals(baseKey)) {
            baseKey = base.key();
            levelsUp = 0;
        }
        int available = chainOf(base).size() - 1;
        levelsUp = Math.max(0, Math.min(available, levelsUp + (amount > 0 ? 1 : -1)));
    }

    private static void resetSelection() {
        baseKey = null;
        levelsUp = 0;
    }
}
