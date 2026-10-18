package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

import java.util.UUID;

public final class PeakPlayerIcon extends PeakElement<PeakPlayerIcon> {
    private PeakPlayerIconSource source;
    private String targetUuid;
    private String texturePath;
    private boolean textureSlim = false;
    private PeakPlayerIconView view = PeakPlayerIconView.FACE;

    private PeakPlayerIcon(String id, PeakPlayerIconSource source, String targetUuid) {
        super(id);
        this.source = source;
        this.targetUuid = targetUuid;
        size(PeakSize.of(PeakSize.PeakDimension.pixels(16), PeakSize.PeakDimension.pixels(16)));
    }

    public static PeakPlayerIcon clientPlayer() { return new PeakPlayerIcon(PeakId.random(), PeakPlayerIconSource.CLIENT_PLAYER, null); }
    public static PeakPlayerIcon clientPlayer(String id) { return new PeakPlayerIcon(id, PeakPlayerIconSource.CLIENT_PLAYER, null); }
    public static PeakPlayerIcon targetedPlayer() { return new PeakPlayerIcon(PeakId.random(), PeakPlayerIconSource.TARGETED_PLAYER, null); }
    public static PeakPlayerIcon targetedPlayer(String id) { return new PeakPlayerIcon(id, PeakPlayerIconSource.TARGETED_PLAYER, null); }

    public static PeakPlayerIcon of(UUID uuid) { return new PeakPlayerIcon(PeakId.random(), PeakPlayerIconSource.UUID, uuid.toString()); }
    public static PeakPlayerIcon of(String id, UUID uuid) { return new PeakPlayerIcon(id, PeakPlayerIconSource.UUID, uuid.toString()); }

    public static PeakPlayerIcon ofTexture(String texturePath) { return ofTexture(PeakId.random(), texturePath, false); }
    public static PeakPlayerIcon ofTexture(String texturePath, boolean slim) { return ofTexture(PeakId.random(), texturePath, slim); }
    public static PeakPlayerIcon ofTexture(String id, String texturePath, boolean slim) {
        PeakPlayerIcon icon = new PeakPlayerIcon(id, PeakPlayerIconSource.TEXTURE, null);
        icon.texturePath = texturePath;
        icon.textureSlim = slim;
        return icon;
    }

    public PeakPlayerIconSource getSource() { return source; }
    public String getTargetUuid() { return targetUuid; }
    public String getTexturePath() { return texturePath; }
    public boolean isTextureSlim() { return textureSlim; }
    public PeakPlayerIconView getView() { return view != null ? view : PeakPlayerIconView.FACE; }
    public PeakPlayerIcon view(PeakPlayerIconView view) { this.view = view; return this; }

    @Override
    public String elementType() { return "player_icon"; }
}
