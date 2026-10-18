package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

import java.util.UUID;

public final class PeakChibiPlayer extends PeakElement<PeakChibiPlayer> {
    private PeakPlayerIconSource source;
    private String targetUuid;
    private String texturePath;
    private boolean textureSlim = false;
    private float rotationYDegrees = PeakChibiRotation.THREE_QUARTER.degrees();

    private PeakChibiPlayer(String id, PeakPlayerIconSource source, String targetUuid) {
        super(id);
        this.source = source;
        this.targetUuid = targetUuid;
        size(PeakSize.of(PeakSize.PeakDimension.pixels(48), PeakSize.PeakDimension.pixels(64)));
    }

    public static PeakChibiPlayer clientPlayer() { return new PeakChibiPlayer(PeakId.random(), PeakPlayerIconSource.CLIENT_PLAYER, null); }
    public static PeakChibiPlayer clientPlayer(String id) { return new PeakChibiPlayer(id, PeakPlayerIconSource.CLIENT_PLAYER, null); }
    public static PeakChibiPlayer targetedPlayer() { return new PeakChibiPlayer(PeakId.random(), PeakPlayerIconSource.TARGETED_PLAYER, null); }
    public static PeakChibiPlayer targetedPlayer(String id) { return new PeakChibiPlayer(id, PeakPlayerIconSource.TARGETED_PLAYER, null); }
    public static PeakChibiPlayer of(UUID uuid) { return new PeakChibiPlayer(PeakId.random(), PeakPlayerIconSource.UUID, uuid.toString()); }
    public static PeakChibiPlayer of(String id, UUID uuid) { return new PeakChibiPlayer(id, PeakPlayerIconSource.UUID, uuid.toString()); }

    public static PeakChibiPlayer ofTexture(String texturePath) { return ofTexture(PeakId.random(), texturePath, false); }
    public static PeakChibiPlayer ofTexture(String texturePath, boolean slim) { return ofTexture(PeakId.random(), texturePath, slim); }
    public static PeakChibiPlayer ofTexture(String id, String texturePath, boolean slim) {
        PeakChibiPlayer chibi = new PeakChibiPlayer(id, PeakPlayerIconSource.TEXTURE, null);
        chibi.texturePath = texturePath;
        chibi.textureSlim = slim;
        return chibi;
    }

    public PeakPlayerIconSource getSource() { return source; }
    public String getTargetUuid() { return targetUuid; }
    public String getTexturePath() { return texturePath; }
    public boolean isTextureSlim() { return textureSlim; }
    public float getRotationYDegrees() { return rotationYDegrees; }
    public PeakChibiPlayer rotationYDegrees(float degrees) { this.rotationYDegrees = degrees; return this; }
    public PeakChibiPlayer rotationY(PeakChibiRotation rotation) { this.rotationYDegrees = rotation.degrees(); return this; }

    @Override
    public String elementType() { return "chibi_player"; }
}
