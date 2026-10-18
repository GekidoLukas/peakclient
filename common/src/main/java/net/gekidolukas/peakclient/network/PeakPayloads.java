package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.server.PeakClients;
import net.gekidolukas.peakclient.server.PeakMenuSessions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PeakPayloads {
    @FunctionalInterface
    public interface ClientboundVisitor {
        <T extends CustomPacketPayload> void visit(CustomPacketPayload.Type<T> type,
                                                   StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                   Consumer<T> handler);
    }

    @FunctionalInterface
    public interface ServerboundVisitor {
        <T extends CustomPacketPayload> void visit(CustomPacketPayload.Type<T> type,
                                                   StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                   BiConsumer<ServerPlayer, T> handler);
    }

    private PeakPayloads() {}

    public static void forEachClientbound(ClientboundVisitor visitor) {
        visitor.visit(PeakUiDocumentPayload.TYPE, PeakUiDocumentPayload.CODEC, payload -> PeakClientPayloadHandlers.onDocument(payload));
        visitor.visit(PeakUiUpdatePayload.TYPE, PeakUiUpdatePayload.CODEC, payload -> PeakClientPayloadHandlers.onUpdate(payload));
        visitor.visit(PeakVanillaHudPayload.TYPE, PeakVanillaHudPayload.CODEC, payload -> PeakClientPayloadHandlers.onVanillaHud(payload));
        visitor.visit(PeakCustomValueSetPayload.TYPE, PeakCustomValueSetPayload.CODEC, payload -> PeakClientPayloadHandlers.onCustomValueSet(payload));
        visitor.visit(PeakCustomValueClearPayload.TYPE, PeakCustomValueClearPayload.CODEC, payload -> PeakClientPayloadHandlers.onCustomValueClear(payload));
        visitor.visit(PeakWaypointUpsertPayload.TYPE, PeakWaypointUpsertPayload.CODEC, payload -> PeakClientPayloadHandlers.onWaypointUpsert(payload));
        visitor.visit(PeakWaypointPositionSyncPayload.TYPE, PeakWaypointPositionSyncPayload.CODEC,
                payload -> PeakClientPayloadHandlers.onWaypointPosition(payload));
        visitor.visit(PeakWaypointRemovePayload.TYPE, PeakWaypointRemovePayload.CODEC, payload -> PeakClientPayloadHandlers.onWaypointRemove(payload));
        visitor.visit(PeakWaypointClearPayload.TYPE, PeakWaypointClearPayload.CODEC, payload -> PeakClientPayloadHandlers.onWaypointClear(payload));
        visitor.visit(PeakCustomWorldBorderCurrentPayload.TYPE, PeakCustomWorldBorderCurrentPayload.CODEC,
                payload -> PeakClientPayloadHandlers.onWorldBorderCurrent(payload));
        visitor.visit(PeakCustomWorldBorderTargetPayload.TYPE, PeakCustomWorldBorderTargetPayload.CODEC,
                payload -> PeakClientPayloadHandlers.onWorldBorderTarget(payload));
        visitor.visit(PeakCustomWorldBorderClearPayload.TYPE, PeakCustomWorldBorderClearPayload.CODEC,
                payload -> PeakClientPayloadHandlers.onWorldBorderClear(payload));
        visitor.visit(PeakMenuPayload.TYPE, PeakMenuPayload.CODEC, payload -> PeakClientPayloadHandlers.onMenu(payload));
    }

    public static void forEachServerbound(ServerboundVisitor visitor) {
        visitor.visit(PeakHelloPayload.TYPE, PeakHelloPayload.CODEC, (player, payload) -> PeakClients.onHello(player, payload));
        visitor.visit(PeakMenuEventPayload.TYPE, PeakMenuEventPayload.CODEC, (player, payload) -> PeakMenuSessions.onEvent(player, payload));
    }
}
