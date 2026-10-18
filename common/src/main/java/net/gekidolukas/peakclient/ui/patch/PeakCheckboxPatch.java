package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakCheckboxLook;

public final class PeakCheckboxPatch extends PeakBasePatch<PeakCheckboxPatch> {
    @Override protected PeakCheckboxPatch self() { return this; }

    public PeakCheckboxPatch checked(boolean checked) { data.addProperty("checked", checked); return self(); }
    public PeakCheckboxPatch text(String text) { data.addProperty("text", text); return self(); }
    public PeakCheckboxPatch color(int argb) { data.addProperty("color", argb); return self(); }
    public PeakCheckboxPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return self(); }
    public PeakCheckboxPatch look(PeakCheckboxLook look) { data.addProperty("look", look.name()); return self(); }
}
