package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakWaypointRemovePayload(String waypointId) implements CustomPacketPayload {
    public static final Type<PeakWaypointRemovePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "waypoint_remove"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakWaypointRemovePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PeakWaypointRemovePayload::waypointId, PeakWaypointRemovePayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
