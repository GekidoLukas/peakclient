package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakCustomValueSetPayload(String namespace, String key, String value) implements CustomPacketPayload {
    public static final Type<PeakCustomValueSetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "custom_value_set"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PeakCustomValueSetPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PeakCustomValueSetPayload::namespace,
                    ByteBufCodecs.STRING_UTF8, PeakCustomValueSetPayload::key,
                    ByteBufCodecs.STRING_UTF8, PeakCustomValueSetPayload::value,
                    PeakCustomValueSetPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
