package net.gekidolukas.peakclient.ui;

public final class PeakCarousel extends PeakContainer<PeakCarousel> {
    private PeakCarouselDirection movement = PeakCarouselDirection.LEFT;
    private float speed = 30f;
    private PeakCycleOrder order = PeakCycleOrder.SEQUENTIAL;
    private int fadeStart = 0;
    private int fadeEnd = 0;
    private PeakCarouselMode mode = PeakCarouselMode.LOOP;
    private boolean onlyWhenOverflowing = false;
    private int pauseTicks = 40;

    private transient PeakCarouselTrack<PeakElement<?>> track;
    private transient boolean timed;
    private transient double lastTime;
    private transient double pingPongTicks;

    public PeakCarousel() { this(PeakId.random()); }
    public PeakCarousel(String id) { super(id); }
    public static PeakCarousel create() { return new PeakCarousel(); }
    public static PeakCarousel create(String id) { return new PeakCarousel(id); }

    public PeakCarouselDirection getMovement() { return movement == null ? PeakCarouselDirection.LEFT : movement; }
    public PeakCarousel movement(PeakCarouselDirection movement) { this.movement = movement; return this; }
    public float getSpeed() { return speed; }
    public PeakCarousel speed(float pixelsPerSecond) { this.speed = pixelsPerSecond; return this; }
    public PeakCycleOrder getOrder() { return order == null ? PeakCycleOrder.SEQUENTIAL : order; }
    public PeakCarousel order(PeakCycleOrder order) { this.order = order; return this; }
    public int getFadeStart() { return Math.max(0, fadeStart); }
    public PeakCarousel fadeStart(int pixels) { this.fadeStart = Math.max(0, pixels); return this; }
    public int getFadeEnd() { return Math.max(0, fadeEnd); }
    public PeakCarousel fadeEnd(int pixels) { this.fadeEnd = Math.max(0, pixels); return this; }
    public PeakCarouselMode getMode() { return mode == null ? PeakCarouselMode.LOOP : mode; }
    public PeakCarousel mode(PeakCarouselMode mode) { this.mode = mode; return this; }
    public PeakCarousel pingPong() { return mode(PeakCarouselMode.PINGPONG); }
    public PeakCarousel loop() { return mode(PeakCarouselMode.LOOP); }
    public boolean isOnlyWhenOverflowing() { return onlyWhenOverflowing; }
    public PeakCarousel onlyWhenOverflowing(boolean only) { this.onlyWhenOverflowing = only; return this; }
    public int getPauseTicks() { return Math.max(0, pauseTicks); }
    public PeakCarousel pauseTicks(int ticks) { this.pauseTicks = Math.max(0, ticks); return this; }
    public PeakCarousel fade(int pixels) { return fadeStart(pixels).fadeEnd(pixels); }
    public PeakCarousel fade(int startPixels, int endPixels) { return fadeStart(startPixels).fadeEnd(endPixels); }

    public PeakCarouselTrack<PeakElement<?>> track() {
        if (track == null) track = new PeakCarouselTrack<>();
        return track;
    }

    public double takeElapsedTicks(double now) {
        double elapsed = timed ? Math.max(0.0, Math.min(5.0, now - lastTime)) : 0.0;
        timed = true;
        lastTime = now;
        return elapsed;
    }

    public double advancePingPong(double ticks) {
        pingPongTicks += ticks;
        return pingPongTicks;
    }

    public void resetAnchor() {
        if (track != null) track.reset();
        timed = false;
        pingPongTicks = 0.0;
    }

    @Override
    public String elementType() { return "carousel"; }
}
