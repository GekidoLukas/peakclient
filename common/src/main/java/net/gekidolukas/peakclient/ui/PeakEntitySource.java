package net.gekidolukas.peakclient.ui;

public final class PeakEntitySource {
    public enum Kind { FIXED, PLAYER, TARGETED_ENTITY, PLAYER_TEXTURE }

    private Kind kind;
    private String entityTypeId;
    private String texturePath;
    private boolean slim;

    private PeakEntitySource() {}

    public static PeakEntitySource fixed(String entityTypeId) {
        PeakEntitySource source = new PeakEntitySource();
        source.kind = Kind.FIXED; source.entityTypeId = entityTypeId;
        return source;
    }
    public static PeakEntitySource player() {
        PeakEntitySource source = new PeakEntitySource(); source.kind = Kind.PLAYER; return source;
    }
    public static PeakEntitySource playerTexture(String texturePath, boolean slim) {
        PeakEntitySource source = new PeakEntitySource();
        source.kind = Kind.PLAYER_TEXTURE; source.texturePath = texturePath; source.slim = slim;
        return source;
    }
    public static PeakEntitySource targetedEntity() {
        PeakEntitySource source = new PeakEntitySource(); source.kind = Kind.TARGETED_ENTITY; return source;
    }

    public Kind kind() { return kind; }
    public String entityTypeId() { return entityTypeId; }
    public String texturePath() { return texturePath; }
    public boolean slim() { return slim; }
}
