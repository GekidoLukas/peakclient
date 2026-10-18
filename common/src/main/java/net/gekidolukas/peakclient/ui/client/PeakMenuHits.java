package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakStyle;

public final class PeakMenuHits {
    private PeakMenuHits() {}

    public static PeakLayoutEntry interactiveAt(PeakLayoutProbe probe, int x, int y) {
        PeakLayoutEntry top = probe.findAt(x, y, entry -> !isPassThrough(entry.element()));
        for (PeakLayoutEntry entry = top; entry != null; entry = probe.parentOf(entry)) {
            if (entry.element().getInteraction() != null) return entry;
        }
        return null;
    }

    public static boolean isPassThrough(PeakElement<?> element) {
        if (element.getInteraction() != null) return false;
        if (!(element instanceof PeakContainer<?>)) return false;
        PeakStyle style = element.getStyle();
        boolean hasBackground = !style.getBackground().isNone();
        boolean hasBorder = style.getBorderWidth() > 0 && !style.getBorderColor().isNone();
        return !hasBackground && !hasBorder;
    }
}
