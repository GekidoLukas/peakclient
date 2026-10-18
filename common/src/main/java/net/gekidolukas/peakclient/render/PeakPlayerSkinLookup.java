package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPlayerIconSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;

import java.util.UUID;

public final class PeakPlayerSkinLookup {
    private PeakPlayerSkinLookup() {}

    public static PlayerSkin resolve(PeakPlayerIconSource source, String targetUuid, String texturePath, boolean textureSlim, Minecraft mc) {
        if (source == null) return null;
        return switch (source) {
            case CLIENT_PLAYER -> mc.player != null ? mc.player.getSkin() : null;
            case TARGETED_PLAYER -> mc.crosshairPickEntity instanceof AbstractClientPlayer target ? target.getSkin() : null;
            case UUID -> resolveByUuid(targetUuid, mc);
            case TEXTURE -> PeakTextureSkins.get(PeakValueResolver.string(texturePath), textureSlim);
        };
    }

    private static PlayerSkin resolveByUuid(String targetUuid, Minecraft mc) {
        try {
            UUID uuid = PeakValueResolver.uuid(targetUuid);
            if (uuid == null) return null;
            if (mc.getConnection() != null) {
                var info = mc.getConnection().getPlayerInfo(uuid);
                if (info != null) return info.getSkin();
            }
            return PeakChibiSkinResolver.resolve(uuid);
        } catch (Exception e) {
            return null;
        }
    }
}
