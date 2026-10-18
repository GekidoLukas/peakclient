package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakOption;

import java.util.Arrays;

public final class PeakDropdownPatch extends PeakBasePatch<PeakDropdownPatch> {
    @Override protected PeakDropdownPatch self() { return this; }

    public PeakDropdownPatch selected(String value) { data.addProperty("selected", value); return self(); }
    public PeakDropdownPatch options(PeakOption... options) { return field("options", Arrays.asList(options)); }
    public PeakDropdownPatch placeholder(String text) { data.addProperty("placeholder", text); return self(); }
    public PeakDropdownPatch maxVisible(int rows) { data.addProperty("maxVisible", rows); return self(); }
}
