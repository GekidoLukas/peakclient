package net.gekidolukas.peakclient.server;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.network.PeakHelloPayload;
import net.gekidolukas.peakclient.network.PeakProtocol;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class PeakClients {
    private record ClientInfo(int protocolVersion, int features) {}

    private static final Map<UUID, ClientInfo> CLIENTS = new ConcurrentHashMap<>();
    private static final List<Consumer<PeakClientReadyEvent>> READY_LISTENERS = new CopyOnWriteArrayList<>();

    private PeakClients() {}

    public static boolean hasClient(UUID playerId) {
        return CLIENTS.containsKey(playerId);
    }

    public static int protocolVersion(UUID playerId) {
        ClientInfo info = CLIENTS.get(playerId);
        return info == null ? -1 : info.protocolVersion();
    }

    public static int features(UUID playerId) {
        ClientInfo info = CLIENTS.get(playerId);
        return info == null ? 0 : info.features();
    }

    public static void addReadyListener(Consumer<PeakClientReadyEvent> listener) {
        READY_LISTENERS.add(listener);
    }

    public static void remove(UUID playerId) {
        CLIENTS.remove(playerId);
    }

    public static void onHello(ServerPlayer player, PeakHelloPayload hello) {
        UUID id = player.getUUID();
        CLIENTS.put(id, new ClientInfo(hello.protocolVersion(), hello.features()));
        if (hello.protocolVersion() != PeakProtocol.VERSION) {
            PeakLog.warn("Player {} uses PeakClient protocol {} but the server expects {}",
                    player.getGameProfile().getName(), hello.protocolVersion(), PeakProtocol.VERSION);
        }
        PeakPlayerStates.markOnline(id);
        PeakReplaySummary replay = PeakPlayerStates.replay(player);

        PeakClientReadyEvent event = new PeakClientReadyEvent(player, hello.protocolVersion(), hello.features(), replay);
        for (Consumer<PeakClientReadyEvent> listener : READY_LISTENERS) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                PeakLog.error("A client ready listener failed", e);
            }
        }
        PeakAudiences.refreshAll();
    }
}
