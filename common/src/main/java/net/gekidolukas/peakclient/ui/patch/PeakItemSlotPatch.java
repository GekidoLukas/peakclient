package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakItemSource;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.gekidolukas.peakclient.ui.serialize.PeakComponentAdapter;
import net.minecraft.network.chat.Component;

public final class PeakItemSlotPatch extends PeakBasePatch<PeakItemSlotPatch> {
    @Override protected PeakItemSlotPatch self() { return this; }

    public PeakItemSlotPatch source(PeakItemSource source) { data.add("source", PeakUiSerializer.GSON.toJsonTree(source)); return self(); }
    public PeakItemSlotPatch selected(boolean selected) { data.addProperty("selected", selected); return self(); }
    public PeakItemSlotPatch showCount(boolean show) { data.addProperty("showCount", show); return self(); }
    public PeakItemSlotPatch showDurability(boolean show) { data.addProperty("showDurability", show); return self(); }
    public PeakItemSlotPatch countText(String text) { data.addProperty("countText", text); return self(); }
    public PeakItemSlotPatch countText(Component text) { data.add("countText", PeakComponentAdapter.toTree(text)); return self(); }
    public PeakItemSlotPatch itemTooltip(boolean enabled) { data.addProperty("itemTooltip", enabled); return self(); }
}
