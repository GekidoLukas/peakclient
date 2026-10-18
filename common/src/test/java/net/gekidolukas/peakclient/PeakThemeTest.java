package net.gekidolukas.peakclient;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakFocusStyle;
import net.gekidolukas.peakclient.ui.PeakLinearGradient;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakObjectFit;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakRect;
import net.gekidolukas.peakclient.ui.PeakScrollbarStyle;
import net.gekidolukas.peakclient.ui.PeakSoundSpec;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.gekidolukas.peakclient.ui.PeakTooltipStyle;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakThemeTest {
    private static PeakDocument roundTrip(PeakDocument document) {
        return PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
    }

    @Test
    void defaultsReproduceTheOldLook() {
        PeakMenuSettings settings = PeakMenuSettings.create();

        assertEquals(PeakPaint.solid(0xFFFFFFFF), PeakTheme.focusColor(settings, null));
        assertEquals(1, PeakTheme.focusWidth(settings, null));
        assertTrue(PeakTheme.focusVisible(settings, null));
        assertEquals(PeakPaint.solid(0xF0100010), PeakTheme.tooltip(settings, null).getBackground());
        assertEquals(PeakTooltipStyle.defaultBorder(), PeakTheme.tooltip(settings, null).getBorder());
        assertEquals(1, PeakTheme.tooltip(settings, null).getBorderWidth());
        assertEquals(3, PeakTheme.tooltip(settings, null).getPadding());
        assertEquals("minecraft:ui.button.click", PeakTheme.clickSound(settings, null).getSound());
        assertNull(PeakTheme.hoverSound(settings, null));
        assertEquals(PeakPaint.solid(0x90000000), settings.getBackdrop());
    }

    @Test
    void withoutSettingsTheDefaultsApply() {
        assertEquals(PeakPaint.solid(0xFFFFFFFF), PeakTheme.focusColor(null, null));
        assertEquals(1, PeakTheme.focusWidth(null, null));
        assertNotNull(PeakTheme.tooltip(null, null));
        assertNotNull(PeakTheme.clickSound(null, null));
        assertNull(PeakTheme.hoverSound(null, null));
    }

    @Test
    void theElementOverridesTheMenuAndTheMenuOverridesTheDefault() {
        PeakMenuSettings settings = PeakMenuSettings.create()
                .focus(focus -> focus.color(0xFF55FF55).width(2))
                .clickSound("minecraft:block.note_block.bell", 0.5f, 1.5f);
        PeakInteraction plain = PeakInteraction.create();
        PeakInteraction custom = PeakInteraction.create().focusColor(0xFFFF5555).focusWidth(3)
                .clickSound(PeakSoundSpec.of("minecraft:entity.arrow.hit_player"));

        assertEquals(PeakPaint.solid(0xFF55FF55), PeakTheme.focusColor(settings, plain));
        assertEquals(2, PeakTheme.focusWidth(settings, plain));
        assertEquals(PeakPaint.solid(0xFFFF5555), PeakTheme.focusColor(settings, custom));
        assertEquals(3, PeakTheme.focusWidth(settings, custom));
        assertEquals("minecraft:block.note_block.bell", PeakTheme.clickSound(settings, plain).getSound());
        assertEquals(0.5f, PeakTheme.clickSound(settings, plain).getVolume());
        assertEquals("minecraft:entity.arrow.hit_player", PeakTheme.clickSound(settings, custom).getSound());
    }

    @Test
    void anElementWidthOfZeroKeepsTheMenuWidth() {
        PeakMenuSettings settings = PeakMenuSettings.create().focus(focus -> focus.width(4));
        PeakInteraction interaction = PeakInteraction.create().focusColor(0xFFFFFF00);

        assertEquals(4, PeakTheme.focusWidth(settings, interaction));
        assertEquals(PeakPaint.solid(0xFFFFFF00), PeakTheme.focusColor(settings, interaction));
    }

    @Test
    void theFocusRingCanBeSwitchedOff() {
        PeakMenuSettings settings = PeakMenuSettings.create().focus(PeakFocusStyle::none);
        PeakMenuSettings zeroWidth = PeakMenuSettings.create().focus(focus -> focus.width(0));

        assertFalse(PeakTheme.focusVisible(settings, null));
        assertFalse(PeakTheme.focusVisible(zeroWidth, null));
        assertTrue(PeakTheme.focusVisible(settings, PeakInteraction.create().focusColor(0xFFFFFFFF)));
    }

    @Test
    void silentSwitchesBothSoundsOffAndNoneSilencesTheMenu() {
        PeakMenuSettings settings = PeakMenuSettings.create().hoverSound("minecraft:ui.button.click");
        PeakInteraction silent = PeakInteraction.create().silent(true);

        assertNull(PeakTheme.clickSound(settings, silent));
        assertNull(PeakTheme.hoverSound(settings, silent));
        assertNotNull(PeakTheme.hoverSound(settings, null));
        assertNull(PeakTheme.clickSound(PeakMenuSettings.create().noClickSound(), null));
        assertNotNull(PeakTheme.clickSound(PeakMenuSettings.create().noClickSound(),
                PeakInteraction.create().clickSound(PeakSoundSpec.of("minecraft:ui.button.click"))));
    }

    @Test
    void anElementTooltipStyleWinsOverTheMenuStyle() {
        PeakMenuSettings settings = PeakMenuSettings.create().tooltip(style -> style.background(0xFF000000).padding(6));
        PeakTooltipStyle own = PeakTooltipStyle.create().border(0xFFFF0000).borderWidth(2);
        PeakInteraction interaction = PeakInteraction.create().tooltipStyle(own);

        assertSame(own, PeakTheme.tooltip(settings, interaction));
        assertEquals(6, PeakTheme.tooltip(settings, PeakInteraction.create()).getPadding());
        assertEquals(PeakPaint.solid(0xFFFF0000), own.getBorder());
    }

    @Test
    void tooltipColorsFollowTheColorConvention() {
        PeakTooltipStyle style = PeakTooltipStyle.create().background(0x101020).border(0, 0x00FF00).textColor(0);

        assertEquals(PeakPaint.solid(0xFF101020), style.getBackground());
        PeakLinearGradient border = assertInstanceOf(PeakLinearGradient.class, style.getBorder());
        assertEquals(0, border.getStops().get(0).color());
        assertEquals(0xFF00FF00, border.getStops().get(1).color());
        assertEquals(PeakPaint.solid(0xFF000000), style.getTextColor());
        assertEquals(PeakPaint.NONE, PeakTooltipStyle.create().background(0).getBackground());
    }

    @Test
    void theBackdropRectFollowsTheFit() {
        PeakMenuSettings cover = PeakMenuSettings.create().backdropTexture("t", 100, 100).backdropFit(PeakObjectFit.COVER);
        PeakMenuSettings contain = PeakMenuSettings.create().backdropTexture("t", 100, 100).backdropFit(PeakObjectFit.CONTAIN);
        PeakMenuSettings fill = PeakMenuSettings.create().backdropTexture("t", 100, 100).backdropFit(PeakObjectFit.FILL);
        PeakMenuSettings unknown = PeakMenuSettings.create().backdropTexture("t", 0, 0);

        assertEquals(new PeakRect(0, -50, 200, 200), PeakTheme.backdropRect(cover, 200, 100));
        assertEquals(new PeakRect(50, 0, 100, 100), PeakTheme.backdropRect(contain, 200, 100));
        assertEquals(new PeakRect(0, 0, 200, 100), PeakTheme.backdropRect(fill, 200, 100));
        assertEquals(new PeakRect(0, 0, 200, 100), PeakTheme.backdropRect(unknown, 200, 100));
    }

    @Test
    void menuSettingsSurviveTheWireFormat() {
        PeakDocument menu = PeakMenu.create("ns:menu", settings -> settings
                .focus(focus -> focus.color(0xFF00FFFF).width(2))
                .tooltip(style -> style.background(0xFF000000).border(0xFFFFFFFF).padding(5).maxWidth(120).shadow(false))
                .clickSound("minecraft:block.note_block.pling", 0.8f, 1.2f)
                .hoverSound("minecraft:ui.button.click")
                .backdrop(PeakPaint.gradient(0xFF000010, 0xFF100000))
                .backdropTexture("ns:textures/bg.png", 320, 180)
                .backdropFit(PeakObjectFit.CONTAIN)
                .backdropBlur(true)
                .backdropPanorama("ns:textures/gui/panorama", 2.5f)
                .backdropPanoramaPitch(-12f)
                .backdropPanoramaOverlay(false));

        PeakMenuSettings back = roundTrip(menu).getMenu();

        assertEquals(PeakPaint.solid(0xFF00FFFF), back.getFocus().getColor());
        assertEquals(2, back.getFocus().getWidth());
        assertEquals(PeakPaint.solid(0xFF000000), back.getTooltip().getBackground());
        assertEquals(PeakPaint.solid(0xFFFFFFFF), back.getTooltip().getBorder());
        assertEquals(5, back.getTooltip().getPadding());
        assertEquals(120, back.getTooltip().getMaxWidth());
        assertFalse(back.getTooltip().hasShadow());
        assertEquals("minecraft:block.note_block.pling", back.getClickSound().getSound());
        assertEquals(0.8f, back.getClickSound().getVolume());
        assertEquals(1.2f, back.getClickSound().getPitch());
        assertEquals("minecraft:ui.button.click", back.getHoverSound().getSound());
        assertInstanceOf(PeakLinearGradient.class, back.getBackdrop());
        assertEquals("ns:textures/bg.png", back.getBackdropTexture());
        assertEquals(320, back.getBackdropTextureWidth());
        assertEquals(180, back.getBackdropTextureHeight());
        assertEquals(PeakObjectFit.CONTAIN, back.getBackdropFit());
        assertTrue(back.isBackdropBlur());
        assertEquals("ns:textures/gui/panorama", back.getBackdropPanorama());
        assertEquals(2.5f, back.getBackdropPanoramaSpeed());
        assertEquals(-12f, back.getBackdropPanoramaPitch());
        assertFalse(back.isBackdropPanoramaOverlay());
    }

    @Test
    void thePanoramaShortcutsSetAndClearThePath() {
        PeakMenuSettings settings = PeakMenuSettings.create().vanillaPanorama();

        assertEquals("minecraft:textures/gui/title/background/panorama", settings.getBackdropPanorama());
        assertEquals("", settings.noPanorama().getBackdropPanorama());
        assertEquals("", settings.backdropPanorama(null).getBackdropPanorama());
        assertEquals(3f, PeakMenuSettings.create().backdropPanorama("ns:p", 3f).getBackdropPanoramaSpeed());
    }

    @Test
    void thePanoramaSpinStaysWithinOneTurn() {
        assertEquals(10f, net.gekidolukas.peakclient.ui.client.PeakPanoramas.wrap(370f), 0.001f);
        assertEquals(350f, net.gekidolukas.peakclient.ui.client.PeakPanoramas.wrap(-10f), 0.001f);
        assertEquals(0f, net.gekidolukas.peakclient.ui.client.PeakPanoramas.wrap(0f), 0.001f);
    }

    @Test
    void aMenuWithoutThemeKeepsTheDefaultsAfterTheWire() {
        PeakDocument menu = PeakMenu.create("ns:plain");

        PeakMenuSettings back = roundTrip(menu).getMenu();

        assertEquals(PeakPaint.solid(PeakFocusStyle.DEFAULT_COLOR), back.getFocus().getColor());
        assertEquals(PeakPaint.solid(0xF0100010), back.getTooltip().getBackground());
        assertEquals("minecraft:ui.button.click", back.getClickSound().getSound());
        assertNull(back.getHoverSound());
        assertEquals("", back.getBackdropTexture());
        assertFalse(back.isBackdropBlur());
        assertEquals("", back.getBackdropPanorama());
        assertEquals(1f, back.getBackdropPanoramaSpeed());
        assertEquals(10f, back.getBackdropPanoramaPitch());
        assertTrue(back.isBackdropPanoramaOverlay());
        assertEquals(PeakPaint.solid(0x90000000), back.getBackdrop());
    }

    @Test
    void noBackdropAndNoFocusSurviveTheWire() {
        PeakDocument menu = PeakMenu.create("ns:bare", settings -> settings.noBackdrop().focus(PeakFocusStyle::none).noClickSound());

        PeakMenuSettings back = roundTrip(menu).getMenu();

        assertEquals(PeakPaint.NONE, back.getBackdrop());
        assertEquals(PeakPaint.NONE, back.getFocus().getColor());
        assertTrue(back.getClickSound().isNone());
    }

    @Test
    void elementOverridesSurviveTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakButton.text("ns:b", "B")
                .focusColor(0xFFFF8800).focusWidth(3)
                .tooltipStyle(PeakTooltipStyle.create().background(0xFF202020).border(0xFFFFAA00).maxWidth(90))
                .clickSound("minecraft:entity.arrow.hit_player", 1f, 0.7f)
                .hoverSound("minecraft:block.note_block.hat"));

        PeakInteraction read = ((PeakButton) roundTrip(menu).findById("ns:b")).getInteraction();

        assertEquals(PeakPaint.solid(0xFFFF8800), read.getFocusColor());
        assertEquals(3, read.getFocusWidth());
        assertEquals(PeakPaint.solid(0xFF202020), read.getTooltipStyle().getBackground());
        assertEquals(PeakPaint.solid(0xFFFFAA00), read.getTooltipStyle().getBorder());
        assertEquals(90, read.getTooltipStyle().getMaxWidth());
        assertEquals("minecraft:entity.arrow.hit_player", read.getClickSound().getSound());
        assertEquals(0.7f, read.getClickSound().getPitch());
        assertEquals("minecraft:block.note_block.hat", read.getHoverSound().getSound());
    }

    @Test
    void anElementWithoutOverridesCarriesNothingExtra() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakButton.text("ns:b", "B"));

        JsonObject json = PeakUiSerializer.WIRE.toJsonTree(menu, net.gekidolukas.peakclient.ui.PeakElement.class).getAsJsonObject();
        String text = json.toString();

        assertFalse(text.contains("focusColor"));
        assertFalse(text.contains("tooltipStyle"));
        assertFalse(text.contains("hoverSound"));
    }

    @Test
    void elementOverridesCanBePatchedByPath() {
        PeakButton button = PeakButton.text("ns:b", "B");
        JsonObject patch = new JsonObject();
        patch.addProperty("interaction.focusColor", 0xFF123456);
        patch.addProperty("interaction.focusWidth", 2);

        button.applyPatch(patch);

        assertEquals(PeakPaint.solid(0xFF123456), button.getInteraction().getFocusColor());
        assertEquals(2, button.getInteraction().getFocusWidth());
    }
    @Test
    void theScrollbarDefaultsApplyWithoutAThemeOrAHud() {
        PeakScrollbarStyle resolved = PeakTheme.scrollbar(null, new PeakScrollbarStyle());

        assertTrue(resolved.isVisible());
        assertEquals(3, resolved.getThickness());
        assertEquals(2, resolved.getMargin());
        assertEquals(PeakPaint.solid(0x50000000), resolved.getTrackColor());
        assertEquals(PeakPaint.solid(0xC0DDDDDD), resolved.getThumbColor());
        assertEquals(resolved.getThumbColor(), resolved.getThumbHoverColor());
    }

    @Test
    void theScrollbarThemeFillsEveryFieldTheContainerLeavesOpen() {
        PeakMenuSettings settings = PeakMenuSettings.create()
                .scrollbar(bar -> bar.thickness(6).radius(3).margin(4).thumbColor(0x55FF55).trackColor(0x202020).minThumbSize(14));

        PeakScrollbarStyle resolved = PeakTheme.scrollbar(settings, new PeakScrollbarStyle().thumbColor(0xFF8800));

        assertEquals(6, resolved.getThickness());
        assertEquals(3, resolved.getRadius());
        assertEquals(4, resolved.getMargin());
        assertEquals(14, resolved.getMinThumbSize());
        assertEquals(PeakPaint.solid(0xFF202020), resolved.getTrackColor());
        assertEquals(PeakPaint.solid(0xFFFF8800), resolved.getThumbColor());
    }

    @Test
    void anExplicitNoTrackBeatsTheThemeTrack() {
        PeakMenuSettings settings = PeakMenuSettings.create().scrollbar(bar -> bar.trackColor(0x303030));

        assertEquals(PeakPaint.solid(0), PeakTheme.scrollbar(settings, new PeakScrollbarStyle().noTrack()).getTrackColor());
        assertEquals(PeakPaint.solid(0xFF303030), PeakTheme.scrollbar(settings, new PeakScrollbarStyle()).getTrackColor());
    }

    @Test
    void hoverAndDragColorsFollowTheResolvedThumbColor() {
        PeakMenuSettings settings = PeakMenuSettings.create().scrollbar(bar -> bar.thumbColor(0x112233));
        PeakMenuSettings withHover = PeakMenuSettings.create()
                .scrollbar(bar -> bar.thumbColor(0x112233).thumbHoverColor(0x445566));

        PeakScrollbarStyle plain = PeakTheme.scrollbar(settings, new PeakScrollbarStyle());
        PeakScrollbarStyle own = PeakTheme.scrollbar(withHover, new PeakScrollbarStyle().thumbColor(0x778899));

        assertEquals(PeakPaint.solid(0xFF112233), plain.getThumbHoverColor());
        assertEquals(PeakPaint.solid(0xFF112233), plain.getThumbDragColor());
        assertEquals(PeakPaint.solid(0xFF778899), own.getThumbColor());
        assertEquals(PeakPaint.solid(0xFF445566), own.getThumbHoverColor());
        assertEquals(PeakPaint.solid(0xFF445566), own.getThumbDragColor());
    }

    @Test
    void aThemeCanHideAllScrollbarsAndAContainerCanShowItAgain() {
        PeakMenuSettings settings = PeakMenuSettings.create().hideScrollbars();

        assertFalse(PeakTheme.scrollbar(settings, new PeakScrollbarStyle()).isVisible());
        assertTrue(PeakTheme.scrollbar(settings, new PeakScrollbarStyle().visible(true)).isVisible());
        assertFalse(PeakTheme.scrollbar(PeakMenuSettings.create(), new PeakScrollbarStyle().hidden()).isVisible());
    }

    @Test
    void theScrollbarThemeSurvivesTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu", settings -> settings
                .scrollbar(bar -> bar.thickness(7).radius(2).thumbColor(0x00FF00).noTrack().hidden()));

        PeakScrollbarStyle back = roundTrip(menu).getMenu().getScrollbar();

        assertFalse(back.isVisible());
        assertEquals(7, back.getThickness());
        assertEquals(2, back.getRadius());
        assertEquals(PeakPaint.solid(0xFF00FF00), back.getThumbColor());
        assertEquals(PeakPaint.solid(0), back.getTrackColor());
    }

    @Test
    void aMenuWithoutAScrollbarThemeResolvesToTheDefaults() {
        PeakScrollbarStyle back = roundTrip(PeakMenu.create("ns:plain")).getMenu().getScrollbar();

        assertTrue(back.isVisible());
        assertEquals(3, back.getThickness());
        assertEquals(PeakPaint.solid(0x50000000), back.getTrackColor());
    }

    @Test
    void aContainerWithOnlyOneFieldSetKeepsTheRestOpenOverTheWire() {
        PeakDocument menu = PeakMenu.create("ns:menu");
        menu.add(PeakPanel.create("ns:list").column().overflow(PeakOverflow.SCROLL)
                .scrollbar(bar -> bar.thumbColor(0xFF0000)));

        PeakPanel read = (PeakPanel) roundTrip(menu).findById("ns:list");
        PeakMenuSettings theme = PeakMenuSettings.create().scrollbar(bar -> bar.thickness(5));
        PeakScrollbarStyle resolved = PeakTheme.scrollbar(theme, read.getScrollbar());

        assertEquals(5, resolved.getThickness());
        assertEquals(PeakPaint.solid(0xFFFF0000), resolved.getThumbColor());
        assertTrue(resolved.isVisible());
    }
}
