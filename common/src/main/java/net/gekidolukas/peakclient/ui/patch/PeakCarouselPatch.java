package net.gekidolukas.peakclient.ui.patch;

import net.gekidolukas.peakclient.ui.PeakCarouselDirection;
import net.gekidolukas.peakclient.ui.PeakCarouselMode;
import net.gekidolukas.peakclient.ui.PeakCycleOrder;

public final class PeakCarouselPatch extends PeakContainerPatch<PeakCarouselPatch> {
    public PeakCarouselPatch movement(PeakCarouselDirection movement) { data.addProperty("movement", movement.name()); return this; }
    public PeakCarouselPatch speed(float pixelsPerSecond) { data.addProperty("speed", pixelsPerSecond); return this; }
    public PeakCarouselPatch order(PeakCycleOrder order) { data.addProperty("order", order.name()); return this; }
    public PeakCarouselPatch fadeStart(int pixels) { data.addProperty("fadeStart", Math.max(0, pixels)); return this; }
    public PeakCarouselPatch fadeEnd(int pixels) { data.addProperty("fadeEnd", Math.max(0, pixels)); return this; }
    public PeakCarouselPatch mode(PeakCarouselMode mode) { data.addProperty("mode", mode.name()); return this; }
    public PeakCarouselPatch pingPong() { return mode(PeakCarouselMode.PINGPONG); }
    public PeakCarouselPatch loop() { return mode(PeakCarouselMode.LOOP); }
    public PeakCarouselPatch onlyWhenOverflowing(boolean only) { data.addProperty("onlyWhenOverflowing", only); return this; }
    public PeakCarouselPatch pauseTicks(int ticks) { data.addProperty("pauseTicks", Math.max(0, ticks)); return this; }
    public PeakCarouselPatch fade(int pixels) { return fadeStart(pixels).fadeEnd(pixels); }
    @Override protected PeakCarouselPatch self() { return this; }
}
