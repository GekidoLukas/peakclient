package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakCustomValueClearPayload(String namespace, String key) implements CustomPacketPayload {
    public static final Type<PeakCustomValueClearPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "custom_value_clear"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PeakCustomValueClearPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PeakCustomValueClearPayload::namespace,
                    ByteBufCodecs.STRING_UTF8, PeakCustomValueClearPayload::key,
                    PeakCustomValueClearPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
