package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakElement;

import java.util.ArrayList;
import java.util.List;

final class PeakMenuFocusNavigation {
    private final PeakMenuContext context;
    private final PeakInputControllers controllers;

    PeakMenuFocusNavigation(PeakMenuContext context, PeakInputControllers controllers) {
        this.context = context;
        this.controllers = controllers;
    }

    void move(boolean backwards) {
        PeakLayoutProbe probe = context.probe();
        List<PeakElement<?>> focusable = new ArrayList<>();
        for (PeakLayoutEntry entry : probe.entries()) {
            PeakElement<?> element = entry.element();
            if (entry.invisible() || element.getInteraction() == null || !element.getInteraction().isEnabled()) continue;
            if (!PeakMenuFocus.isReachable(probe, entry)) continue;
            if (!focusable.contains(element)) focusable.add(element);
        }
        if (focusable.isEmpty()) return;

        PeakElement<?> current = context.focusedElement();
        int index = current == null ? -1 : focusable.indexOf(current);
        int next;
        if (index < 0) next = backwards ? focusable.size() - 1 : 0;
        else next = Math.floorMod(index + (backwards ? -1 : 1), focusable.size());

        PeakElement<?> target = focusable.get(next);
        context.input().focus(target.getId());
        controllers.of(target).focusedByTab(target);
        reveal(target);
    }

    private void reveal(PeakElement<?> target) {
        PeakLayoutEntry entry = context.entryOf(target);
        if (entry == null) return;
        PeakMenuScrollState scroll = PeakClientMenus.scrollState(context.menuId());
        for (PeakMenuFocus.Scroll step : PeakMenuFocus.plan(context.probe(), entry).scrolls()) {
            scroll.scrollBy(step.containerId(), step.delta());
        }
    }
}
