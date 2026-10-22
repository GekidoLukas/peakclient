package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import net.gekidolukas.peakclient.ui.PeakTextFilter;
import net.gekidolukas.peakclient.ui.serialize.PeakComponentAdapter;
import net.minecraft.network.chat.Component;

public final class PeakTextBoxPatch extends PeakBasePatch<PeakTextBoxPatch> {
    @Override protected PeakTextBoxPatch self() { return this; }

    public PeakTextBoxPatch value(String value) { data.addProperty("value", value); return self(); }
    public PeakTextBoxPatch placeholder(String placeholder) { data.addProperty("placeholder", placeholder); return self(); }
    public PeakTextBoxPatch placeholder(Component placeholder) { data.add("placeholder", PeakComponentAdapter.toTree(placeholder)); return self(); }
    public PeakTextBoxPatch maxLength(int value) { data.addProperty("maxLength", value); return self(); }
    public PeakTextBoxPatch filter(PeakTextFilter filter) { data.addProperty("filter", filter.name()); return self(); }
    public PeakTextBoxPatch color(int argb) { data.addProperty("color", argb); return self(); }
    public PeakTextBoxPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return self(); }
}
