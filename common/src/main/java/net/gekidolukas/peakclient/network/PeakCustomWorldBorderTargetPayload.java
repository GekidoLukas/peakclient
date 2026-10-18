package net.gekidolukas.peakclient.network;

import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakCustomWorldBorderTargetPayload(double minX, double minZ, double maxX, double maxZ, int transitionTicks)
        implements CustomPacketPayload {
    public static final Type<PeakCustomWorldBorderTargetPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "custom_world_border_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PeakCustomWorldBorderTargetPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderTargetPayload::minX,
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderTargetPayload::minZ,
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderTargetPayload::maxX,
                    ByteBufCodecs.DOUBLE, PeakCustomWorldBorderTargetPayload::maxZ,
                    ByteBufCodecs.VAR_INT, PeakCustomWorldBorderTargetPayload::transitionTicks,
                    PeakCustomWorldBorderTargetPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
