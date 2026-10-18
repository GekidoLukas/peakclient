package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakUiDocumentPayload(byte[] gzippedJson) implements CustomPacketPayload {
    public static final Type<PeakUiDocumentPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "ui_document"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PeakUiDocumentPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.byteArray(PeakProtocol.MAX_PAYLOAD_BYTES), PeakUiDocumentPayload::gzippedJson,
                    PeakUiDocumentPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
