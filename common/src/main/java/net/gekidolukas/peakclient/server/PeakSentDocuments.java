package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.ui.PeakDocument;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakSentDocuments {
    private static final String MENU_PREFIX = "menu:";
    private static final Map<String, PeakDocument> LAST_SENT = new ConcurrentHashMap<>();

    private PeakSentDocuments() {}

    public static void rememberDocument(UUID playerId, String documentId, PeakDocument document) {
        LAST_SENT.put(key(playerId, documentId), document);
    }

    public static void rememberMenu(UUID playerId, String menuId, PeakDocument menu) {
        LAST_SENT.put(key(playerId, MENU_PREFIX + menuId), menu);
    }

    public static PeakDocument knownDocument(Collection<UUID> playerIds, String documentId) {
        return known(playerIds, documentId);
    }

    public static PeakDocument knownMenu(Collection<UUID> playerIds, String menuId) {
        return known(playerIds, MENU_PREFIX + menuId);
    }

    public static void forgetMenu(UUID playerId, String menuId) {
        LAST_SENT.remove(key(playerId, MENU_PREFIX + menuId));
    }

    public static void forgetMenus(UUID playerId) {
        String prefix = key(playerId, MENU_PREFIX);
        LAST_SENT.keySet().removeIf(entry -> entry.startsWith(prefix));
    }

    public static void forgetPlayer(UUID playerId) {
        String prefix = playerId + ":";
        LAST_SENT.keySet().removeIf(entry -> entry.startsWith(prefix));
    }

    public static void clearAll() {
        LAST_SENT.clear();
    }

    private static PeakDocument known(Collection<UUID> playerIds, String suffix) {
        for (UUID id : playerIds) {
            PeakDocument document = LAST_SENT.get(key(id, suffix));
            if (document != null) return document;
        }
        return null;
    }

    private static String key(UUID playerId, String suffix) {
        return playerId + ":" + suffix;
    }
}
