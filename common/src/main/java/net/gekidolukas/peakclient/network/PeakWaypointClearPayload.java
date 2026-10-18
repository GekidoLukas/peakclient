package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakWaypointClearPayload() implements CustomPacketPayload {
    public static final Type<PeakWaypointClearPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "waypoint_clear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakWaypointClearPayload> CODEC =
            StreamCodec.unit(new PeakWaypointClearPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
