package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakId;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class PeakMenuEvents {
    private static final List<Consumer<PeakMenuActionEvent>> ACTION_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PeakMenuNavigationEvent>> NAVIGATION_LISTENERS = new CopyOnWriteArrayList<>();
    private static final Map<String, Map<String, List<Consumer<PeakMenuActionEvent>>>> HANDLERS = new ConcurrentHashMap<>();

    private PeakMenuEvents() {}

    public static void onAction(Consumer<PeakMenuActionEvent> listener) {
        ACTION_LISTENERS.add(listener);
    }

    public static void onNavigation(Consumer<PeakMenuNavigationEvent> listener) {
        NAVIGATION_LISTENERS.add(listener);
    }

    public static void on(String menuId, String actionId, Consumer<PeakMenuActionEvent> handler) {
        PeakId.require(menuId, "Menu");
        PeakId.require(actionId, "Action");
        HANDLERS.computeIfAbsent(menuId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(actionId, id -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    public static void set(String menuId, String actionId, Consumer<PeakMenuActionEvent> handler) {
        PeakId.require(menuId, "Menu");
        PeakId.require(actionId, "Action");
        List<Consumer<PeakMenuActionEvent>> handlers = HANDLERS.computeIfAbsent(menuId, id -> new ConcurrentHashMap<>())
                .computeIfAbsent(actionId, id -> new CopyOnWriteArrayList<>());
        synchronized (handlers) {
            handlers.clear();
            handlers.add(handler);
        }
    }

    static void dispatchAction(PeakMenuActionEvent event) {
        Map<String, List<Consumer<PeakMenuActionEvent>>> forMenu = HANDLERS.get(event.menuId());
        if (forMenu != null) {
            List<Consumer<PeakMenuActionEvent>> handlers = forMenu.get(event.actionId());
            if (handlers != null) for (Consumer<PeakMenuActionEvent> handler : handlers) call(handler, event);
        }
        for (Consumer<PeakMenuActionEvent> listener : ACTION_LISTENERS) call(listener, event);
    }

    static void dispatchNavigation(PeakMenuNavigationEvent event) {
        for (Consumer<PeakMenuNavigationEvent> listener : NAVIGATION_LISTENERS) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                PeakLog.error("A menu navigation listener failed", e);
            }
        }
    }

    private static void call(Consumer<PeakMenuActionEvent> handler, PeakMenuActionEvent event) {
        try {
            handler.accept(event);
        } catch (Exception e) {
            PeakLog.error("A menu action handler failed for '" + event.menuId() + "/" + event.actionId() + "'", e);
        }
    }

    public static void clearHandlers() {
        ACTION_LISTENERS.clear();
        NAVIGATION_LISTENERS.clear();
        HANDLERS.clear();
    }
}
