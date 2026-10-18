package net.gekidolukas.peakclient.helper;

import net.gekidolukas.peakclient.PeakClientServer;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakPopUpContainer;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PeakHud {
    private PeakHud() {}

    private static final Map<UUID, List<ActiveNotification>> ACTIVE_NOTIFICATIONS = new ConcurrentHashMap<>();

    private record ActiveNotification(String popupId, List<PeakElement<?>> children, PeakPopupAnimation enterAnimation,
                                      PeakPopupAnimation exitAnimation, PeakPaint backgroundColor, int padding,
                                      PeakLayoutDirection direction, long expiryTick) {}

    private static String notificationDocumentId(UUID playerId) {
        return "peakclient:notifications/" + playerId;
    }

    public static void notify(Collection<UUID> playerIds, String text, PeakPopupAnimation animation, PeakPaint color,
                              int timeToLiveTicks) {
        showPopup(playerIds, List.of(PeakLabel.create(text).color(color)), animation, PeakPopupAnimation.FADE,
                PeakPaint.rgba(0, 0, 0, 160), 6, PeakLayoutDirection.COLUMN, timeToLiveTicks);
    }

    public static void showPopup(Collection<UUID> playerIds, List<PeakElement<?>> children, PeakPopupAnimation enter,
                                 PeakPopupAnimation exit, PeakPaint backgroundColor, int padding, PeakLayoutDirection direction,
                                 int timeToLiveTicks) {
        for (UUID playerId : playerIds) {
            addNotification(playerId, children, enter, exit, backgroundColor, padding, direction, timeToLiveTicks);
        }
    }

    private static void addNotification(UUID playerId, List<PeakElement<?>> children, PeakPopupAnimation enter,
                                        PeakPopupAnimation exit, PeakPaint backgroundColor, int padding,
                                        PeakLayoutDirection direction, int timeToLiveTicks) {
        long now = PeakClientServer.currentTick();
        List<ActiveNotification> list = ACTIVE_NOTIFICATIONS.computeIfAbsent(playerId, id -> new ArrayList<>());
        synchronized (list) {
            list.removeIf(notification -> notification.expiryTick() <= now);
            list.add(new ActiveNotification(PeakId.random(), children, enter, exit, backgroundColor, padding, direction,
                    now + Math.max(1, timeToLiveTicks)));
            sendNotificationDocument(playerId, list, now);
        }
    }

    private static void sendNotificationDocument(UUID playerId, List<ActiveNotification> list, long now) {
        PeakDocument doc = new PeakDocument(notificationDocumentId(playerId))
                .deleteIfEmpty(true)
                .documentTimeToLiveTicks(1200);

        for (int i = 0; i < list.size(); i++) {
            ActiveNotification notification = list.get(i);
            boolean isNewest = i == list.size() - 1;
            long remainingTicks = Math.max(1, notification.expiryTick() - now);

            PeakPopUpContainer popup = new PeakPopUpContainer(notification.popupId())
                    .animateEnter(isNewest)
                    .enterAnimation(notification.enterAnimation())
                    .exitAnimation(notification.exitAnimation())
                    .timeToLiveTicks((int) remainingTicks)
                    .direction(notification.direction())
                    .absolute(PeakPosition.of(PeakAnchor.TOP_RIGHT, -10, 10 + i * 22));
            popup.getStyle().background(notification.backgroundColor());
            popup.getStyle().padding(notification.padding());
            for (PeakElement<?> child : notification.children()) popup.add(child);

            doc.add(popup);
        }

        PeakClientApi.get().sendUiToClient(doc, List.of(playerId));
    }

    public static void clearPlayer(UUID playerId) {
        ACTIVE_NOTIFICATIONS.remove(playerId);
    }
}
