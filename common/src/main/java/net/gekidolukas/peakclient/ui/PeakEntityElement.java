package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

public final class PeakEntityElement extends PeakElement<PeakEntityElement> {
    private PeakEntitySource source;
    private PeakEntityCopyMode copyMode = PeakEntityCopyMode.FULL;
    private float yaw = 20f;
    private float pitch = -10f;
    private float zoom = 1f;
    private boolean showNameTag = false;

    public PeakEntityElement(PeakEntitySource source) { this(PeakId.random(), source); }

    public PeakEntityElement(String id, PeakEntitySource source) {
        super(id);
        this.source = source;
        size(PeakSize.of(PeakSize.PeakDimension.pixels(32), PeakSize.PeakDimension.pixels(48)));
    }

    public static PeakEntityElement create(PeakEntitySource source) { return new PeakEntityElement(source); }
    public static PeakEntityElement create(String id, PeakEntitySource source) { return new PeakEntityElement(id, source); }

    public static PeakEntityElement fixed(String entityTypeId) { return new PeakEntityElement(PeakEntitySource.fixed(entityTypeId)); }
    public static PeakEntityElement player() { return new PeakEntityElement(PeakEntitySource.player()); }
    public static PeakEntityElement targetedEntity() { return new PeakEntityElement(PeakEntitySource.targetedEntity()); }
    public static PeakEntityElement playerTexture(String texturePath) { return new PeakEntityElement(PeakEntitySource.playerTexture(texturePath, false)); }
    public static PeakEntityElement playerTexture(String texturePath, boolean slim) {
        return new PeakEntityElement(PeakEntitySource.playerTexture(texturePath, slim));
    }

    public PeakEntitySource getSource() { return source; }
    public PeakEntityElement source(PeakEntitySource source) { this.source = source; return this; }
    public PeakEntityCopyMode getCopyMode() { return copyMode; }
    public PeakEntityElement copyMode(PeakEntityCopyMode mode) { this.copyMode = mode; return this; }
    public float getYaw() { return yaw; }
    public PeakEntityElement yaw(float yaw) { this.yaw = yaw; return this; }
    public float getPitch() { return pitch; }
    public PeakEntityElement pitch(float pitch) { this.pitch = pitch; return this; }
    public float getZoom() { return zoom; }
    public PeakEntityElement zoom(float zoom) { this.zoom = zoom; return this; }
    public boolean isShowNameTag() { return showNameTag; }
    public PeakEntityElement showNameTag(boolean show) { this.showNameTag = show; return this; }

    @Override
    public String elementType() { return "entity"; }
}
