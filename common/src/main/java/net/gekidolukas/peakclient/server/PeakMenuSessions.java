package net.gekidolukas.peakclient.server;

import com.google.gson.reflect.TypeToken;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.network.PeakMenuEventPayload;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakMenuSessions {
    private static final Type ARGUMENTS_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final Type HISTORY_TYPE = new TypeToken<List<String>>() {}.getType();

    private static final Map<UUID, PeakMenuSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private PeakMenuSessions() {}

    static PeakMenuSession session(UUID playerId) {
        return SESSIONS.computeIfAbsent(playerId, id -> new PeakMenuSession());
    }

    public static void markSent(UUID playerId, String menuId) {
        session(playerId).markKnown(menuId);
    }

    public static void markRemoved(UUID playerId, String menuId) {
        PeakMenuSession session = SESSIONS.get(playerId);
        if (session != null) session.markRemoved(menuId);
    }

    public static void markCleared(UUID playerId) {
        PeakMenuSession session = SESSIONS.get(playerId);
        if (session != null) session.markCleared();
    }

    public static boolean isKnown(UUID playerId, String menuId) {
        PeakMenuSession session = SESSIONS.get(playerId);
        return session != null && session.isKnown(menuId);
    }

    public static String current(UUID playerId) {
        PeakMenuSession session = SESSIONS.get(playerId);
        return session == null ? null : session.current();
    }

    public static List<String> history(UUID playerId) {
        PeakMenuSession session = SESSIONS.get(playerId);
        return session == null ? List.of() : session.history();
    }

    public static void onEvent(ServerPlayer player, PeakMenuEventPayload payload) {
        UUID id = player.getUUID();
        switch (payload.kind()) {
            case ACTION -> onAction(player, id, payload);
            case NAVIGATED -> onNavigated(player, id, payload);
            case HUD_ACTION -> onDocumentAction(player, id, payload);
        }
    }

    private static void onAction(ServerPlayer player, UUID id, PeakMenuEventPayload payload) {
        Map<String, String> args = parseArguments(payload.argsJson());
        if (args == null) {
            warnOnce(id, "malformed", "Ignored a menu action from {}: its arguments are malformed", player);
            return;
        }
        PeakMenuSession.Result result = session(id).checkAction(payload.menuId(), payload.elementId(),
                payload.actionId(), args, System.currentTimeMillis());
        switch (result) {
            case ACCEPTED -> PeakMenuEvents.dispatchAction(new PeakMenuActionEvent(player, payload.menuId(),
                    payload.elementId(), payload.actionId(), Map.copyOf(args)));
            case UNKNOWN_MENU -> warnOnce(id, "unknown", "Ignored a menu action from {}: the menu was never sent to this player", player);
            case INVALID -> warnOnce(id, "invalid", "Ignored a menu action from {}: its fields are invalid or too long", player);
            case RATE_LIMITED -> warnOnce(id, "rate", "Ignored menu actions from {}: more than "
                    + PeakMenuSession.MAX_EVENTS_PER_SECOND + " per second", player);
        }
    }

    private static void onDocumentAction(ServerPlayer player, UUID id, PeakMenuEventPayload payload) {
        Map<String, String> args = parseArguments(payload.argsJson());
        if (args == null) {
            warnOnce(id, "malformed", "Ignored a document action from {}: its arguments are malformed", player);
            return;
        }
        boolean sent = PeakSentDocuments.knownDocument(List.of(id), payload.menuId()) != null;
        PeakMenuSession.Result result = session(id).checkDocumentAction(payload.menuId(), payload.elementId(),
                payload.actionId(), args, System.currentTimeMillis(), sent);
        switch (result) {
            case ACCEPTED -> PeakDocumentEvents.dispatchAction(new PeakDocumentActionEvent(player, payload.menuId(),
                    payload.elementId(), payload.actionId(), Map.copyOf(args)));
            case UNKNOWN_MENU -> warnOnce(id, "unknown-document", "Ignored a document action from {}: the document was never sent to this player", player);
            case INVALID -> warnOnce(id, "invalid", "Ignored a document action from {}: its fields are invalid or too long", player);
            case RATE_LIMITED -> warnOnce(id, "rate", "Ignored actions from {}: more than "
                    + PeakMenuSession.MAX_EVENTS_PER_SECOND + " per second", player);
        }
    }

    private static void onNavigated(ServerPlayer player, UUID id, PeakMenuEventPayload payload) {
        List<String> history;
        try {
            history = PeakUiSerializer.GSON.fromJson(payload.argsJson(), HISTORY_TYPE);
        } catch (RuntimeException e) {
            history = null;
        }
        if (history == null) history = List.of();
        PeakMenuSession session = session(id);
        session.navigated(payload.menuId(), history);
        PeakMenuEvents.dispatchNavigation(new PeakMenuNavigationEvent(player, session.current(), session.history(), payload.actionId()));
    }

    private static Map<String, String> parseArguments(String json) {
        if (json == null || json.isEmpty()) return new LinkedHashMap<>();
        try {
            Map<String, String> parsed = PeakUiSerializer.GSON.fromJson(json, ARGUMENTS_TYPE);
            return parsed == null ? new LinkedHashMap<>() : parsed;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void warnOnce(UUID id, String reason, String message, ServerPlayer player) {
        if (WARNED.add(id + ":" + reason)) PeakLog.warn(message, player.getGameProfile().getName());
    }

    public static void forget(UUID playerId) {
        SESSIONS.remove(playerId);
        String prefix = playerId + ":";
        WARNED.removeIf(key -> key.startsWith(prefix));
    }

    public static void clearAll() {
        SESSIONS.clear();
        WARNED.clear();
    }
}
