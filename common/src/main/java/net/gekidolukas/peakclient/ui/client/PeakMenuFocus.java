package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakStyle;

import java.util.ArrayList;
import java.util.List;

public final class PeakMenuFocus {
    public record Scroll(String containerId, int delta) {}

    public record Plan(boolean reachable, List<Scroll> scrolls) {}

    record InnerBox(int left, int top, int right, int bottom) {
        int width() { return right - left; }

        int height() { return bottom - top; }
    }

    private PeakMenuFocus() {}

    public static Plan plan(PeakLayoutProbe probe, PeakLayoutEntry target) {
        List<Scroll> scrolls = new ArrayList<>();
        if (target.invisible()) return new Plan(false, scrolls);

        int left = target.absX();
        int top = target.absY();
        int right = left + target.width();
        int bottom = top + target.height();

        for (PeakLayoutEntry ancestor = probe.parentOf(target); ancestor != null; ancestor = probe.parentOf(ancestor)) {
            if (!(ancestor.element() instanceof PeakContainer<?> container)) continue;
            PeakOverflow overflow = container.getOverflow();

            if (overflow == PeakOverflow.HIDDEN) {
                int centerX = (left + right) / 2;
                int centerY = (top + bottom) / 2;
                if (centerX < ancestor.absX() || centerX > ancestor.absX() + ancestor.width()
                        || centerY < ancestor.absY() || centerY > ancestor.absY() + ancestor.height()) {
                    return new Plan(false, scrolls);
                }
            } else if (overflow == PeakOverflow.SCROLL) {
                InnerBox inner = innerBox(ancestor, container);
                boolean vertical = container.scrollsVertically();
                int delta;
                if (vertical) {
                    delta = delta(top, bottom, inner.top(), inner.bottom());
                    top -= delta;
                    bottom -= delta;
                } else {
                    delta = delta(left, right, inner.left(), inner.right());
                    left -= delta;
                    right -= delta;
                }
                if (delta != 0) scrolls.add(new Scroll(container.getId(), delta));
            }
        }
        return new Plan(true, scrolls);
    }

    public static boolean isReachable(PeakLayoutProbe probe, PeakLayoutEntry target) {
        return plan(probe, target).reachable();
    }

    static int delta(int start, int end, int viewStart, int viewEnd) {
        if (end - start > viewEnd - viewStart) return start - viewStart;
        if (start < viewStart) return start - viewStart;
        if (end > viewEnd) return end - viewEnd;
        return 0;
    }

    static InnerBox innerBox(PeakLayoutEntry entry, PeakContainer<?> container) {
        PeakStyle style = container.getStyle();
        PeakInsets padding = style.getPadding();
        boolean hasBorder = style.getBorderWidth() > 0 && !style.getBorderColor().isNone();
        int border = hasBorder ? Math.min(style.getBorderWidth(), Math.min(entry.localWidth(), entry.localHeight()) / 2) : 0;

        float perUnit = 1f;
        if (entry.localWidth() > 0) perUnit = entry.width() / (float) entry.localWidth();
        else if (entry.localHeight() > 0) perUnit = entry.height() / (float) entry.localHeight();

        int left = entry.absX() + Math.round(Math.max(padding.getLeft(), border) * perUnit);
        int top = entry.absY() + Math.round(Math.max(padding.getTop(), border) * perUnit);
        int right = entry.absX() + entry.width() - Math.round(Math.max(padding.getRight(), border) * perUnit);
        int bottom = entry.absY() + entry.height() - Math.round(Math.max(padding.getBottom(), border) * perUnit);
        return new InnerBox(left, top, right, bottom);
    }
}
