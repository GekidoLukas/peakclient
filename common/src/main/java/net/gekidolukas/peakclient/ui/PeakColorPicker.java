package net.gekidolukas.peakclient.ui;

public final class PeakColorPicker extends PeakElement<PeakColorPicker> implements PeakInput {
    @PeakOpaqueColor private int value = 0xFFFFFFFF;
    private boolean alpha = false;
    private boolean showHex = true;
    private String target = "";
    private String hexField = "";

    public PeakColorPicker() { this(PeakId.random()); }

    public PeakColorPicker(String id) {
        super(id);
        size = PeakSize.pixels(150, 116);
        interaction = PeakInteraction.create();
    }

    public static PeakColorPicker create() { return new PeakColorPicker(); }
    public static PeakColorPicker create(String id) { return new PeakColorPicker(id); }

    public static PeakColorPicker create(String id, int color) {
        return new PeakColorPicker(id).value(color);
    }

    public int getValue() {
        int normalized = PeakArgb.ensureOpaque(value);
        return alpha ? normalized : (normalized | 0xFF000000);
    }

    public PeakColorPicker value(int color) {
        this.value = PeakArgb.ensureOpaque(color);
        return this;
    }

    public boolean isAlpha() { return alpha; }
    public PeakColorPicker alpha(boolean alpha) { this.alpha = alpha; return this; }

    public boolean isShowHex() { return showHex; }
    public PeakColorPicker showHex(boolean showHex) { this.showHex = showHex; return this; }

    public String getTarget() { return target == null ? "" : target; }
    public PeakColorPicker target(String richTextBoxId) {
        this.target = richTextBoxId == null || richTextBoxId.isEmpty() ? "" : PeakId.require(richTextBoxId, "Element");
        return this;
    }

    public String getHexField() { return hexField == null ? "" : hexField; }
    public PeakColorPicker hexField(String textBoxId) {
        this.hexField = textBoxId == null || textBoxId.isEmpty() ? "" : PeakId.require(textBoxId, "Element");
        return this;
    }

    @Override
    public String inputValue() {
        return PeakColorMath.formatHex(getValue(), alpha);
    }

    @Override
    public String elementType() { return "color_picker"; }
}
