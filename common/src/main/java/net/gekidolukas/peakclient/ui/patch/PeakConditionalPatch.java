package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakCondition;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.List;

public final class PeakConditionalPatch extends PeakContainerPatch<PeakConditionalPatch> {
    public PeakConditionalPatch conditions(List<PeakCondition> conditions) {
        data.add("conditions", PeakUiSerializer.GSON.toJsonTree(conditions));
        return this;
    }
    public PeakConditionalPatch requireAll(boolean requireAll) { data.addProperty("requireAll", requireAll); return this; }
    @Override protected PeakConditionalPatch self() { return this; }
}
