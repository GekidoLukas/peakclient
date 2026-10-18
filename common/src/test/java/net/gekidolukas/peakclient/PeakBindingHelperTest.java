package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakBindingHelper;
import net.gekidolukas.peakclient.render.PeakBindingResolver;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.PeakFieldTable;
import net.gekidolukas.peakclient.ui.PeakGradientDirection;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLinearGradient;
import net.gekidolukas.peakclient.ui.PeakRadialGradient;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakBindingHelperTest {
    private static final PeakValueKey<String> TEAM = PeakValueKey.ofString("bindtest:team");

    @AfterEach
    void cleanUp() {
        PeakClientCustomValues.clearNamespace("bindtest");
    }

    @Test
    void paintExpressionsRoundTrip() {
        assertEquals(PeakPaint.solid(0xFF3366CC), PeakPaintAdapter.parse(PeakBindingHelper.paint(0x3366CC)));
        PeakPaint gradient = PeakPaint.gradient(0xFFFF0000, 0xFF0000FF).vertical().animated(20);
        assertEquals(gradient, PeakPaintAdapter.parse(PeakBindingHelper.paint(gradient)));
    }

    @Test
    void theGradientBuilderProducesTheWireFormat() {
        String expression = PeakBindingHelper.gradient()
                .stop(0f, PeakPaint.RED).stop(0.5f, 0xFF00FF00).stop(1f, PeakPaint.BLUE).vertical().animated(30).expression();

        PeakLinearGradient parsed = assertInstanceOf(PeakLinearGradient.class, PeakPaintAdapter.parse(expression));
        assertEquals(3, parsed.getStops().size());
        assertEquals(PeakGradientDirection.TOP_TO_BOTTOM, parsed.getDirection());
        assertEquals(30, parsed.getCycleTicks());
        assertEquals(0xFFFF5555, parsed.getStops().get(0).color());
        assertEquals(0xFF5555FF, parsed.getStops().get(2).color());
    }

    @Test
    void theGradientBuilderKnowsAnglesAndRadialGradients() {
        PeakLinearGradient angled = assertInstanceOf(PeakLinearGradient.class, PeakPaintAdapter.parse(
                PeakBindingHelper.gradient().stop(0f, PeakPaint.RED).stop(1f, PeakPaint.BLUE).angle(45f).expression()));
        assertEquals(45f, angled.getAngle());

        PeakLinearGradient reset = assertInstanceOf(PeakLinearGradient.class, PeakPaintAdapter.parse(
                PeakBindingHelper.gradient().stop(0f, PeakPaint.RED).stop(1f, PeakPaint.BLUE).angle(45f).vertical().expression()));
        assertEquals(PeakGradientDirection.TOP_TO_BOTTOM, reset.getDirection());
        assertTrue(reset.isAxisAligned());

        PeakRadialGradient radial = assertInstanceOf(PeakRadialGradient.class, PeakPaintAdapter.parse(
                PeakBindingHelper.gradient().radial().center(0.2f, 0.8f).radius(0.5f).animated(10)
                        .stop(0f, PeakPaint.RED).stop(1f, PeakPaint.BLUE).expression()));
        assertEquals(0.2f, radial.getCenterX());
        assertEquals(0.8f, radial.getCenterY());
        assertEquals(0.5f, radial.getRadius());
        assertEquals(10, radial.getCycleTicks());
    }

    @Test
    void radialAndAngledPaintsRoundTripThroughTheHelper() {
        PeakPaint radial = PeakRadialGradient.builder().stop(0f, PeakPaint.RED).stop(1f, PeakPaint.BLUE).radius(0.7f).build();
        assertEquals(radial, PeakPaintAdapter.parse(PeakBindingHelper.paint(radial)));
        PeakPaint angled = PeakPaint.gradient(0xFFFF0000, 0xFF0000FF).angle(30f);
        assertEquals(angled, PeakPaintAdapter.parse(PeakBindingHelper.paint(angled)));
    }

    @Test
    void evenlySpreadsTheStops() {
        String expression = PeakBindingHelper.gradient().evenly(PeakPaint.RED, 0xFF00FF00, PeakPaint.BLUE).expression();

        PeakLinearGradient parsed = assertInstanceOf(PeakLinearGradient.class, PeakPaintAdapter.parse(expression));
        assertEquals(0.5f, parsed.getStops().get(1).position());
        assertThrows(IllegalArgumentException.class, () -> PeakBindingHelper.gradient().evenly(PeakPaint.RED));
    }

    @Test
    void placeholdersCanSitInsideGradientStops() {
        String expression = PeakBindingHelper.gradient().stop(0f, TEAM, 0xFFFF0000).stop(1f, PeakPaint.BLACK).expression();

        assertTrue(PeakBindingHelper.containsPlaceholder(expression));
        assertTrue(expression.contains("{bindtest:team##FFFF0000}"), expression);
        assertTrue(PeakBindingHelper.validatePaint(expression).isEmpty());
    }

    @Test
    void validateReportsBrokenExpressions() {
        assertTrue(PeakBindingHelper.validatePaint("not a color").isPresent());
        assertTrue(PeakBindingHelper.validatePaint("{\"stops\":[]}").isPresent());
        assertTrue(PeakBindingHelper.validatePaint("#FF0000").isEmpty());
        assertTrue(PeakBindingHelper.validatePaint("{bindtest:team}").isEmpty());
    }

    @Test
    void textAndPlaceholderHelpers() {
        assertEquals("Team {bindtest:team} wins", PeakBindingHelper.text("Team ", TEAM, " wins"));
        assertEquals("{bindtest:team#none}", PeakBindingHelper.placeholder(TEAM, "none"));
        assertEquals("{bindtest:score#0}", PeakBindingHelper.placeholder("bindtest:score", "0"));
        assertThrows(IllegalArgumentException.class, () -> PeakBindingHelper.placeholder("nokey"));
        assertEquals("{minecraft:player_health}", PeakBindingHelper.builtin("player_health"));
    }

    @Test
    void pathConstantsExistOnTheElements() {
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.STYLE_BACKGROUND));
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.STYLE_BORDER_COLOR));
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.STYLE_SHADOW_COLOR));
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.STYLE_BORDER_WIDTH));
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.STYLE_BORDER_RADIUS));
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.OPACITY));
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.HIDDEN));
        assertTrue(PeakFieldTable.hasPath(PeakPanel.class, PeakBindingHelper.Paths.SCALE));
        assertTrue(PeakFieldTable.hasPath(PeakLabel.class, PeakBindingHelper.Paths.COLOR));
        assertTrue(PeakFieldTable.hasPath(PeakLabel.class, PeakBindingHelper.Paths.TEXT));
    }

    @Test
    void paintFieldsAreDetectedByType() {
        assertTrue(PeakFieldTable.isPaintPath(PeakPanel.class, "style.background"));
        assertTrue(PeakFieldTable.isPaintPath(PeakLabel.class, "color"));
        assertEquals(false, PeakFieldTable.isPaintPath(PeakPanel.class, "opacity"));
        assertEquals(false, PeakFieldTable.isPaintPath(PeakPanel.class, "style.borderWidth"));
    }

    @Test
    void aBoundSolidColorReplacesTheBackground() {
        PeakPanel panel = PeakPanel.create("bind:a").background(0xFF101010).bind("style.background", "{bindtest:team}");
        PeakClientCustomValues.set("bindtest", "team", "#FF8800");

        PeakBindingResolver.applyTree(panel);

        assertEquals(PeakPaint.solid(0xFFFF8800), panel.getStyle().getBackground());
    }

    @Test
    void aBoundGradientReplacesTheBackground() {
        PeakPanel panel = PeakPanel.create("bind:b").background(0xFF101010)
                .bind("style.background", PeakBindingHelper.gradient().stop(0f, TEAM).stop(1f, PeakPaint.BLACK).vertical().expression());
        PeakClientCustomValues.set("bindtest", "team", "#00FF00");

        PeakBindingResolver.applyTree(panel);

        PeakLinearGradient gradient = assertInstanceOf(PeakLinearGradient.class, panel.getStyle().getBackground());
        assertEquals(0xFF00FF00, gradient.getStops().get(0).color());
        assertEquals(PeakGradientDirection.TOP_TO_BOTTOM, gradient.getDirection());
    }

    @Test
    void anUnresolvedPlaceholderKeepsTheBaseValue() {
        PeakPanel panel = PeakPanel.create("bind:c").background(0xFF202020).bind("style.background", "{bindtest:missing}");

        PeakBindingResolver.applyTree(panel);

        assertEquals(PeakPaint.solid(0xFF202020), panel.getStyle().getBackground());
    }

    @Test
    void aBoundLabelColorAcceptsAGradient() {
        PeakLabel label = PeakLabel.create("bind:d", "x")
                .bind("color", PeakBindingHelper.gradient().stop(0f, TEAM, 0xFFFFFFFF).stop(1f, 0xFF0000FF).expression());
        PeakClientCustomValues.set("bindtest", "team", "#FF0000");

        PeakBindingResolver.applyTree(label);

        PeakLinearGradient gradient = assertInstanceOf(PeakLinearGradient.class, label.getColor());
        assertEquals(0xFFFF0000, gradient.getStops().get(0).color());
    }
}
