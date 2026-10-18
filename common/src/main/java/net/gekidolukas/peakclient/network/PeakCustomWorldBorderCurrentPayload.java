package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakCustomWorldBorderCurrentPayload(double minX, double minZ, double maxX, double maxZ)
        implements CustomPacketPayload {
    public static final Type<PeakCustomWorldBorderCurrentPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "custom_world_border_current"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakCustomWorldBorderCurrentPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderCurrentPayload::minX,
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderCurrentPayload::minZ,
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderCurrentPayload::maxX,
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderCurrentPayload::maxZ,
                    PeakCustomWorldBorderCurrentPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
