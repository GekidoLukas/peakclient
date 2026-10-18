package net.gekidolukas.peakclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakPatchTest {

    private static JsonObject patch(String key, Object value) {
        JsonObject patch = new JsonObject();
        if (value instanceof Number number) patch.addProperty(key, number);
        else if (value instanceof Boolean flag) patch.addProperty(key, flag);
        else patch.addProperty(key, String.valueOf(value));
        return patch;
    }

    @Test
    void labelTextPatchClearsTranslationKey() {
        PeakLabel label = PeakLabel.translatable("gui.done");
        label.applyPatch(patch("text", "Hello"));
        assertEquals("Hello", label.getText());
        assertNull(label.getTranslationKey());
    }

    @Test
    void labelTranslationKeyWithoutArgsResetsArgs() {
        PeakLabel label = PeakLabel.create("x");
        label.applyPatch(patch("translationKey", "gui.cancel"));
        assertEquals("gui.cancel", label.getTranslationKey());
        assertTrue(label.getTranslationArgs().isEmpty());
    }

    @Test
    void opaqueColorFieldsGainAlpha() {
        PeakLabel label = PeakLabel.create("x");
        label.applyPatch(patch("color", 0x00FF00));
        assertEquals(PeakPaint.solid(0xFF00FF00), label.getColor());
    }

    @Test
    void colorOrNoneFieldsTreatPlainRgbAsOpaqueAndZeroAsNone() {
        PeakMinimap minimap = PeakMinimap.create("minecraft:textures/gui/presets/isles.png");
        minimap.applyPatch(patch("outOfBorderColor", 0x00112233));
        assertEquals(PeakPaint.solid(0xFF112233), minimap.getOutOfBorderColor());
        minimap.applyPatch(patch("outOfBorderColor", 0x80112233));
        assertEquals(PeakPaint.solid(0x80112233), minimap.getOutOfBorderColor());
        minimap.applyPatch(patch("outOfBorderColor", 0));
        assertEquals(PeakPaint.solid(0), minimap.getOutOfBorderColor());
    }

    @Test
    void dottedPatchChangesOneStyleFieldOnly() {
        PeakPanel panel = PeakPanel.create("p").padding(5).border(2, 0xFF00FF00).borderRadius(3);
        panel.applyPatch(patch("style.background", 0x112233));
        assertEquals(PeakPaint.solid(0xFF112233), panel.getStyle().getBackground());
        assertEquals(5, panel.getStyle().getPadding().getTop());
        assertEquals(2, panel.getStyle().getBorderWidth());
        assertEquals(3, panel.getStyle().getBorderRadius());
    }

    @Test
    void dottedPatchChangesOneSizeDimension() {
        PeakPanel panel = PeakPanel.create("p").size(40, 80);
        panel.applyPatch(PeakPatch.panel().width(100).build());
        assertEquals(100f, panel.getSize().getWidth().value());
        assertEquals(80f, panel.getSize().getHeight().value());
    }

    @Test
    void typedStylePatchesKeepOtherStyleFields() {
        PeakPanel panel = PeakPanel.create("p").padding(4, 8).border(1, 0xFFFF0000).background(0x202020);
        panel.applyPatch(PeakPatch.panel().background(0x00FF00).boxShadow(0x80000000, 1, 2, 3).build());
        assertEquals(PeakPaint.solid(0xFF00FF00), panel.getStyle().getBackground());
        assertEquals(PeakPaint.solid(0x80000000), panel.getStyle().getShadowColor());
        assertEquals(2, panel.getStyle().getShadowOffsetY());
        assertEquals(4, panel.getStyle().getPadding().getTop());
        assertEquals(8, panel.getStyle().getPadding().getLeft());
        assertEquals(1, panel.getStyle().getBorderWidth());
    }

    @Test
    void unknownDottedPathsAreRejected() {
        PeakPanel panel = PeakPanel.create("p");
        assertTrue(panel.isPatchKeyKnown("style.background"));
        assertTrue(panel.isPatchKeyKnown("size.width"));
        assertFalse(panel.isPatchKeyKnown("style.nope"));
        assertFalse(panel.isPatchKeyKnown("nope.backgroundColor"));
        assertFalse(panel.isPatchKeyKnown("id.length"));
    }

    @Test
    void minimapEdgeIndicatorDistanceIsPatchable() {
        PeakMinimap minimap = PeakMinimap.create("minecraft:textures/gui/presets/isles.png");
        minimap.applyPatch(patch("edgeIndicatorMaxDistance", 321.5));
        assertEquals(321.5, minimap.getEdgeIndicatorMaxDistance());
    }

    @Test
    void entityElementPatchSetsYaw() {
        PeakEntityElement entity = PeakEntityElement.fixed("minecraft:zombie");
        entity.applyPatch(patch("yaw", 45f));
        assertEquals(45f, entity.getYaw());
    }

    @Test
    void classesPatchReplacesTheSet() {
        PeakLabel label = PeakLabel.create("x").addClass("old");
        JsonArray array = new JsonArray();
        array.add("first");
        array.add("second");
        JsonObject patch = new JsonObject();
        patch.add("classes", array);
        label.applyPatch(patch);
        assertEquals(List.of("first", "second"), List.copyOf(label.getClasses()));
    }

    @Test
    void bindingsAreMergedAndRemoved() {
        PeakLabel label = PeakLabel.create("x");
        JsonObject bindings = new JsonObject();
        bindings.addProperty("opacity", "{a:b}");
        JsonObject patch = new JsonObject();
        patch.add("bindings", bindings);
        label.applyPatch(patch);
        assertEquals("{a:b}", label.getBindings().get("opacity"));

        JsonArray removed = new JsonArray();
        removed.add("opacity");
        JsonObject unbind = new JsonObject();
        unbind.add("unbindings", removed);
        label.applyPatch(unbind);
        assertTrue(label.getBindings().isEmpty());
    }

    @Test
    void unknownKeysDoNotThrowAndAreReportedAsUnknown() {
        PeakLabel label = PeakLabel.create("x");
        label.applyPatch(patch("doesNotExist", 1));
        assertFalse(label.isPatchKeyKnown("doesNotExist"));
        assertTrue(label.isPatchKeyKnown("opacity"));
        assertTrue(label.isPatchKeyKnown("bindings"));
    }

    @Test
    void timerPatchKeepsFrozenValueWhenOnlyRunningChanges() {
        PeakTimer timer = PeakTimer.create(100);
        timer.applyPatch(patch("running", false));
        assertEquals(100, timer.currentValue());
        timer.applyPatch(patch("value", 40));
        assertEquals(40, timer.currentValue());
    }

    @Test
    void fieldTableExposesExpectedFields() {
        PeakFieldTable label = PeakFieldTable.of(PeakLabel.class);
        assertTrue(label.has("opacity"));
        assertTrue(label.isOpaqueColor("color"));
        assertFalse(label.has("id"));
        assertFalse(label.has("children"));

        PeakFieldTable minimap = PeakFieldTable.of(PeakMinimap.class);
        assertTrue(minimap.has("borderTextureWidth"));
        assertTrue(minimap.has("edgeIconScaleMaxDistance"));
        assertFalse(minimap.isOpaqueColor("outOfBorderColor"));

        PeakFieldTable iconBar = PeakFieldTable.of(PeakIconBar.class);
        assertTrue(iconBar.has("fallbackFullColor"));
    }
}
