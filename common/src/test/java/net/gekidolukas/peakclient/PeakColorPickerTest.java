package net.gekidolukas.peakclient;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakColorMath;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakColorPickerLayout;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakRef;
import net.gekidolukas.peakclient.ui.client.PeakColorPickerState;
import net.gekidolukas.peakclient.ui.client.PeakInputValues;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PeakColorPickerTest {
    private static PeakColorPicker roundTrip(PeakColorPicker picker) {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(picker);
        return (PeakColorPicker) PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu)).findById(picker.getId());
    }

    @Test
    void hsvAndRgbConvertBothWays() {
        assertEquals(0xFF0000, PeakColorMath.hsvToRgb(0f, 1f, 1f));
        assertEquals(0x00FF00, PeakColorMath.hsvToRgb(120f, 1f, 1f));
        assertEquals(0x0000FF, PeakColorMath.hsvToRgb(240f, 1f, 1f));
        assertEquals(0xFFFFFF, PeakColorMath.hsvToRgb(77f, 0f, 1f));
        assertEquals(0x000000, PeakColorMath.hsvToRgb(77f, 1f, 0f));
        assertEquals(0xFF0000, PeakColorMath.hsvToRgb(360f, 1f, 1f));

        for (int rgb : new int[]{0xFF8800, 0x123456, 0xABCDEF, 0x808080, 0x010203, 0xFEDCBA}) {
            PeakColorMath.Hsv hsv = PeakColorMath.rgbToHsv(rgb);
            assertEquals(rgb, PeakColorMath.hsvToRgb(hsv.hue(), hsv.saturation(), hsv.value()), "round trip of " + Integer.toHexString(rgb));
        }
    }

    @Test
    void greyAndBlackHaveNoSaturationOrHue() {
        PeakColorMath.Hsv grey = PeakColorMath.rgbToHsv(0x808080);
        PeakColorMath.Hsv black = PeakColorMath.rgbToHsv(0x000000);

        assertEquals(0f, grey.saturation());
        assertEquals(0f, grey.hue());
        assertEquals(0f, black.value());
    }

    @Test
    void hexParsingAcceptsTheCommonFormsOnly() {
        assertEquals(0xFFFF8800, PeakColorMath.tryParseHex("#FF8800", false));
        assertEquals(0xFFFF8800, PeakColorMath.tryParseHex("ff8800", true));
        assertEquals(0xFFFF8800, PeakColorMath.tryParseHex("0xFF8800", false));
        assertEquals(0x80FF8800, PeakColorMath.tryParseHex("#80FF8800", true));
        assertEquals(0xFFFF8800, PeakColorMath.tryParseHex("#00FF8800", true));

        assertNull(PeakColorMath.tryParseHex("#80FF8800", false));
        assertNull(PeakColorMath.tryParseHex("#FF88", true));
        assertNull(PeakColorMath.tryParseHex("#GG8800", false));
        assertNull(PeakColorMath.tryParseHex("", false));
        assertNull(PeakColorMath.tryParseHex(null, true));
    }

    @Test
    void hexFormattingMatchesTheAlphaSetting() {
        assertEquals("#FF8800", PeakColorMath.formatHex(0xFFFF8800, false));
        assertEquals("#80FF8800", PeakColorMath.formatHex(0x80FF8800, true));
        assertEquals("#000001", PeakColorMath.formatHex(0xFF000001, false));
    }

    @Test
    void theValueIsAlwaysOpaqueUnlessAlphaIsOn() {
        PeakColorPicker opaque = PeakColorPicker.create("p", 0x80FF8800);
        PeakColorPicker withAlpha = PeakColorPicker.create("q", 0x80FF8800).alpha(true);
        PeakColorPicker plain = PeakColorPicker.create("r", 0x336699);

        assertEquals(0xFFFF8800, opaque.getValue());
        assertEquals("#FF8800", opaque.inputValue());
        assertEquals(0x80FF8800, withAlpha.getValue());
        assertEquals("#80FF8800", withAlpha.inputValue());
        assertEquals(0xFF336699, plain.getValue());
    }

    @Test
    void thePickerSurvivesTheWire() {
        PeakColorPicker picker = PeakColorPicker.create("ns:picker", 0xCC112233).alpha(true).showHex(false)
                .target("ns:notes").hexField("ns:hex")
                .onChange(PeakAction.send("picked"));

        PeakColorPicker read = roundTrip(picker);

        assertEquals(0xCC112233, read.getValue());
        assertTrue(read.isAlpha());
        assertFalse(read.isShowHex());
        assertEquals("ns:notes", read.getTarget());
        assertEquals("ns:hex", read.getHexField());
        assertEquals("picked", read.actionsFor(PeakEvent.CHANGE).get(0).target());
        assertNotNull(read.getInteraction());
    }

    @Test
    void aDefaultPickerCarriesOnlyItsType() {
        PeakColorPicker read = roundTrip(PeakColorPicker.create("ns:plain"));

        assertEquals(0xFFFFFFFF, read.getValue());
        assertFalse(read.isAlpha());
        assertTrue(read.isShowHex());
        assertEquals("", read.getTarget());
        assertEquals("", read.getHexField());
    }

    @Test
    void patchesChangeTheValueAndTheLinks() {
        PeakColorPicker picker = PeakColorPicker.create("ns:picker");

        picker.applyPatch(PeakPatch.colorPicker().value(0x336699).alpha(true).target("ns:notes").hexField("ns:hex").build());

        assertEquals(0xFF336699, picker.getValue());
        assertTrue(picker.isAlpha());
        assertEquals("ns:notes", picker.getTarget());
        assertEquals("ns:hex", picker.getHexField());
        assertEquals("color_picker", picker.elementType());
        assertEquals("ns:picker", PeakRef.colorPicker("ns:picker").id());
    }

    @Test
    void theSetColorActionCarriesTheTargetAndColor() {
        PeakAction action = PeakAction.setColor("ns:picker", 0xFF8800);

        assertEquals(PeakActionType.SET_COLOR, action.type());
        assertEquals("ns:picker", action.target());
        assertEquals(0xFF8800, action.colorValue());
        assertThrows(IllegalArgumentException.class, () -> PeakAction.setColor("a:b:c", 0));

        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(net.gekidolukas.peakclient.ui.PeakButton.text("ns:swatch", "Orange").onClick(action));
        PeakDocument back = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu));
        PeakAction read = ((net.gekidolukas.peakclient.ui.PeakButton) back.findById("ns:swatch"))
                .actionsFor(PeakEvent.CLICK).get(0);
        assertEquals(PeakActionType.SET_COLOR, read.type());
        assertEquals(0xFF8800, read.colorValue());
    }

    @Test
    void collectedValuesIncludeThePicker() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakColorPicker.create("ns:picker", 0xFF336699));
        PeakDocumentModel model = new PeakDocumentModel(menu);

        Map<String, String> arguments = PeakInputValues.arguments(model, PeakAction.send("save").collectAll(), Map.of());

        assertEquals("#336699", arguments.get("ns:picker"));
    }

    @Test
    void theStateKeepsTheHueForGreysAndFollowsExternalChanges() {
        PeakColorPicker picker = PeakColorPicker.create("p", 0xFFFF0000);
        PeakColorPickerState state = PeakColorPickerState.of(picker);
        assertEquals(0f, state.hue(), 0.01f);

        state.setHue(200f);
        state.setSaturationBrightness(0f, 0.5f);
        picker.value(state.argb(false));
        state.applied(picker.getValue());
        state.sync(picker);
        assertEquals(200f, state.hue(), 0.01f);
        assertEquals(0f, state.saturation());

        picker.value(0xFF00FF00);
        state.sync(picker);
        assertEquals(120f, state.hue(), 0.5f);
        assertEquals(1f, state.saturation(), 0.001f);

        picker.value(0xFF808080);
        state.sync(picker);
        assertEquals(120f, state.hue(), 0.5f);
    }

    @Test
    void theStateBuildsOpaqueOrAlphaColors() {
        PeakColorPickerState state = new PeakColorPickerState();
        state.setHue(240f);
        state.setSaturationBrightness(1f, 1f);
        state.setOpacity(0.5f);

        assertEquals(0xFF0000FF, state.argb(false));
        assertEquals(0x800000FF, state.argb(true));

        state.setOpacity(0f);
        assertEquals(0x010000FF, state.argb(true));
    }

    @Test
    void theLayoutSplitsTheBoxIntoFieldBarsAndFooter() {
        PeakColorPickerLayout plain = PeakColorPickerLayout.of(150, 116, false);
        PeakColorPickerLayout alpha = PeakColorPickerLayout.of(150, 116, true);

        assertEquals(0, plain.field().x());
        assertEquals(150 - PeakColorPickerLayout.BAR_WIDTH, plain.hue().x());
        assertNull(plain.alpha());
        assertTrue(plain.field().right() < plain.hue().x());
        assertTrue(plain.field().bottom() + PeakColorPickerLayout.GAP <= plain.preview().y());
        assertEquals(116, plain.preview().bottom());

        assertNotNull(alpha.alpha());
        assertTrue(alpha.hue().right() < alpha.alpha().x());
        assertTrue(alpha.field().width() < plain.field().width());
    }

    @Test
    void thePartsAreFoundAndTheValuesClamp() {
        PeakColorPickerLayout layout = PeakColorPickerLayout.of(150, 116, true);

        assertEquals(PeakColorPickerLayout.Part.FIELD, layout.partAt(5, 5));
        assertEquals(PeakColorPickerLayout.Part.HUE, layout.partAt(layout.hue().x() + 2, 5));
        assertEquals(PeakColorPickerLayout.Part.ALPHA, layout.partAt(layout.alpha().x() + 2, 5));
        assertEquals(PeakColorPickerLayout.Part.NONE, layout.partAt(5, layout.preview().y() + 2));

        assertEquals(0f, layout.saturationAt(-20));
        assertEquals(1f, layout.saturationAt(500));
        assertEquals(1f, layout.valueAt(-20));
        assertEquals(0f, layout.valueAt(500));
        assertEquals(0f, layout.hueAt(-5));
        assertEquals(360f, layout.hueAt(500));
        assertEquals(1f, layout.alphaAt(-5));
        assertEquals(0f, layout.alphaAt(500));
    }

    @Test
    void markerPositionsStayInsideTheirAreas() {
        PeakColorPickerLayout layout = PeakColorPickerLayout.of(150, 116, true);

        assertEquals(layout.field().x(), layout.fieldX(0f));
        assertEquals(layout.field().right() - 1, layout.fieldX(1f));
        assertEquals(layout.field().y(), layout.fieldY(1f));
        assertEquals(layout.field().bottom() - 1, layout.fieldY(0f));
        assertEquals(layout.hue().y(), layout.hueY(0f));
        assertEquals(layout.hue().bottom() - 1, layout.hueY(360f));
        assertEquals(layout.alpha().y(), layout.alphaY(1f));
    }

    @Test
    void tinyBoxesStayUsable() {
        PeakColorPickerLayout layout = PeakColorPickerLayout.of(10, 10, true);

        assertTrue(layout.field().width() >= PeakColorPickerLayout.MIN_FIELD);
        assertTrue(layout.field().height() >= PeakColorPickerLayout.MIN_FIELD);
        assertNotEquals(PeakColorPickerLayout.Part.NONE, layout.partAt(1, 1));
    }

    @Test
    void patchKeysAreKnown() {
        PeakColorPicker picker = PeakColorPicker.create("p");
        JsonObject patch = PeakPatch.colorPicker().value(1).build();

        assertTrue(picker.isPatchKeyKnown("value"));
        assertTrue(picker.isPatchKeyKnown("hexField"));
        assertTrue(patch.has("value"));
    }
}
