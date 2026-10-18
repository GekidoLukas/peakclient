package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

public final class PeakImage extends PeakElement<PeakImage> {
    private String texture;
    private int sourceWidth = 0;
    private int sourceHeight = 0;
    private PeakObjectFit objectFit = PeakObjectFit.FILL;
    private float aspectRatio = 0f;

    public PeakImage(String texture) {
        this(PeakId.random(), texture);
    }

    public PeakImage(String id, String texture) {
        super(id);
        this.texture = texture;
        size(PeakSize.of(PeakSize.PeakDimension.pixels(16), PeakSize.PeakDimension.pixels(16)));
    }

    public PeakImage(String id, String texture, int sourceWidth, int sourceHeight) {
        this(id, texture);
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
    }

    public static PeakImage create(String texture) { return new PeakImage(texture); }
    public static PeakImage create(String id, String texture) { return new PeakImage(id, texture); }
    public static PeakImage create(String texture, int sourceWidth, int sourceHeight) {
        return new PeakImage(PeakId.random(), texture, sourceWidth, sourceHeight);
    }

    public String getTexture() { return texture; }
    public PeakImage texture(String texture) { this.texture = texture; return this; }
    public PeakImage texture(String texture, int sourceWidth, int sourceHeight) {
        this.texture = texture;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        return this;
    }

    public int getSourceWidth() { return sourceWidth; }
    public int getSourceHeight() { return sourceHeight; }
    public boolean hasKnownSourceSize() { return sourceWidth > 0 && sourceHeight > 0; }
    public PeakImage sourceSize(int width, int height) { this.sourceWidth = width; this.sourceHeight = height; return this; }

    public PeakObjectFit getObjectFit() { return objectFit; }
    public PeakImage objectFit(PeakObjectFit fit) { this.objectFit = fit; return this; }
    public float getAspectRatio() { return aspectRatio; }
    public PeakImage aspectRatio(float ratio) { this.aspectRatio = ratio; return this; }

    @Override
    public String elementType() { return "image"; }
}
