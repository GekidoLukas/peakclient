package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakObjectFit;

public final class PeakImagePatch extends PeakBasePatch<PeakImagePatch> {
    public PeakImagePatch texture(String texture) { data.addProperty("texture", texture); return this; }
    public PeakImagePatch texture(String texture, int sourceWidth, int sourceHeight) {
        data.addProperty("texture", texture);
        data.addProperty("sourceWidth", sourceWidth);
        data.addProperty("sourceHeight", sourceHeight);
        return this;
    }
    public PeakImagePatch sourceSize(int width, int height) {
        data.addProperty("sourceWidth", width);
        data.addProperty("sourceHeight", height);
        return this;
    }
    public PeakImagePatch objectFit(PeakObjectFit fit) { data.addProperty("objectFit", fit.name()); return this; }
    public PeakImagePatch aspectRatio(float ratio) { data.addProperty("aspectRatio", ratio); return this; }
    @Override protected PeakImagePatch self() { return this; }
}
