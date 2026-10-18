package net.gekidolukas.peakclient.ui.patch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakCompassStackDirection;
import net.gekidolukas.peakclient.ui.PeakCompassTickPosition;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.List;

public final class PeakCompassPatch extends PeakBasePatch<PeakCompassPatch> {

    public PeakCompassPatch fieldOfViewDegrees(float degrees) { data.addProperty("fieldOfViewDegrees", degrees); return this; }
    public PeakCompassPatch maxDistance(double distance) { data.addProperty("maxDistance", distance); return this; }
    public PeakCompassPatch stackDirection(PeakCompassStackDirection direction) { data.addProperty("stackDirection", direction.name()); return this; }
    public PeakCompassPatch tickPosition(PeakCompassTickPosition position) { data.addProperty("tickPosition", position.name()); return this; }
    public PeakCompassPatch groupSameNameWaypoints(boolean group) { data.addProperty("groupSameNameWaypoints", group); return this; }
    public PeakCompassPatch sameNameMergeDistance(int pixels) { data.addProperty("sameNameMergeDistance", pixels); return this; }
    @Override protected PeakCompassPatch self() { return this; }
}
