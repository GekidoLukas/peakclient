package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakItemSource;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

public final class PeakItemPatch extends PeakBasePatch<PeakItemPatch> {
    public PeakItemPatch source(PeakItemSource source) { data.add("source", PeakUiSerializer.GSON.toJsonTree(source)); return this; }
    @Override protected PeakItemPatch self() { return this; }
}