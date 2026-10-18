package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakVanillaHudPayload(byte[] gzippedJson) implements CustomPacketPayload {
    public static final Type<PeakVanillaHudPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "vanilla_hud"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PeakVanillaHudPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.byteArray(65_536), PeakVanillaHudPayload::gzippedJson,
                    PeakVanillaHudPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
