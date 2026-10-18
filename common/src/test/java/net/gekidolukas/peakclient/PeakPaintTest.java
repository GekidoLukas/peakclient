package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakColorStop;
import net.gekidolukas.peakclient.ui.PeakColorStops;
import net.gekidolukas.peakclient.ui.PeakGradientDirection;
import net.gekidolukas.peakclient.ui.PeakLinearGradient;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakRadialGradient;
import net.gekidolukas.peakclient.ui.PeakSolidColor;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakPaintTest {
    private static final class Holder {
        PeakPaint first;
        PeakPaint second;
    }

    @Test
    void solidColorsFollowTheColorConvention() {
        assertEquals(0xFFFF0000, PeakPaint.solid(0xFF0000).argb());
        assertEquals(0x80FF0000, PeakPaint.solid(0x80FF0000).argb());
        assertEquals(0, PeakPaint.solid(0).argb());
        assertTrue(PeakPaint.solid(0).isNone());
        assertTrue(PeakPaint.NONE.isNone());
        assertFalse(PeakPaint.RED.isNone());
        assertEquals(0, PeakPaint.rgba(10, 20, 30, 0).argb());
        assertEquals(0xFF0A141E, PeakPaint.rgb(10, 20, 30).argb());
    }

    @Test
    void hexAcceptsSixAndEightDigits() {
        assertEquals(0xFF00AA00, PeakPaint.hex("#00AA00").argb());
        assertEquals(0x8000AA00, PeakPaint.hex("0x8000AA00").argb());
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.hex("#12"));
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.hex("zz"));
    }

    @Test
    void opaqueAndNoneRulesApplyToEveryColor() {
        PeakSolidColor alphaZero = new PeakSolidColor(0x00FF0000);
        assertEquals(0xFFFF0000, alphaZero.opaque().argb());
        assertEquals(0xFF000000, PeakPaint.NONE.opaque().argb());
        assertEquals(0xFFFF0000, alphaZero.orNone().argb());
        assertEquals(0, PeakPaint.NONE.orNone().argb());

        PeakLinearGradient gradient = PeakLinearGradient.builder().stop(0f, 0).stop(1f, 0xFF00FF00).build();
        assertEquals(0, gradient.firstColor());
        assertEquals(0xFF000000, gradient.opaque().firstColor());
    }

    @Test
    void withAlphaReplacesTheAlphaOfEveryStop() {
        PeakLinearGradient gradient = PeakPaint.gradient(0xFFFF0000, 0xFF0000FF);
        PeakLinearGradient faded = gradient.withAlpha(0.5f);
        assertEquals(0x80FF0000, faded.getStops().get(0).color());
        assertEquals(0x800000FF, faded.getStops().get(1).color());
        assertEquals(0x80FFFFFF, PeakPaint.WHITE.withAlpha(0.5f).argb());
    }

    @Test
    void gradientsSampleBetweenTheirStops() {
        PeakLinearGradient gradient = PeakPaint.gradient(0xFF000000, 0xFFFFFFFF);
        assertEquals(0xFF000000, gradient.colorAt(0f));
        assertEquals(0xFFFFFFFF, gradient.colorAt(1f));
        assertEquals(0xFF808080, gradient.colorAt(0.5f));
        assertEquals(0xFF000000, gradient.colorAt(-3f));
        assertEquals(0xFFFFFFFF, gradient.colorAt(7f));
    }

    @Test
    void stopsAreSortedAndPositionsRespected() {
        PeakLinearGradient gradient = PeakLinearGradient.builder()
                .stop(1f, 0xFF0000FF)
                .stop(0f, 0xFFFF0000)
                .stop(0.5f, 0xFF00FF00)
                .build();
        assertEquals(0xFFFF0000, gradient.firstColor());
        assertEquals(0xFF00FF00, gradient.colorAt(0.5f));
        assertEquals(List.of(0f, 0.5f, 1f), gradient.getStops().stream().map(PeakColorStop::position).toList());
    }

    @Test
    void aGradientNeedsAStop() {
        assertThrows(IllegalArgumentException.class, () -> PeakLinearGradient.builder().build());
    }

    @Test
    void animationIsPartOfTheValue() {
        PeakLinearGradient still = PeakPaint.gradient(0xFFFF0000, 0xFF0000FF);
        PeakLinearGradient animated = still.animated(60);
        assertFalse(still.isAnimated());
        assertTrue(animated.isAnimated());
        assertEquals(60, animated.getCycleTicks());
        assertNotEquals(still, animated);
        assertEquals(still, animated.still());
        assertEquals(PeakGradientDirection.TOP_TO_BOTTOM, still.vertical().getDirection());
    }

    private static PeakLinearGradient blackToWhite() {
        return PeakLinearGradient.builder().stop(0f, 0xFF000000).stop(1f, 0xFFFFFFFF).build();
    }

    private static int level(int argb) {
        return argb & 0xFF;
    }

    @Test
    void anAxisAlignedAngleIsTheSameAsADirection() {
        assertTrue(blackToWhite().isAxisAligned());
        assertEquals(blackToWhite().vertical(), blackToWhite().angle(90f));
        assertEquals(blackToWhite(), blackToWhite().angle(0f));
        assertEquals(blackToWhite(), blackToWhite().angle(360f));
        assertEquals(blackToWhite().vertical(), blackToWhite().angle(-270f));
        assertFalse(blackToWhite().angle(45f).isAxisAligned());
        assertEquals(PeakGradientDirection.TOP_TO_BOTTOM, blackToWhite().angle(90f).getDirection());
        assertEquals(90f, blackToWhite().vertical().getAngle());
        assertEquals(45f, blackToWhite().angle(45f).getAngle());
    }

    @Test
    void angledGradientsReachTheirEndsAtTheCorners() {
        PeakLinearGradient diagonal = blackToWhite().angle(45f);

        assertEquals(0, level(diagonal.colorAt(0f, 0f, 1f)), 1);
        assertEquals(255, level(diagonal.colorAt(1f, 1f, 1f)), 1);
        assertEquals(127, level(diagonal.colorAt(0.5f, 0.5f, 1f)), 2);
        assertEquals(level(diagonal.colorAt(1f, 0f, 1f)), level(diagonal.colorAt(0f, 1f, 1f)), 1);

        PeakLinearGradient wide = blackToWhite().angle(45f);
        assertEquals(0, level(wide.colorAt(0f, 0f, 3f)), 1);
        assertEquals(255, level(wide.colorAt(1f, 1f, 3f)), 1);
    }

    @Test
    void reversedAnglesRunBackwards() {
        PeakLinearGradient backwards = blackToWhite().angle(180f);

        assertEquals(255, level(backwards.colorAt(0f, 0.5f, 2f)), 1);
        assertEquals(0, level(backwards.colorAt(1f, 0.5f, 2f)), 1);
        PeakLinearGradient up = blackToWhite().angle(270f);
        assertEquals(255, level(up.colorAt(0.5f, 0f, 1f)), 1);
        assertEquals(0, level(up.colorAt(0.5f, 1f, 1f)), 1);
    }

    @Test
    void axisAlignedGradientsKeepSamplingByAxis() {
        PeakLinearGradient horizontal = blackToWhite();
        assertEquals(horizontal.colorAt(0.25f), horizontal.colorAt(0.25f, 0.9f, 4f));
        PeakLinearGradient vertical = blackToWhite().vertical();
        assertEquals(vertical.colorAt(0.75f), vertical.colorAt(0.1f, 0.75f, 4f));
    }

    @Test
    void radialGradientsGrowFromTheCenter() {
        PeakRadialGradient radial = PeakRadialGradient.builder().stop(0f, 0xFF000000).stop(1f, 0xFFFFFFFF).build();

        assertEquals(0, level(radial.colorAt(0.5f, 0.5f, 1f)));
        assertEquals(255, level(radial.colorAt(0f, 0f, 1f)), 1);
        assertEquals(255, level(radial.colorAt(1f, 1f, 1f)), 1);
        assertTrue(level(radial.colorAt(0.5f, 0f, 1f)) < level(radial.colorAt(0f, 0f, 1f)));
        assertEquals(level(radial.colorAt(0.5f, 0f, 1f)), level(radial.colorAt(0f, 0.5f, 1f)), 1);
    }

    @Test
    void radialRadiusIsAFractionOfTheShorterSide() {
        PeakRadialGradient radial = PeakRadialGradient.builder().stop(0f, 0xFF000000).stop(1f, 0xFFFFFFFF).radius(0.5f).build();

        assertEquals(255, level(radial.colorAt(0.5f, 0f, 1f)), 1);
        assertEquals(255, level(radial.colorAt(1f, 0.5f, 1f)), 1);
        assertEquals(255, level(radial.colorAt(0.5f, 0f, 3f)), 1);
        assertEquals(0, level(radial.colorAt(0.5f, 0.5f, 3f)));
        assertEquals(127, level(radial.colorAt(0.5f, 0.25f, 1f)), 2);
    }

    @Test
    void radialCentersMoveTheOrigin() {
        PeakRadialGradient radial = PeakRadialGradient.builder().stop(0f, 0xFF000000).stop(1f, 0xFFFFFFFF).center(0f, 0f).build();

        assertEquals(0, level(radial.colorAt(0f, 0f, 1f)));
        assertEquals(255, level(radial.colorAt(1f, 1f, 1f)), 1);
    }

    @Test
    void angleAndRadialGradientsRoundTripAndOmitDefaults() {
        PeakLinearGradient angled = PeakLinearGradient.builder().stop(0f, PeakPaint.RED).stop(1f, PeakPaint.BLUE).angle(30f).animated(25).build();
        assertEquals(angled, PeakPaint.parse(angled.toExpression()));
        assertTrue(angled.toExpression().contains("\"angle\":30"));
        assertFalse(angled.toExpression().contains("direction"));
        assertFalse(blackToWhite().toExpression().contains("angle"));

        PeakRadialGradient radial = PeakRadialGradient.builder().stop(0f, PeakPaint.RED).stop(1f, PeakPaint.BLUE)
                .center(0.25f, 0.75f).radius(0.6f).animated(15).build();
        assertEquals(radial, PeakPaint.parse(radial.toExpression()));
        PeakRadialGradient plain = PeakRadialGradient.of(0xFFFF0000, 0xFF0000FF);
        assertFalse(plain.toExpression().contains("center"));
        assertFalse(plain.toExpression().contains("radius"));
        assertTrue(plain.toExpression().contains("\"type\":\"radial\""));
        assertEquals(plain, PeakPaint.parse(plain.toExpression()));
    }

    @Test
    void anAngleWinsOverADirectionOnTheWire() {
        PeakPaint parsed = PeakPaint.parse("{\"stops\":[[0,\"#FF0000\"],[1,\"#0000FF\"]],\"direction\":\"vertical\",\"angle\":135}");

        assertEquals(135f, assertInstanceOf(PeakLinearGradient.class, parsed).getAngle());
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.parse("{\"type\":\"cone\",\"stops\":[[0,1]]}"));
    }

    @Test
    void newGradientsTransformLikeTheLinearOnes() {
        PeakRadialGradient radial = PeakRadialGradient.of(0x00FF0000, 0xFF0000FF);
        assertEquals(0xFF000000, radial.opaque().firstColor() & 0xFF000000);
        assertEquals(0x80, radial.orNone().withAlpha(0.5f).firstColor() >>> 24, 2);
        assertFalse(radial.isSolid());
        assertFalse(radial.isNone());
        assertEquals(radial.animated(10).isAnimated(), true);
        assertEquals(blackToWhite().angle(30f).animated(10).still(), blackToWhite().angle(30f));
        assertEquals(30f, blackToWhite().angle(30f).opaque().getAngle());
    }

    @Test
    void angledPaintsSurviveTheWireFormatOfAField() {
        Holder holder = new Holder();
        holder.first = blackToWhite().angle(60f);
        holder.second = PeakRadialGradient.builder().stop(0f, PeakPaint.RED).stop(1f, PeakPaint.BLUE).radius(0.5f).build();

        Holder read = PeakUiSerializer.GSON.fromJson(PeakUiSerializer.GSON.toJson(holder), Holder.class);

        assertEquals(holder.first, read.first);
        assertEquals(holder.second, read.second);
    }

    @Test
    void aLoopingGradientAtPhaseZeroLooksLikeTheStillOne() {
        List<PeakColorStop> stops = PeakLinearGradient.builder()
                .stop(0f, 0xFFFF0000).stop(0.5f, 0xFF00FF00).stop(1f, 0xFF0000FF).build().getStops();
        PeakLinearGradient still = PeakLinearGradient.of(stops, PeakGradientDirection.LEFT_TO_RIGHT, 0);

        for (float position = 0f; position <= 1f; position += 0.125f) {
            assertEquals(still.colorAt(position), PeakColorStops.sampleLoop(stops, position, 0f), "position " + position);
        }
    }

    @Test
    void aLoopingGradientHasNoHardSeam() {
        List<PeakColorStop> stops = PeakLinearGradient.builder().stop(0f, 0xFFFF0000).stop(1f, 0xFF0000FF).build().getStops();
        float period = 2f;

        for (float phase = 0f; phase < 1f; phase += 0.05f) {
            int previous = PeakColorStops.sampleLoop(stops, 0f, phase);
            for (float position = 0.01f; position <= 1f; position += 0.01f) {
                int current = PeakColorStops.sampleLoop(stops, position, phase);
                assertTrue(Math.abs((current & 0xFF) - (previous & 0xFF)) <= 20, "jump at " + position + " phase " + phase);
                assertTrue(Math.abs(((current >> 16) & 0xFF) - ((previous >> 16) & 0xFF)) <= 20, "jump at " + position + " phase " + phase);
                previous = current;
            }
        }
        assertEquals(2f, period);
    }

    @Test
    void aLoopingGradientRepeatsAfterOnePhase() {
        List<PeakColorStop> stops = PeakLinearGradient.builder()
                .stop(0f, 0xFFFF0000).stop(0.5f, 0xFF00FF00).stop(1f, 0xFF0000FF).build().getStops();

        for (float position = 0f; position <= 1f; position += 0.1f) {
            int start = PeakColorStops.sampleLoop(stops, position, 0.25f);
            int after = PeakColorStops.sampleLoop(stops, position, 1.25f);
            assertEquals(start, after, "position " + position);
        }
    }

    @Test
    void aGradientThatEndsInItsFirstColorLoopsWithoutAnExtraGap() {
        List<PeakColorStop> stops = PeakLinearGradient.builder()
                .stop(0f, 0xFFFF0000).stop(0.5f, 0xFF0000FF).stop(1f, 0xFFFF0000).build().getStops();

        assertEquals(PeakColorStops.sampleLoop(stops, 0f, 0f), PeakColorStops.sampleLoop(stops, 1f, 0f));
        assertEquals(PeakColorStops.sampleLoop(stops, 0.25f, 0f), PeakColorStops.sampleLoop(stops, 0.25f, 1f));
        assertEquals(0xFF0000FF, PeakColorStops.sampleLoop(stops, 0.5f, 0f));
    }

    @Test
    void expressionsRoundTrip() {
        PeakPaint solid = PeakPaint.rgba(10, 20, 30, 200);
        assertEquals(solid, PeakPaint.parse(solid.toExpression()));
        assertEquals(PeakPaint.NONE, PeakPaint.parse(PeakPaint.NONE.toExpression()));

        PeakPaint gradient = PeakLinearGradient.builder()
                .stop(0f, PeakPaint.RED).stop(0.25f, PeakPaint.YELLOW).stop(1f, PeakPaint.GREEN)
                .vertical().animated(30).build();
        assertEquals(gradient, PeakPaint.parse(gradient.toExpression()));
    }

    @Test
    void expressionsAcceptNumbersAndColorStrings() {
        assertEquals(0xFF336699, ((PeakSolidColor) PeakPaint.parse("#336699")).argb());
        assertEquals(0x80336699, ((PeakSolidColor) PeakPaint.parse("#80336699")).argb());
        assertEquals(0xFF336699, ((PeakSolidColor) PeakPaint.parse("0x336699")).argb());
        assertEquals(0xFF336699, ((PeakSolidColor) PeakPaint.parse(Integer.toString(0xFF336699))).argb());

        PeakPaint gradient = PeakPaint.parse("{\"stops\":[[0,\"#FF0000\"],[1,\"#0000FF\"]],\"direction\":\"vertical\",\"cycle\":20}");
        PeakLinearGradient parsed = assertInstanceOf(PeakLinearGradient.class, gradient);
        assertEquals(0xFFFF0000, parsed.firstColor());
        assertEquals(PeakGradientDirection.TOP_TO_BOTTOM, parsed.getDirection());
        assertEquals(20, parsed.getCycleTicks());
    }

    @Test
    void invalidExpressionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.parse(""));
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.parse("not a color"));
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.parse("{\"stops\":[]}"));
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.parse("{\"stops\":[[0]]}"));
        assertThrows(IllegalArgumentException.class, () -> PeakPaint.parse("{\"stops\":[[0,1]],\"direction\":\"diagonal\"}"));
    }

    @Test
    void theWireFormatKeepsSolidColorsAsNumbers() {
        Holder holder = new Holder();
        holder.first = PeakPaint.solid(0xFF3366);
        holder.second = PeakLinearGradient.builder().stop(0f, 0xFFFF0000).stop(1f, 0xFF0000FF).vertical().animated(10).build();

        String json = PeakUiSerializer.GSON.toJson(holder);
        assertTrue(json.contains("\"first\":" + 0xFFFF3366), json);

        Holder read = PeakUiSerializer.GSON.fromJson(json, Holder.class);
        assertEquals(holder.first, read.first);
        assertEquals(holder.second, read.second);
    }

    @Test
    void aNullPaintStaysNull() {
        Holder read = PeakUiSerializer.GSON.fromJson("{\"first\":null}", Holder.class);
        assertNull(read.first);
    }
}
