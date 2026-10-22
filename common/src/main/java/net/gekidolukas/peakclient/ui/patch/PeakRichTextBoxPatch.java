package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import com.google.gson.JsonArray;
import net.gekidolukas.peakclient.ui.serialize.PeakComponentAdapter;
import net.minecraft.network.chat.Component;

public final class PeakRichTextBoxPatch extends PeakBasePatch<PeakRichTextBoxPatch> {
    @Override protected PeakRichTextBoxPatch self() { return this; }

    public PeakRichTextBoxPatch value(String value) { data.addProperty("value", value); return self(); }
    public PeakRichTextBoxPatch placeholder(String placeholder) { data.addProperty("placeholder", placeholder); return self(); }
    public PeakRichTextBoxPatch placeholder(Component placeholder) { data.add("placeholder", PeakComponentAdapter.toTree(placeholder)); return self(); }
    public PeakRichTextBoxPatch maxLength(int value) { data.addProperty("maxLength", value); return self(); }
    public PeakRichTextBoxPatch maxLines(int value) { data.addProperty("maxLines", value); return self(); }
    public PeakRichTextBoxPatch wrap(boolean wrap) { data.addProperty("wrap", wrap); return self(); }
    public PeakRichTextBoxPatch lineSpacing(int value) { data.addProperty("lineSpacing", value); return self(); }
    public PeakRichTextBoxPatch color(int argb) { data.addProperty("color", argb); return self(); }
    public PeakRichTextBoxPatch color(PeakPaint paint) { data.add("color", PeakPaintAdapter.toTree(paint)); return self(); }
    public PeakRichTextBoxPatch formatting(boolean formatting) { data.addProperty("formatting", formatting); return self(); }
    public PeakRichTextBoxPatch colors(int... palette) {
        JsonArray paletteJson = new JsonArray();
        for (int rgb : palette) paletteJson.add(rgb & 0xFFFFFF);
        data.add("colors", paletteJson);
        return self();
    }
}
