package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.server.PeakClientReadyEvent;
import net.gekidolukas.peakclient.server.PeakDocumentActionEvent;
import net.gekidolukas.peakclient.server.PeakDocumentEvents;
import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.server.PeakMenuEvents;
import net.gekidolukas.peakclient.server.PeakMenuNavigationEvent;
import net.gekidolukas.peakclient.server.PeakMenuSessions;
import net.gekidolukas.peakclient.ui.PeakId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PeakClient {
    private PeakClient() {}

    public static boolean isAvailable() {
        return PeakClientApiHolder.INSTANCE != null;
    }

    public static PeakTargets to(ServerPlayer player) {
        return PeakTargets.ofIds(List.of(player.getUUID()));
    }

    public static PeakTargets to(UUID playerId) {
        return PeakTargets.ofIds(List.of(playerId));
    }

    public static PeakTargets to(Collection<?> targets) {
        List<UUID> ids = new ArrayList<>(targets.size());
        for (Object target : targets) {
            if (target instanceof ServerPlayer player) ids.add(player.getUUID());
            else if (target instanceof UUID id) ids.add(id);
            else throw new IllegalArgumentException("Targets must be ServerPlayer or UUID, got " + (target == null ? "null" : target.getClass().getName()));
        }
        return PeakTargets.ofIds(ids);
    }

    public static PeakTargets to(PeakAudience audience) {
        return new PeakTargets(audience::members);
    }

    public static PeakAudience audience(Collection<ServerPlayer> players) {
        return PeakAudience.players(players);
    }

    public static PeakAudience audienceOfIds(Collection<UUID> playerIds) {
        return PeakAudience.fixed(playerIds);
    }

    public static PeakAudience allPlayers() {
        return PeakAudience.all();
    }

    public static PeakAudience team(String teamName) {
        return PeakAudience.team(teamName);
    }

    public static PeakAudience dimension(ResourceKey<Level> dimension) {
        return PeakAudience.dimension(dimension);
    }

    public static PeakAudience dynamic(Supplier<Collection<UUID>> supplier) {
        return PeakAudience.dynamic(supplier);
    }

    public static boolean hasClient(UUID playerId) {
        return PeakClientApi.get().hasClient(playerId);
    }

    public static void onClientReady(Consumer<PeakClientReadyEvent> listener) {
        PeakClientApi.get().onClientReady(listener);
    }

    public static void forgetPlayer(UUID playerId) {
        PeakClientApi.get().forgetPlayer(playerId);
    }

    public static void onMenuAction(Consumer<PeakMenuActionEvent> listener) {
        PeakMenuEvents.onAction(listener);
    }

    public static void onMenuNavigation(Consumer<PeakMenuNavigationEvent> listener) {
        PeakMenuEvents.onNavigation(listener);
    }

    public static PeakMenuHandlers menu(String menuId) {
        return new PeakMenuHandlers(PeakId.require(menuId, "Menu"));
    }

    public static void onDocumentAction(Consumer<PeakDocumentActionEvent> listener) {
        PeakDocumentEvents.onAction(listener);
    }

    public static PeakDocumentHandlers document(String documentId) {
        return new PeakDocumentHandlers(PeakId.require(documentId, "Document"));
    }

    public static String currentMenu(UUID playerId) {
        return PeakMenuSessions.current(playerId);
    }

    public static List<String> menuHistory(UUID playerId) {
        return PeakMenuSessions.history(playerId);
    }
}
