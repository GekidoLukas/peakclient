package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPlayerIconView;

import java.util.UUID;

public final class PeakPlayerIconPatch extends PeakBasePatch<PeakPlayerIconPatch> {
    public PeakPlayerIconPatch targetUuid(UUID uuid) { data.addProperty("targetUuid", uuid.toString()); data.addProperty("source", "UUID"); return this; }
    public PeakPlayerIconPatch view(PeakPlayerIconView view) { data.addProperty("view", view.name()); return this; }
    public PeakPlayerIconPatch clientPlayer() { data.addProperty("source", "CLIENT_PLAYER"); return this; }
    public PeakPlayerIconPatch targetedPlayer() { data.addProperty("source", "TARGETED_PLAYER"); return this; }
    public PeakPlayerIconPatch texture(String path, boolean slim) {
        data.addProperty("texturePath", path);
        data.addProperty("textureSlim", slim);
        data.addProperty("source", "TEXTURE");
        return this;
    }
    @Override protected PeakPlayerIconPatch self() { return this; }
}
