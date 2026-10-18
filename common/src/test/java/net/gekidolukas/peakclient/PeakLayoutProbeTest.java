package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakLabel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakLayoutProbeTest {
    private static PeakLayoutEntry entry(String id, int depth, int x, int y, int w, int h) {
        return entry(id, depth, x, y, w, h, false, PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MIN,
                PeakLayoutEntry.NO_CLIP_MAX, PeakLayoutEntry.NO_CLIP_MAX);
    }

    private static PeakLayoutEntry entry(String id, int depth, int x, int y, int w, int h, boolean invisible,
                                         int clipLeft, int clipTop, int clipRight, int clipBottom) {
        return new PeakLayoutEntry("doc", id, "PeakLabel", w, h, x, y, depth, PeakLabel.create(id, id), -1,
                w, h, 1f, -1, -1, invisible, clipLeft, clipTop, clipRight, clipBottom);
    }

    @Test
    void parentsFollowTheRecordingOrder() {
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry("a", 1, 0, 0, 100, 100));
        probe.record(entry("a1", 2, 0, 0, 50, 50));
        probe.record(entry("a1x", 3, 0, 0, 10, 10));
        probe.record(entry("a2", 2, 50, 0, 50, 50));
        probe.record(entry("b", 1, 100, 0, 100, 100));

        List<PeakLayoutEntry> entries = probe.entries();
        assertEquals(-1, entries.get(0).parentIndex());
        assertEquals(0, entries.get(1).parentIndex());
        assertEquals(1, entries.get(2).parentIndex());
        assertEquals(0, entries.get(3).parentIndex());
        assertEquals(-1, entries.get(4).parentIndex());
        assertEquals(List.of("a", "a1", "a1x"), probe.chainOf(entries.get(2)).stream().map(PeakLayoutEntry::elementId).toList());
    }

    @Test
    void theLastRecordedElementUnderThePointWins() {
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry("panel", 1, 0, 0, 100, 100));
        probe.record(entry("button", 2, 10, 10, 40, 20));
        probe.record(entry("overlay", 1, 0, 0, 100, 100));

        assertEquals("overlay", probe.findAt(20, 15).elementId());
        assertEquals("button", probe.findAt(20, 15, e -> e.elementId().equals("button")).elementId());
        assertNull(probe.findAt(500, 500));
    }

    @Test
    void clippedAndInvisibleElementsAreNotHit() {
        PeakLayoutProbe probe = new PeakLayoutProbe();
        probe.record(entry("clipped", 1, 0, 0, 100, 100, false, 0, 0, 50, 50));
        probe.record(entry("hidden", 1, 0, 0, 100, 100, true,
                PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MIN, PeakLayoutEntry.NO_CLIP_MAX, PeakLayoutEntry.NO_CLIP_MAX));

        assertEquals("clipped", probe.findAt(25, 25).elementId());
        assertNull(probe.findAt(75, 75));
    }
}
