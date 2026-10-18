package net.gekidolukas.peakclient;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.render.PeakScrollbars;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakScrollbarStyle;
import net.gekidolukas.peakclient.ui.client.PeakScrollbarRegion;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakScrollbarsTest {
    @Test
    void thumbSizeFollowsTheVisibleFraction() {
        PeakScrollbars.Geometry half = PeakScrollbars.geometry(100, 100, 0, 2, 8);
        assertEquals(96, half.trackLength());
        assertEquals(48, half.thumbLength());
        assertEquals(2, half.thumbStart());

        PeakScrollbars.Geometry tiny = PeakScrollbars.geometry(100, 10000, 0, 2, 8);
        assertEquals(8, tiny.thumbLength());
    }

    @Test
    void thumbMovesAcrossTheTrackWithTheOffset() {
        PeakScrollbars.Geometry top = PeakScrollbars.geometry(100, 100, 0, 2, 8);
        PeakScrollbars.Geometry middle = PeakScrollbars.geometry(100, 100, 50, 2, 8);
        PeakScrollbars.Geometry bottom = PeakScrollbars.geometry(100, 100, 100, 2, 8);

        assertEquals(2, top.thumbStart());
        assertEquals(2 + 24, middle.thumbStart());
        assertEquals(2 + 48, bottom.thumbStart());
        assertEquals(2 + 96, bottom.thumbStart() + bottom.thumbLength());
    }

    @Test
    void offsetsAreClampedInTheGeometry() {
        PeakScrollbars.Geometry past = PeakScrollbars.geometry(100, 100, 999, 2, 8);
        PeakScrollbars.Geometry before = PeakScrollbars.geometry(100, 100, -5, 2, 8);

        assertEquals(2 + 48, past.thumbStart());
        assertEquals(2, before.thumbStart());
    }

    @Test
    void mouseToOffsetIsTheInverseOfTheGeometry() {
        for (int offset : new int[]{0, 1, 25, 50, 77, 100}) {
            PeakScrollbars.Geometry g = PeakScrollbars.geometry(100, 100, offset, 2, 8);
            int back = PeakScrollbars.offsetForThumbPosition(g.trackLength(), g.thumbLength(), 100, g.thumbStart() - g.trackStart());
            assertTrue(Math.abs(back - offset) <= 2, "offset " + offset + " came back as " + back);
        }
    }

    @Test
    void draggingBeyondTheTrackClamps() {
        assertEquals(0, PeakScrollbars.offsetForThumbPosition(96, 48, 100, -40));
        assertEquals(100, PeakScrollbars.offsetForThumbPosition(96, 48, 100, 400));
        assertEquals(0, PeakScrollbars.offsetForThumbPosition(96, 96, 100, 10));
        assertEquals(0, PeakScrollbars.offsetForThumbPosition(96, 48, 0, 10));
    }

    @Test
    void regionsHitTheTrackWithSlopAndRespectTheClip() {
        PeakScrollbarRegion region = new PeakScrollbarRegion("list", true,
                100, 10, 103, 110, 100, 10, 103, 58, 100, 0, 0, 200, 60);

        assertTrue(region.hitsTrack(101, 50));
        assertTrue(region.hitsTrack(98, 50));
        assertFalse(region.hitsTrack(90, 50));
        assertTrue(region.visibleAt(101, 50));
        assertFalse(region.visibleAt(101, 80));
        assertTrue(region.thumbContainsAxis(20));
        assertFalse(region.thumbContainsAxis(80));
        assertEquals(10, region.trackStart());
        assertEquals(100, region.trackLength());
        assertEquals(48, region.thumbLength());
        assertEquals(0, region.offsetForThumbStart(10));
        assertEquals(100, region.offsetForThumbStart(10 + 52));
    }

    @Test
    void horizontalRegionsUseTheXAxis() {
        PeakScrollbarRegion region = new PeakScrollbarRegion("strip", false,
                10, 50, 110, 53, 10, 50, 60, 53, 40, 0, 0, 400, 400);

        assertEquals(10, region.trackStart());
        assertEquals(100, region.trackLength());
        assertEquals(50, region.thumbLength());
        assertEquals(30, region.axis(30, 70));
    }

    @Test
    void scrollbarStyleDefaultsKeepTheOldLook() {
        PeakScrollbarStyle style = new PeakScrollbarStyle();

        assertTrue(style.isVisible());
        assertEquals(3, style.getThickness());
        assertEquals(2, style.getMargin());
        assertEquals(0, style.getRadius());
        assertEquals(PeakPaint.solid(0x50000000), style.getTrackColor());
        assertEquals(PeakPaint.solid(0xC0DDDDDD), style.getThumbColor());
        assertEquals(PeakPaint.solid(0xC0DDDDDD), style.getThumbHoverColor());
        assertEquals(PeakPaint.solid(0xC0DDDDDD), style.getThumbDragColor());
    }

    @Test
    void scrollbarStyleClampsAndFallsBack() {
        PeakScrollbarStyle style = new PeakScrollbarStyle().thickness(6).radius(10)
                .thumbColor(0x3366FF).thumbHoverColor(0xFFFFFF);

        assertEquals(3, style.getRadius());
        assertEquals(PeakPaint.solid(0xFF3366FF), style.getThumbColor());
        assertEquals(PeakPaint.solid(0xFFFFFFFF), style.getThumbDragColor());
        assertEquals(PeakPaint.solid(0), new PeakScrollbarStyle().noTrack().getTrackColor());
    }

    @Test
    void containersCarryTheScrollbarOverTheWire() {
        PeakPanel list = PeakPanel.create("list").column().overflow(PeakOverflow.SCROLL).size(100, 50);
        list.scrollbar(style -> style.radius(1).thickness(5).thumbColor(0x55AAFF).hidden());
        PeakDocument doc = PeakDocument.create("doc");
        doc.add(list);

        String json = PeakUiSerializer.toJson(doc);
        PeakDocument back = PeakUiSerializer.documentFromJson(json);
        PeakPanel read = (PeakPanel) back.findById("list");

        assertFalse(read.getScrollbar().isVisible());
        assertEquals(5, read.getScrollbar().getThickness());
        assertEquals(PeakPaint.solid(0xFF55AAFF), read.getScrollbar().getThumbColor());

        PeakPanel plain = PeakPanel.create("plain");
        PeakDocument other = PeakDocument.create("other");
        other.add(plain);
        assertTrue(((PeakPanel) PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(other)).findById("plain"))
                .getScrollbar().isVisible());
    }

    @Test
    void scrollbarFieldsArePatchable() {
        PeakPanel list = PeakPanel.create("list").column().overflow(PeakOverflow.SCROLL);

        JsonObject patch = PeakPatch.panel().scrollbarVisible(false).scrollbarThumbColor(0x336699).build();
        list.applyPatch(patch);

        assertFalse(list.getScrollbar().isVisible());
        assertEquals(PeakPaint.solid(0xFF336699), list.getScrollbar().getThumbColor());

        PeakElement<?> same = list;
        same.applyPatch(PeakPatch.panel().field("scrollbar.thickness", 7).build());
        assertEquals(7, list.getScrollbar().getThickness());
        assertTrue(list.isPatchKeyKnown("scrollbar.thumbColor"));
    }
}
