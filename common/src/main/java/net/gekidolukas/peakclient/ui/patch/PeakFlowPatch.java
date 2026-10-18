package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakMainAxisAlign;

public final class PeakFlowPatch extends PeakContainerPatch<PeakFlowPatch> {
    public PeakFlowPatch lineGap(int gap) { data.addProperty("lineGap", Math.max(-1, gap)); return this; }
    public PeakFlowPatch lineAlign(PeakMainAxisAlign align) { data.addProperty("lineAlign", align.name()); return this; }
    public PeakFlowPatch reverse(boolean reverse) { data.addProperty("reverse", reverse); return this; }
    public PeakFlowPatch maxLines(int lines) { data.addProperty("maxLines", Math.max(0, lines)); return this; }
    public PeakFlowPatch wrapSize(int size) { data.addProperty("wrapSize", Math.max(0, size)); return this; }
    @Override protected PeakFlowPatch self() { return this; }
}
