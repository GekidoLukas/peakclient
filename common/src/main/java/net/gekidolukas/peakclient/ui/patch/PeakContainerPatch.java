package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakMainAxisAlign;
import net.gekidolukas.peakclient.ui.PeakOverflow;

public abstract class PeakContainerPatch<SELF extends PeakContainerPatch<SELF>> extends PeakBasePatch<SELF> {
    public SELF direction(PeakLayoutDirection direction) { data.addProperty("direction", direction.name()); return self(); }
    public SELF alignChildren(PeakAlign align) { data.addProperty("alignChildren", align.name()); return self(); }
    public SELF overflow(PeakOverflow overflow) { data.addProperty("overflow", overflow.name()); return self(); }
    public SELF mainAxisAlign(PeakMainAxisAlign align) { data.addProperty("mainAxisAlign", align.name()); return self(); }
    public SELF gap(int gap) { data.addProperty("gap", gap); return self(); }
}
