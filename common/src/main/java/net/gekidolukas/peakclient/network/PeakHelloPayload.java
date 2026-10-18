package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakHelloPayload(int protocolVersion, int features) implements CustomPacketPayload {
    public static final Type<PeakHelloPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "hello"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakHelloPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PeakHelloPayload::protocolVersion,
                    ByteBufCodecs.VAR_INT, PeakHelloPayload::features,
                    PeakHelloPayload::new);

    public static PeakHelloPayload current() {
        return new PeakHelloPayload(PeakProtocol.VERSION, 0);
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
