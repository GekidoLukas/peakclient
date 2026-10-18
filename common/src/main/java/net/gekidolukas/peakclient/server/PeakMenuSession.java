package net.gekidolukas.peakclient.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakMenuSession {
    public enum Result { ACCEPTED, UNKNOWN_MENU, INVALID, RATE_LIMITED }

    public static final int MAX_EVENTS_PER_SECOND = 30;
    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_ARGUMENTS = 32;
    public static final int MAX_ARGUMENT_LENGTH = 2048;
    public static final int MAX_HISTORY = 32;

    private final Set<String> known = new HashSet<>();
    private String current = "";
    private List<String> history = List.of();
    private boolean windowOpen;
    private long windowStart;
    private int windowCount;

    public synchronized void markKnown(String menuId) {
        known.add(menuId);
    }

    public synchronized void markRemoved(String menuId) {
        known.remove(menuId);
        history = history.stream().filter(id -> !id.equals(menuId)).toList();
        if (current.equals(menuId)) current = history.isEmpty() ? "" : history.get(history.size() - 1);
    }

    public synchronized void markCleared() {
        known.clear();
        current = "";
        history = List.of();
    }

    public synchronized boolean isKnown(String menuId) {
        return known.contains(menuId);
    }

    public synchronized String current() {
        return current.isEmpty() ? null : current;
    }

    public synchronized List<String> history() {
        return history;
    }

    public synchronized Result checkAction(String menuId, String elementId, String actionId, Map<String, String> args, long nowMillis) {
        return check(menuId, elementId, actionId, args, nowMillis, known.contains(menuId));
    }

    public synchronized Result checkDocumentAction(String documentId, String elementId, String actionId,
                                                   Map<String, String> args, long nowMillis, boolean sentToPlayer) {
        return check(documentId, elementId, actionId, args, nowMillis, sentToPlayer);
    }

    private Result check(String menuId, String elementId, String actionId, Map<String, String> args, long nowMillis,
                         boolean knownTarget) {
        if (menuId.isEmpty() || menuId.length() > MAX_ID_LENGTH
                || actionId.isEmpty() || actionId.length() > MAX_ID_LENGTH
                || elementId.length() > MAX_ID_LENGTH) {
            return Result.INVALID;
        }
        if (args.size() > MAX_ARGUMENTS) return Result.INVALID;
        for (Map.Entry<String, String> entry : args.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null || key.length() > MAX_ID_LENGTH || value.length() > MAX_ARGUMENT_LENGTH) {
                return Result.INVALID;
            }
        }
        if (!knownTarget) return Result.UNKNOWN_MENU;
        if (!windowOpen || nowMillis - windowStart >= 1000 || nowMillis < windowStart) {
            windowOpen = true;
            windowStart = nowMillis;
            windowCount = 0;
        }
        windowCount++;
        return windowCount > MAX_EVENTS_PER_SECOND ? Result.RATE_LIMITED : Result.ACCEPTED;
    }

    public synchronized void navigated(String newCurrent, List<String> newHistory) {
        List<String> sanitized = new ArrayList<>();
        for (String id : newHistory) {
            if (id != null && id.length() <= MAX_ID_LENGTH && known.contains(id) && sanitized.size() < MAX_HISTORY) {
                sanitized.add(id);
            }
        }
        history = List.copyOf(sanitized);
        current = newCurrent != null && known.contains(newCurrent) ? newCurrent : "";
    }
}
