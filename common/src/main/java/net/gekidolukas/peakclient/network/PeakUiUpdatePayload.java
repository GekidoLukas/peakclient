package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakUiUpdatePayload(String documentId, byte[] gzippedJson) implements CustomPacketPayload {
    public static final Type<PeakUiUpdatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "ui_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PeakUiUpdatePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, PeakUiUpdatePayload::documentId,
                    ByteBufCodecs.byteArray(PeakProtocol.MAX_PAYLOAD_BYTES), PeakUiUpdatePayload::gzippedJson,
                    PeakUiUpdatePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
