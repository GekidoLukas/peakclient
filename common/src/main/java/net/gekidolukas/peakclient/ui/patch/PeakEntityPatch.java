package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakEntityCopyMode;
import net.gekidolukas.peakclient.ui.PeakEntitySource;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

public final class PeakEntityPatch extends PeakBasePatch<PeakEntityPatch> {
    public PeakEntityPatch source(PeakEntitySource source) { data.add("source", PeakUiSerializer.GSON.toJsonTree(source)); return this; }
    public PeakEntityPatch copyMode(PeakEntityCopyMode mode) { data.addProperty("copyMode", mode.name()); return this; }
    public PeakEntityPatch yaw(float yaw) { data.addProperty("yaw", yaw); return this; }
    public PeakEntityPatch pitch(float pitch) { data.addProperty("pitch", pitch); return this; }
    public PeakEntityPatch zoom(float zoom) { data.addProperty("zoom", zoom); return this; }
    public PeakEntityPatch showNameTag(boolean show) { data.addProperty("showNameTag", show); return this; }
    @Override protected PeakEntityPatch self() { return this; }
}
