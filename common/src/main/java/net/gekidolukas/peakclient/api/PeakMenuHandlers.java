package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.server.PeakMenuEvents;

import java.util.function.Consumer;

public final class PeakMenuHandlers {
    private final String menuId;

    PeakMenuHandlers(String menuId) {
        this.menuId = menuId;
    }

    public PeakMenuHandlers on(String actionId, Consumer<PeakMenuActionEvent> handler) {
        PeakMenuEvents.on(menuId, actionId, handler);
        return this;
    }
}
