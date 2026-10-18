package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakWaypointUpsertPayload(byte[] gzippedJson) implements CustomPacketPayload {
    public static final Type<PeakWaypointUpsertPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "waypoint_upsert"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakWaypointUpsertPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.byteArray(65_536), PeakWaypointUpsertPayload::gzippedJson, PeakWaypointUpsertPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
