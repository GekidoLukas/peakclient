package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakCustomWorldBorderClearPayload() implements CustomPacketPayload {
    public static final Type<PeakCustomWorldBorderClearPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "custom_world_border_clear"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakCustomWorldBorderClearPayload> CODEC =
            StreamCodec.unit(new PeakCustomWorldBorderClearPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
