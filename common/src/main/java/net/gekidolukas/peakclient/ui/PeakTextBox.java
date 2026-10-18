package net.gekidolukas.peakclient.ui;

public final class PeakTextBox extends PeakElement<PeakTextBox> implements PeakInput {
    public static final int MAX_LENGTH_LIMIT = 256;

    private String value = "";
    private String placeholder = "";
    private int maxLength = 64;
    private PeakTextFilter filter = PeakTextFilter.ANY;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    private int changeDelayTicks = 5;

    public PeakTextBox() { this(PeakId.random()); }

    public PeakTextBox(String id) {
        super(id);
        size = PeakSize.pixels(140, 16);
        style.background(0xFF101010).borderWidth(1).borderColor(0xFF808080);
        interaction = PeakInteraction.create();
    }

    public static PeakTextBox create() { return new PeakTextBox(); }
    public static PeakTextBox create(String id) { return new PeakTextBox(id); }
    public static PeakTextBox create(String id, String value) { return new PeakTextBox(id).value(value); }

    public String getValue() { return value == null ? "" : value; }
    public PeakTextBox value(String value) { this.value = value == null ? "" : value; return this; }

    public String getPlaceholder() { return placeholder == null ? "" : placeholder; }
    public PeakTextBox placeholder(String placeholder) { this.placeholder = placeholder == null ? "" : placeholder; return this; }

    public int getMaxLength() { return Math.max(1, Math.min(maxLength, MAX_LENGTH_LIMIT)); }
    public PeakTextBox maxLength(int maxLength) { this.maxLength = Math.max(1, Math.min(maxLength, MAX_LENGTH_LIMIT)); return this; }

    public PeakTextFilter getFilter() { return filter == null ? PeakTextFilter.ANY : filter; }
    public PeakTextBox filter(PeakTextFilter filter) { this.filter = filter; return this; }

    public PeakPaint getColor() { return color; }
    public PeakTextBox color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakTextBox color(int argb) { return color(PeakPaint.solid(argb)); }

    public int getChangeDelayTicks() { return Math.max(0, changeDelayTicks); }
    public PeakTextBox changeDelayTicks(int ticks) { this.changeDelayTicks = Math.max(0, ticks); return this; }

    @Override
    public String inputValue() { return getValue(); }

    @Override
    public String elementType() { return "text_box"; }
}
