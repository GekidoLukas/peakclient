package net.gekidolukas.peakclient.ui;

public enum PeakEvent {
    CLICK(Kind.INTERACTION),
    RIGHT_CLICK(Kind.INTERACTION),
    DOUBLE_CLICK(Kind.INTERACTION),
    HOVER_ENTER(Kind.INTERACTION),
    HOVER_LEAVE(Kind.INTERACTION),
    FOCUS(Kind.INTERACTION),
    BLUR(Kind.INTERACTION),
    CHANGE(Kind.INPUT),
    SUBMIT(Kind.INPUT),
    VALUE_CHANGE(Kind.DATA),
    VALUE_INCREASE(Kind.DATA),
    VALUE_DECREASE(Kind.DATA),
    VALUE_ABOVE(Kind.DATA),
    VALUE_BELOW(Kind.DATA),
    APPEAR(Kind.LIFECYCLE),
    SHOW(Kind.LIFECYCLE),
    HIDE(Kind.LIFECYCLE),
    TIMER_END(Kind.LIFECYCLE),
    DELAY(Kind.TIME),
    INTERVAL(Kind.TIME);

    public enum Kind { INTERACTION, INPUT, DATA, LIFECYCLE, TIME }

    private final Kind kind;

    PeakEvent(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isInteraction() {
        return kind == Kind.INTERACTION;
    }

    public boolean isInput() {
        return kind == Kind.INPUT;
    }

    public boolean isData() {
        return kind == Kind.DATA;
    }

    public boolean isTime() {
        return kind == Kind.TIME;
    }

    public boolean isThreshold() {
        return this == VALUE_ABOVE || this == VALUE_BELOW;
    }

    public boolean needsMenu() {
        return kind == Kind.INTERACTION || kind == Kind.INPUT;
    }
}
