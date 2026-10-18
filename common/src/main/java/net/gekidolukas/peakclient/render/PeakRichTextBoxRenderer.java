package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakScrollbarStyle;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.gekidolukas.peakclient.ui.client.PeakRichText;
import net.gekidolukas.peakclient.ui.client.PeakRichTextEditor;
import net.gekidolukas.peakclient.ui.client.PeakTextLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakRichTextBoxRenderer implements PeakElementRenderer<PeakRichTextBox> {
    public static final int PADDING_X = 4;
    public static final int PADDING_Y = 3;
    private static final int NEWLINE_MARK = 4;

    @Override
    public PeakPixelSize measureContent(Font font, PeakRichTextBox box) {
        return PeakPixelSize.ZERO;
    }

    public static int lineHeight(Font font, PeakRichTextBox box) {
        return font.lineHeight + box.getLineSpacing();
    }

    public static PeakTextLayout.Measure measureOf(Font font, PeakRichText text) {
        return (from, to) -> font.width(text.serialize(from, to));
    }

    public static int wrapWidth(Font font, PeakRichTextBox box, PeakScrollbarStyle scrollbar, PeakRichTextEditor editor,
                                 int boxWidth, int boxHeight) {
        if (!box.isWrap()) return 0;
        int innerWidth = Math.max(1, boxWidth - PADDING_X * 2);
        int innerHeight = Math.max(1, boxHeight - PADDING_Y * 2);
        PeakTextLayout.Measure measure = measureOf(font, editor.text());
        PeakTextLayout layout = editor.layout(innerWidth, measure);
        if (scrollbar.isVisible() && layout.lineCount() * lineHeight(font, box) > innerHeight) {
            int reserve = scrollbar.getThickness() + scrollbar.getMargin() + 2;
            return Math.max(1, innerWidth - reserve);
        }
        return innerWidth;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakRichTextBox box, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        PeakStyle style = box.getStyle();
        boolean enabled = box.getInteraction() == null || box.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(box);
        boolean hovered = enabled && PeakMenuInteractionState.isHovered(box);
        int w = Math.max(width, 1);
        int h = Math.max(height, 1);

        PeakPaintRenderer.fill(graphics, style.getBackground(), 0, 0, w, h);
        boolean ring = focused && PeakMenuInteractionState.focusVisible(box);
        PeakPaint borderPaint = ring ? PeakMenuInteractionState.focusColor(box)
                : PeakMenuInteractionState.isInvalid(box) ? PeakMenuInteractionState.invalidColor(box)
                : hovered ? PeakPaint.solid(PeakInputColors.BORDER_HOVER)
                : (style.getBorderColor().isNone() ? PeakPaint.solid(PeakInputColors.BORDER) : style.getBorderColor());
        int border = Math.max(1, ring ? Math.max(style.getBorderWidth(), PeakMenuInteractionState.focusWidth(box)) : style.getBorderWidth());
        PeakPaintRenderer.drawFrame(graphics, borderPaint, w, h, border);

        PeakRichTextEditor editor = PeakMenuInteractionState.richEditor(box);
        if (editor == null) {
            editor = new PeakRichTextEditor();
            editor.configure(box.getMaxLength(), box.getMaxLines());
            editor.setValue(box.getValue());
        }
        PeakRichText text = editor.text();
        PeakTextLayout.Measure measure = measureOf(font, text);

        int innerWidth = Math.max(1, w - PADDING_X * 2);
        int innerHeight = Math.max(1, h - PADDING_Y * 2);
        int lineHeight = lineHeight(font, box);
        PeakScrollbarStyle scrollbar = PeakMenuInteractionState.scrollbar(box.getScrollbar());
        int wrapWidth = wrapWidth(font, box, scrollbar, editor, w, h);
        PeakTextLayout layout = editor.layout(wrapWidth, measure);

        int contentHeight = layout.lineCount() * lineHeight;
        int max = Math.max(0, contentHeight - innerHeight);
        int offset = PeakMenuInteractionState.reportScroll(box.getId(), max);

        boolean caretMoved = focused && editor.takeCaretMoved();
        if (caretMoved) {
            int caretLine = layout.lineOf(editor.caret());
            int lineTop = caretLine * lineHeight;
            int wanted = offset;
            if (lineTop < wanted) wanted = lineTop;
            else if (lineTop + font.lineHeight > wanted + innerHeight) wanted = lineTop + font.lineHeight - innerHeight;
            wanted = Math.max(0, Math.min(wanted, max));
            if (wanted != offset) {
                PeakMenuInteractionState.scrollTo(box.getId(), wanted);
                offset = wanted;
            }
            if (!box.isWrap()) {
                int caretX = layout.xOf(editor.caret(), measure);
                int scrollX = editor.scrollX();
                if (caretX - scrollX > innerWidth - 1) scrollX = caretX - innerWidth + 1;
                if (caretX - scrollX < 0) scrollX = caretX;
                editor.scrollX(scrollX);
            }
        }
        int scrollX = box.isWrap() ? 0 : editor.scrollX();

        PeakPaint textPaint = enabled ? box.getColor() : PeakPaint.solid(PeakInputColors.TEXT_DISABLED);

        int scissorLeft = Math.round((ctx.absX() + PADDING_X * ctx.absScale()) * ctx.scissorUnitFactor());
        int scissorRight = Math.round((ctx.absX() + (w - PADDING_X) * ctx.absScale()) * ctx.scissorUnitFactor());
        int scissorTop = Math.round((ctx.absY() + border * ctx.absScale()) * ctx.scissorUnitFactor());
        int scissorBottom = Math.round((ctx.absY() + (h - border) * ctx.absScale()) * ctx.scissorUnitFactor());
        graphics.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);
        try {
            if (text.length() == 0 && !focused) {
                if (!box.getPlaceholder().isEmpty()) {
                    PeakImmediateRender.text(graphics, font, box.getPlaceholder(), PADDING_X, PADDING_Y, 0x707070, false);
                }
            } else {
                drawLines(graphics, font, box, editor, layout, measure, focused, textPaint, offset, scrollX, innerHeight, lineHeight);
            }
        } finally {
            graphics.disableScissor();
        }

        if (max > 0 && scrollbar.isVisible() && PeakMenuInteractionState.isActive()) {
            PeakUiRenderer.drawBoxScrollbar(graphics, box.getId(), scrollbar, 0, 0, w, h, true, offset, max,
                    ctx.absX(), ctx.absY(), ctx.absScale(), ctx.scissorUnitFactor());
        }
    }

    private static void drawLines(GuiGraphics graphics, Font font, PeakRichTextBox box, PeakRichTextEditor editor,
                                  PeakTextLayout layout, PeakTextLayout.Measure measure, boolean focused, PeakPaint textPaint,
                                  int offset, int scrollX, int innerHeight, int lineHeight) {
        PeakRichText text = editor.text();
        int firstLine = Math.max(0, offset / lineHeight);
        int lastLine = Math.min(layout.lineCount() - 1, (offset + innerHeight) / lineHeight);
        int selectionStart = editor.selectionStart();
        int selectionEnd = editor.selectionEnd();
        boolean hasSelection = focused && editor.hasSelection();
        boolean blink = focused && (PeakClientTickCounter.current() / 10) % 2 == 0;
        int caretLine = layout.lineOf(editor.caret());
        int baseX = PADDING_X - scrollX;
        int dropIndex = PeakMenuInteractionState.dropIndex(box);
        int dropLine = dropIndex >= 0 ? layout.lineOf(dropIndex) : -1;

        for (int i = firstLine; i <= lastLine; i++) {
            PeakTextLayout.Line line = layout.line(i);
            int y = PADDING_Y + i * lineHeight - offset;

            if (hasSelection) {
                int a = Math.max(selectionStart, line.start());
                int b = Math.min(selectionEnd, line.end());
                boolean newlineSelected = line.end() < text.length() && text.charAt(line.end()) == '\n'
                        && selectionStart <= line.end() && selectionEnd > line.end();
                if (a < b || newlineSelected) {
                    int x1 = baseX + measure.width(line.start(), Math.min(a, line.end()));
                    int x2 = baseX + measure.width(line.start(), Math.max(b, Math.min(a, line.end())));
                    if (newlineSelected) x2 += NEWLINE_MARK;
                    PeakImmediateRender.fill(graphics, x1, y - 1, x2, y + font.lineHeight, PeakInputColors.SELECTION);
                }
            }

            int x = baseX;
            int lineWidth = Math.max(1, measure.width(line.start(), line.end()));
            for (PeakRichText.Run run : text.runs(line.start(), line.end())) {
                String piece = PeakRichText.flagCodesOf(run.style()) + text.plain(run.start(), run.end());
                if (run.style().color() != 0) {
                    PeakImmediateRender.text(graphics, font, piece, x, y, run.style().color(), false);
                } else {
                    PeakImmediateRender.text(graphics, font, piece, x, y, textPaint, false, baseX, lineWidth);
                }
                x += font.width(piece);
            }

            if (dropIndex >= 0 && i == dropLine) {
                int markerX = baseX + layout.xOf(dropIndex, measure);
                PeakImmediateRender.fill(graphics, markerX, y - 1, markerX + 1, y + font.lineHeight, 0xFF55FFFF);
            }

            if (blink && i == caretLine) {
                int caretX = baseX + layout.xOf(editor.caret(), measure);
                PeakImmediateRender.fill(graphics, caretX, y - 1, caretX + 1, y + font.lineHeight, PeakInputColors.CARET);
            }
        }
    }
}
