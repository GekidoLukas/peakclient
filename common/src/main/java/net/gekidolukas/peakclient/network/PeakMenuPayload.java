package net.gekidolukas.peakclient.network;

import io.netty.buffer.ByteBuf;
import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakMenuPayload(Kind kind, String menuId, int flags, byte[] gzippedJson) implements CustomPacketPayload {
    public enum Kind { SEND, UPDATE, OPEN, CLOSE, BACK, REMOVE, CLEAR }

    public static final int FLAG_REPLACE = 1;
    public static final int FLAG_FORCE = 2;
    public static final int MAX_ID_LENGTH = 128;

    public static final Type<PeakMenuPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "menu"));

    private static final StreamCodec<ByteBuf, Kind> KIND_CODEC =
            ByteBufCodecs.idMapper(index -> Kind.values()[Math.floorMod(index, Kind.values().length)], Kind::ordinal);

    public static final StreamCodec<RegistryFriendlyByteBuf, PeakMenuPayload> CODEC =
            StreamCodec.composite(
                    KIND_CODEC, PeakMenuPayload::kind,
                    ByteBufCodecs.stringUtf8(MAX_ID_LENGTH), PeakMenuPayload::menuId,
                    ByteBufCodecs.VAR_INT, PeakMenuPayload::flags,
                    ByteBufCodecs.byteArray(PeakProtocol.MAX_PAYLOAD_BYTES), PeakMenuPayload::gzippedJson,
                    PeakMenuPayload::new
            );

    public static PeakMenuPayload command(Kind kind, String menuId, int flags) {
        return new PeakMenuPayload(kind, menuId == null ? "" : menuId, flags, new byte[0]);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
