package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakOverflow;

import java.util.ArrayList;
import java.util.List;

public final class PeakMenuScrollKeys {
    public enum Command { LINE_BACK, LINE_FORWARD, PAGE_BACK, PAGE_FORWARD, START, END }

    public enum Axis { VERTICAL, HORIZONTAL, ANY }

    public record Step(String containerId, int delta) {}

    static final int EDGE_DELTA = 1_000_000;

    private PeakMenuScrollKeys() {}

    public static Step plan(PeakLayoutProbe probe, PeakLayoutEntry focus, PeakLayoutEntry mouse,
                            PeakMenuScrollState scroll, Command command, Axis axis) {
        boolean forward = command == Command.LINE_FORWARD || command == Command.PAGE_FORWARD || command == Command.END;

        for (PeakLayoutEntry candidate : candidates(probe, focus, mouse)) {
            PeakContainer<?> container = scrollContainer(candidate);
            if (container == null) continue;
            boolean vertical = isVertical(container);
            if (axis == Axis.VERTICAL && !vertical) continue;
            if (axis == Axis.HORIZONTAL && vertical) continue;

            String id = container.getId();
            int max = scroll.max(id);
            int offset = scroll.offset(id);
            if (forward ? offset >= max : offset <= 0) continue;

            int delta = switch (command) {
                case LINE_BACK -> -PeakMenuScrollState.WHEEL_STEP;
                case LINE_FORWARD -> PeakMenuScrollState.WHEEL_STEP;
                case PAGE_BACK -> -pageSize(candidate, container, vertical);
                case PAGE_FORWARD -> pageSize(candidate, container, vertical);
                case START -> -EDGE_DELTA;
                case END -> EDGE_DELTA;
            };
            return new Step(id, delta);
        }
        return null;
    }

    private static List<PeakLayoutEntry> candidates(PeakLayoutProbe probe, PeakLayoutEntry focus, PeakLayoutEntry mouse) {
        List<PeakLayoutEntry> result = new ArrayList<>();
        addChain(probe, focus, result);
        addChain(probe, mouse, result);

        List<PeakLayoutEntry> all = new ArrayList<>();
        for (PeakLayoutEntry entry : probe.entries()) {
            if (!entry.invisible() && scrollContainer(entry) != null) all.add(entry);
        }
        if (all.size() == 1 && !result.contains(all.get(0))) result.add(all.get(0));
        return result;
    }

    private static void addChain(PeakLayoutProbe probe, PeakLayoutEntry start, List<PeakLayoutEntry> into) {
        for (PeakLayoutEntry entry = start; entry != null; entry = probe.parentOf(entry)) {
            if (!into.contains(entry)) into.add(entry);
        }
    }

    private static PeakContainer<?> scrollContainer(PeakLayoutEntry entry) {
        if (entry.element() instanceof PeakContainer<?> container && container.getOverflow() == PeakOverflow.SCROLL) {
            return container;
        }
        return null;
    }

    private static boolean isVertical(PeakContainer<?> container) {
        return container.scrollsVertically();
    }

    private static int pageSize(PeakLayoutEntry entry, PeakContainer<?> container, boolean vertical) {
        PeakMenuFocus.InnerBox inner = PeakMenuFocus.innerBox(entry, container);
        int screenSize = vertical ? inner.height() : inner.width();
        float scale = entry.absScale() > 0f ? entry.absScale() : 1f;
        int local = Math.round(screenSize / scale);
        return Math.max(PeakMenuScrollState.WHEEL_STEP, local - PeakMenuScrollState.WHEEL_STEP);
    }
}
