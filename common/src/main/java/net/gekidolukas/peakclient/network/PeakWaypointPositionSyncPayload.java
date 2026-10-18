package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakWaypointPositionSyncPayload(String waypointId, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<PeakWaypointPositionSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "waypoint_position_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakWaypointPositionSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PeakWaypointPositionSyncPayload::waypointId,
                    ByteBufCodecs.DOUBLE, PeakWaypointPositionSyncPayload::x,
                    ByteBufCodecs.DOUBLE, PeakWaypointPositionSyncPayload::y,
                    ByteBufCodecs.DOUBLE, PeakWaypointPositionSyncPayload::z,
                    PeakWaypointPositionSyncPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
