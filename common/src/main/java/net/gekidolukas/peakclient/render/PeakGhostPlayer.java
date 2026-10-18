package net.gekidolukas.peakclient.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;

public final class PeakGhostPlayer extends RemotePlayer {
    private PlayerSkin skinOverride;

    public PeakGhostPlayer(ClientLevel level, GameProfile profile) {
        super(level, profile);
    }

    public void skinOverride(PlayerSkin skin) { this.skinOverride = skin; }

    @Override
    public PlayerSkin getSkin() {
        return skinOverride != null ? skinOverride : super.getSkin();
    }

    public void showAllModelParts() {
        if (getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) != (byte) 0x7F) {
            getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7F);
        }
    }

    public void copyModelParts(Player source) {
        byte parts = source.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
        if (getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION) != parts) {
            getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, parts);
        }
    }
}
