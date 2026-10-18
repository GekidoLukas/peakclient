package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

public final class PeakBlockElement extends PeakElement<PeakBlockElement> {
    private PeakBlockSource source;
    private float yaw = 0f;

    public PeakBlockElement(PeakBlockSource source) { this(PeakId.random(), source); }

    public PeakBlockElement(String id, PeakBlockSource source) {
        super(id);
        this.source = source;
        size(PeakSize.of(PeakSize.PeakDimension.pixels(32), PeakSize.PeakDimension.pixels(32)));
    }

    public static PeakBlockElement create(PeakBlockSource source) { return new PeakBlockElement(source); }
    public static PeakBlockElement create(String id, PeakBlockSource source) { return new PeakBlockElement(id, source); }

    public static PeakBlockElement fixed(String blockId) { return new PeakBlockElement(PeakBlockSource.fixed(blockId)); }
    public static PeakBlockElement targetedBlock() { return new PeakBlockElement(PeakBlockSource.targetedBlock()); }

    public PeakBlockSource getSource() { return source; }
    public PeakBlockElement source(PeakBlockSource source) { this.source = source; return this; }
    public float getYaw() { return yaw; }
    public PeakBlockElement yaw(float yaw) { this.yaw = yaw; return this; }

    @Override
    public String elementType() { return "block"; }
}
