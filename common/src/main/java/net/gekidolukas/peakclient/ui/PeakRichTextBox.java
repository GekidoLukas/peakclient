package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PeakRichTextBox extends PeakElement<PeakRichTextBox> implements PeakInput {
    public static final int MAX_LENGTH_LIMIT = 2000;

    private String value = "";
    private Component placeholder = Component.empty();
    private int maxLength = 512;
    private int maxLines = 0;
    private boolean wrap = true;
    private int lineSpacing = 1;
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    private boolean formatting = true;
    private List<Integer> colors = defaultColors();
    private int changeDelayTicks = 5;
    private PeakScrollbarStyle scrollbar = new PeakScrollbarStyle();

    public PeakRichTextBox() { this(PeakId.random()); }

    public PeakRichTextBox(String id) {
        super(id);
        size = PeakSize.pixels(160, 64);
        style.background(0xFF101010).borderWidth(1).borderColor(0xFF808080);
        interaction = PeakInteraction.create();
    }

    public static PeakRichTextBox create() { return new PeakRichTextBox(); }
    public static PeakRichTextBox create(String id) { return new PeakRichTextBox(id); }
    public static PeakRichTextBox create(String id, String value) { return new PeakRichTextBox(id).value(value); }

    public static List<Integer> defaultColors() {
        return new ArrayList<>(List.of(
                0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000,
                0xFF5555, 0xAA0000, 0xFFAA00, 0xFFFF55,
                0x55FF55, 0x00AA00, 0x55FFFF, 0x00AAAA,
                0x5555FF, 0x0000AA, 0xFF55FF, 0xAA00AA));
    }

    public String getValue() { return value == null ? "" : value; }
    public PeakRichTextBox value(String value) { this.value = value == null ? "" : value; return this; }

    public Component getPlaceholder() { return placeholder == null ? Component.empty() : placeholder; }
    public PeakRichTextBox placeholder(String placeholder) { return placeholder(Component.literal(placeholder == null ? "" : placeholder)); }
    public PeakRichTextBox placeholder(Component placeholder) { this.placeholder = placeholder == null ? Component.empty() : placeholder; return this; }

    public int getMaxLength() { return Math.max(1, Math.min(maxLength, MAX_LENGTH_LIMIT)); }
    public PeakRichTextBox maxLength(int maxLength) { this.maxLength = Math.max(1, Math.min(maxLength, MAX_LENGTH_LIMIT)); return this; }

    public int getMaxLines() { return Math.max(0, maxLines); }
    public PeakRichTextBox maxLines(int maxLines) { this.maxLines = Math.max(0, maxLines); return this; }

    public boolean isWrap() { return wrap; }
    public PeakRichTextBox wrap(boolean wrap) { this.wrap = wrap; return this; }

    public int getLineSpacing() { return Math.max(0, lineSpacing); }
    public PeakRichTextBox lineSpacing(int spacing) { this.lineSpacing = Math.max(0, spacing); return this; }

    public PeakPaint getColor() { return color; }
    public PeakRichTextBox color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakRichTextBox color(int argb) { return color(PeakPaint.solid(argb)); }

    public boolean isFormatting() { return formatting; }
    public PeakRichTextBox formatting(boolean formatting) { this.formatting = formatting; return this; }

    public List<Integer> getColors() { return colors == null || colors.isEmpty() ? defaultColors() : colors; }
    public PeakRichTextBox colors(int... palette) {
        colors = new ArrayList<>();
        for (int rgb : palette) colors.add(rgb & 0xFFFFFF);
        return this;
    }

    public int getChangeDelayTicks() { return Math.max(0, changeDelayTicks); }
    public PeakRichTextBox changeDelayTicks(int ticks) { this.changeDelayTicks = Math.max(0, ticks); return this; }

    public PeakScrollbarStyle getScrollbar() {
        if (scrollbar == null) scrollbar = new PeakScrollbarStyle();
        return scrollbar;
    }
    public PeakRichTextBox scrollbar(Consumer<PeakScrollbarStyle> customizer) {
        customizer.accept(getScrollbar());
        return this;
    }
    public PeakRichTextBox hideScrollbar() { getScrollbar().visible(false); return this; }

    @Override
    public String inputValue() { return getValue(); }

    @Override
    public String elementType() { return "rich_text_box"; }
}
