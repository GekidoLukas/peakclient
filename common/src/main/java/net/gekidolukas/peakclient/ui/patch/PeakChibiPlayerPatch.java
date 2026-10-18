package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakChibiRotation;

import java.util.UUID;

public final class PeakChibiPlayerPatch extends PeakBasePatch<PeakChibiPlayerPatch> {
    public PeakChibiPlayerPatch targetUuid(UUID uuid) { data.addProperty("targetUuid", uuid.toString()); data.addProperty("source", "UUID"); return this; }
    public PeakChibiPlayerPatch rotationYDegrees(float degrees) { data.addProperty("rotationYDegrees", degrees); return this; }
    public PeakChibiPlayerPatch rotationY(PeakChibiRotation rotation) { data.addProperty("rotationYDegrees", rotation.degrees()); return this; }
    public PeakChibiPlayerPatch clientPlayer() { data.addProperty("source", "CLIENT_PLAYER"); return this; }
    public PeakChibiPlayerPatch targetedPlayer() { data.addProperty("source", "TARGETED_PLAYER"); return this; }
    public PeakChibiPlayerPatch texture(String path, boolean slim) {
        data.addProperty("texturePath", path);
        data.addProperty("textureSlim", slim);
        data.addProperty("source", "TEXTURE");
        return this;
    }
    @Override protected PeakChibiPlayerPatch self() { return this; }
}
