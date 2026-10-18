package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakColorAndShortcutTest {

    @Test
    void normalizeTreatsPlainRgbAsOpaqueAndZeroAsNone() {
        assertEquals(0xFFFF0000, PeakArgb.normalize(0xFF0000));
        assertEquals(0, PeakArgb.normalize(0));
        assertEquals(0x80FF0000, PeakArgb.normalize(0x80FF0000));
        assertEquals(0xFF000000, PeakArgb.normalize(0xFF000000));
    }

    @Test
    void styleColorsFollowTheSameRuleAsTextColors() {
        PeakPanel panel = PeakPanel.create("p").background(0xFF0000).border(1, 0x00FF00);
        assertEquals(PeakPaint.solid(0xFFFF0000), panel.getStyle().getBackground());
        assertEquals(PeakPaint.solid(0xFF00FF00), panel.getStyle().getBorderColor());
        assertEquals(PeakPaint.solid(0xFFFF0000), PeakLabel.create("l", "x").color(0xFF0000).getColor());
        assertEquals(PeakPaint.NONE, PeakPanel.create("q").background(PeakPaint.NONE).getStyle().getBackground());
    }

    @Test
    void hexParsesSixAndEightDigits() {
        assertEquals(0xFF112233, PeakPaint.hex("#112233").argb());
        assertEquals(0xFF112233, PeakPaint.hex("0x112233").argb());
        assertEquals(0x80112233, PeakPaint.hex("#80112233").argb());
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.hex("#12345"));
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.hex("zzzzzz"));
    }

    @Test
    void withAlphaAndRgbaHandleZeroAlpha() {
        assertEquals(0x80FF0000, PeakArgb.withAlpha(0xFFFF0000, 128));
        assertEquals(0x80FF0000, PeakArgb.withAlpha(0xFF0000, 0.5f));
        assertEquals(0, PeakArgb.withAlpha(0xFFFF0000, 0));
        assertEquals(0, PeakArgb.rgba(1, 2, 3, 0));
        assertEquals(0xFF010203, PeakArgb.rgb(1, 2, 3));
    }

    @Test
    void sizeAndPositionShortcuts() {
        PeakLabel label = PeakLabel.create("l", "x").size(40, 80).absolute(PeakAnchor.TOP_RIGHT, -10, 5);
        assertEquals(PeakSize.PeakDimension.Mode.PIXELS, label.getSize().getWidth().mode());
        assertEquals(40f, label.getSize().getWidth().value());
        assertEquals(80f, label.getSize().getHeight().value());
        assertEquals(PeakPositionMode.ABSOLUTE, label.getPositionMode());
        assertEquals(PeakAnchor.TOP_RIGHT, label.getPosition().getAnchor());
        assertEquals(-10, label.getPosition().getOffsetX());

        label.width(10).heightPercent(50f);
        assertEquals(10f, label.getSize().getWidth().value());
        assertEquals(PeakSize.PeakDimension.Mode.PERCENT, label.getSize().getHeight().mode());
    }

    @Test
    void styleShortcuts() {
        PeakPanel panel = PeakPanel.create("p").padding(2, 6).margin(1).border(3, 0x0000FF)
                .borderRadius(4).boxShadow(0x80000000, 1, 1, 2).background(PeakPaint.gradient(0xFFFF0000, 0xFF0000FF));
        PeakStyle style = panel.getStyle();
        assertEquals(2, style.getPadding().getTop());
        assertEquals(6, style.getPadding().getRight());
        assertEquals(6, style.getPadding().getLeft());
        assertEquals(1, style.getMargin().getBottom());
        assertEquals(3, style.getBorderWidth());
        assertEquals(PeakPaint.solid(0xFF0000FF), style.getBorderColor());
        assertEquals(4, style.getBorderRadius());
        assertEquals(PeakPaint.solid(0x80000000), style.getShadowColor());
        assertTrue(style.getBackground() instanceof PeakLinearGradient);
    }

    @Test
    void containerShortcuts() {
        PeakDocument doc = PeakDocument.create("d");
        doc.add(PeakLabel.create("a", "1"), PeakLabel.create("b", "2"), PeakLabel.create("c", "3"));
        assertEquals(3, doc.getChildren().size());
        assertNotNull(doc.findById("c"));
        assertEquals(PeakLayoutDirection.ROW, PeakPanel.create("p").row().getDirection());
        assertEquals(PeakLayoutDirection.COLUMN, PeakPanel.create("q").row().column().getDirection());
    }
}
