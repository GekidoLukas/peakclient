package net.gekidolukas.peakclient.ui.patch;

public final class PeakColorPickerPatch extends PeakBasePatch<PeakColorPickerPatch> {
    @Override protected PeakColorPickerPatch self() { return this; }

    public PeakColorPickerPatch value(int color) { data.addProperty("value", color); return self(); }
    public PeakColorPickerPatch alpha(boolean alpha) { data.addProperty("alpha", alpha); return self(); }
    public PeakColorPickerPatch showHex(boolean showHex) { data.addProperty("showHex", showHex); return self(); }
    public PeakColorPickerPatch target(String richTextBoxId) { data.addProperty("target", richTextBoxId); return self(); }
    public PeakColorPickerPatch hexField(String textBoxId) { data.addProperty("hexField", textBoxId); return self(); }
}
