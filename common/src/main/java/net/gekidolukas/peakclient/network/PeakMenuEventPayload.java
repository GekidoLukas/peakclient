package net.gekidolukas.peakclient.network;

import io.netty.buffer.ByteBuf;
import net.gekidolukas.peakclient.PeakClientMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PeakMenuEventPayload(Kind kind, String menuId, String elementId, String actionId, String argsJson)
        implements CustomPacketPayload {
    public enum Kind { ACTION, NAVIGATED, HUD_ACTION }

    public static final int MAX_ID_LENGTH = 128;
    public static final int MAX_ARGS_LENGTH = 16384;

    public static final Type<PeakMenuEventPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PeakClientMod.MOD_ID, "menu_event"));

    private static final StreamCodec<ByteBuf, Kind> KIND_CODEC =
            ByteBufCodecs.idMapper(index -> Kind.values()[Math.floorMod(index, Kind.values().length)], Kind::ordinal);

    public static final StreamCodec<RegistryFriendlyByteBuf, PeakMenuEventPayload> CODEC =
            StreamCodec.composite(
                    KIND_CODEC, PeakMenuEventPayload::kind,
                    ByteBufCodecs.stringUtf8(MAX_ID_LENGTH), PeakMenuEventPayload::menuId,
                    ByteBufCodecs.stringUtf8(MAX_ID_LENGTH), PeakMenuEventPayload::elementId,
                    ByteBufCodecs.stringUtf8(MAX_ID_LENGTH), PeakMenuEventPayload::actionId,
                    ByteBufCodecs.stringUtf8(MAX_ARGS_LENGTH), PeakMenuEventPayload::argsJson,
                    PeakMenuEventPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
