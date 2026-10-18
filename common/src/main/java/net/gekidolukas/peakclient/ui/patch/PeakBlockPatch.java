package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakBlockSource;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

public final class PeakBlockPatch extends PeakBasePatch<PeakBlockPatch> {
    public PeakBlockPatch source(PeakBlockSource source) { data.add("source", PeakUiSerializer.GSON.toJsonTree(source)); return this; }
    public PeakBlockPatch yaw(float yaw) { data.addProperty("yaw", yaw); return this; }
    @Override protected PeakBlockPatch self() { return this; }
}
