package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakScaling;

public final class PeakDocumentPatch extends PeakContainerPatch<PeakDocumentPatch> {
    public PeakDocumentPatch scaling(PeakScaling scaling) { data.addProperty("scaling", scaling.name()); return this; }
    @Override protected PeakDocumentPatch self() { return this; }
}
