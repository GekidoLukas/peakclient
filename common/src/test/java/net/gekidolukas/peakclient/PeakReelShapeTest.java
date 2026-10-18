package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakCarouselDirection;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakReelEasing;
import net.gekidolukas.peakclient.ui.PeakReelMotion;
import net.gekidolukas.peakclient.ui.PeakShape;
import net.gekidolukas.peakclient.ui.PeakShapeKind;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakReelShapeTest {
    private static PeakDocument roundTrip(PeakDocument document) {
        return PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
    }

    @Test
    void aSpinLandsExactlyOnTheTargetAfterTheMinimumLoops() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(1);
        motion.spin(5, 3, 0, 100, 2, PeakReelEasing.DECELERATE);

        assertTrue(motion.isSpinning());
        assertEquals(1.0, motion.position(0), 1e-9);
        double middle = motion.position(50);
        assertTrue(middle > 1.0 && middle < 13.0);
        assertEquals(13.0, motion.position(100), 1e-9);
        assertFalse(motion.isSpinning());

        motion.normalize(5);
        assertEquals(3.0, motion.position(200), 1e-9);
        assertEquals(3, motion.settledIndex(5));
    }

    @Test
    void aSpinFromTheSameIndexWithoutLoopsDoesNotMove() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(2);
        motion.spin(4, 2, 0, 60, 0, PeakReelEasing.DECELERATE);

        assertEquals(2.0, motion.position(30), 1e-9);
        assertEquals(2.0, motion.position(60), 1e-9);
    }

    @Test
    void aTargetBehindTheCurrentIndexWrapsForwards() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(3);
        motion.spin(5, 1, 0, 50, 0, PeakReelEasing.DECELERATE);

        assertEquals(6.0, motion.position(50), 1e-9);
        motion.normalize(5);
        assertEquals(1.0, motion.position(60), 1e-9);
    }

    @Test
    void aNewSpinStartsWhereTheOldOneIs() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(0);
        motion.spin(6, 4, 0, 100, 1, PeakReelEasing.DECELERATE);
        double current = motion.position(30);

        motion.spin(6, 2, 30, 100, 0, PeakReelEasing.DECELERATE);

        assertEquals(current, motion.position(30), 1e-9);
        assertEquals(2.0, PeakReelMotion.mod(motion.position(130), 6), 1e-9);
    }

    @Test
    void jumpAndInitializationAreInstant() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(4);
        assertEquals(4.0, motion.position(0), 1e-9);
        motion.ensureInitialized(1);
        assertEquals(4.0, motion.position(0), 1e-9);
        motion.jump(2);
        assertEquals(2.0, motion.position(10), 1e-9);
        assertFalse(motion.isSpinning());
    }

    @Test
    void easingsEndExactlyAndTheSettleOvershoots() {
        for (PeakReelEasing easing : PeakReelEasing.values()) {
            assertEquals(0.0, easing.apply(0.0), 1e-9);
            assertEquals(1.0, easing.apply(1.0), 1e-9);
        }
        assertTrue(PeakReelEasing.DECELERATE.apply(0.5) > 0.5);
        double peak = 0;
        for (double p = 0; p <= 1.0; p += 0.01) peak = Math.max(peak, PeakReelEasing.SETTLE.apply(p));
        assertTrue(peak > 1.0 && peak < 1.2);
    }

    @Test
    void moduloIsAlwaysPositive() {
        assertEquals(2.0, PeakReelMotion.mod(-3.0, 5), 1e-9);
        assertEquals(0.5, PeakReelMotion.mod(5.5, 5), 1e-9);
    }

    @Test
    void theReelSurvivesTheWireAndTakesPatches() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakReel.create("test:reel").movement(PeakCarouselDirection.LEFT).spinSeconds(3).minLoops(4)
                .easing(PeakReelEasing.SETTLE).startIndex(2).fadeStart(10).fadeEnd(20).gap(6)
                .add(PeakLabel.create("test:a", "A")));

        PeakReel back = assertInstanceOf(PeakReel.class, roundTrip(document).findById("test:reel"));

        assertEquals(PeakCarouselDirection.LEFT, back.getMovement());
        assertEquals(60, back.getSpinTicks());
        assertEquals(4, back.getMinLoops());
        assertEquals(PeakReelEasing.SETTLE, back.getEasing());
        assertEquals(2, back.getStartIndex());
        assertEquals(10, back.getFadeStart());
        assertEquals(20, back.getFadeEnd());

        back.applyPatch(PeakPatch.reel().spinTo(3, 80, 1).build());
        PeakReel.SpinRequest request = back.takeSpinRequest();
        assertEquals(3, request.target());
        assertEquals(80, request.ticks());
        assertEquals(1, request.loops());
        assertEquals(null, back.takeSpinRequest());

        back.applyPatch(PeakPatch.reel().spinTo(1).build());
        PeakReel.SpinRequest plain = back.takeSpinRequest();
        assertEquals(-1, plain.ticks());
        assertEquals(-1, plain.loops());

        back.applyPatch(PeakPatch.reel().jumpTo(4).build());
        assertEquals(4, back.takeJumpRequest());
        assertEquals(-1, back.takeJumpRequest());

        back.applyPatch(PeakPatch.reel().stop().build());
        assertTrue(back.takeStopRequest());
        assertFalse(back.takeStopRequest());
    }

    @Test
    void theShapeSurvivesTheWireAndClampsItsRadius() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakShape.create("test:shape").cornerRadius(40).size(60, 30).add(PeakLabel.create("test:a", "A")));
        document.add(PeakShape.circle().size(48, 48));

        PeakShape back = assertInstanceOf(PeakShape.class, roundTrip(document).findById("test:shape"));
        assertEquals(PeakShapeKind.ROUNDED, back.getShape());
        assertEquals(40, back.getCornerRadius());
        assertEquals(15, back.effectiveRadius(60, 30));
        assertEquals(12, back.effectiveRadius(200, 24));

        PeakShape circle = PeakShape.circle();
        assertEquals(24, circle.effectiveRadius(48, 48));
        assertEquals(10, circle.effectiveRadius(80, 20));

        back.applyPatch(PeakPatch.shape().shape(PeakShapeKind.CIRCLE).cornerRadius(3).build());
        assertEquals(PeakShapeKind.CIRCLE, back.getShape());
        assertEquals(3, back.getCornerRadius());
    }

    @Test
    void aStyleCopyKeepsEverythingButTheRadius() {
        PeakStyle style = PeakStyle.create().background(0xFF112233).borderWidth(2).borderColor(0xFFFFFFFF).padding(1, 2, 3, 4)
                .margin(5).borderRadius(3).boxShadow(0x80000000, 1, 2, 3);

        PeakStyle copy = style.withBorderRadius(9);

        assertEquals(9, copy.getBorderRadius());
        assertEquals(3, style.getBorderRadius());
        assertEquals(style.getBackground(), copy.getBackground());
        assertEquals(style.getBorderColor(), copy.getBorderColor());
        assertEquals(2, copy.getBorderWidth());
        assertEquals(4, copy.getPadding().getLeft());
        assertEquals(5, copy.getMargin().getTop());
        assertEquals(style.getShadowColor(), copy.getShadowColor());
        assertEquals(2, copy.getShadowOffsetY());
        assertEquals(3, copy.getShadowBlur());
    }

    @Test
    void popOutFractionsAreClampedAndOffByDefault() {
        PeakShape plain = PeakShape.circle();
        assertFalse(plain.hasPopOut());

        PeakShape shape = PeakShape.circle().popOutTop(0.5f).popOutBottom(2f).popOutLeft(-1f);
        assertTrue(shape.hasPopOut());
        assertEquals(0.5f, shape.getPopOutTop(), 1e-6);
        assertEquals(1f, shape.getPopOutBottom(), 1e-6);
        assertEquals(0f, shape.getPopOutLeft(), 1e-6);
        assertEquals(0f, shape.getPopOutRight(), 1e-6);
    }

    @Test
    void popOutAndPolygonKindsSurviveTheWireAndPatches() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakShape.create("test:hex").shape(PeakShapeKind.HEXAGON).cornerRadius(3).popOutTop(0.4f)
                .size(40, 46).add(PeakLabel.create("test:a", "A")));

        PeakShape back = assertInstanceOf(PeakShape.class, roundTrip(document).findById("test:hex"));
        assertEquals(PeakShapeKind.HEXAGON, back.getShape());
        assertTrue(back.getShape().isPolygon());
        assertEquals(0.4f, back.getPopOutTop(), 1e-6);
        assertEquals(0f, back.getPopOutBottom(), 1e-6);

        back.applyPatch(PeakPatch.shape().popOutBottom(0.25f).popOutTop(0f).build());
        assertEquals(0.25f, back.getPopOutBottom(), 1e-6);
        assertEquals(0f, back.getPopOutTop(), 1e-6);
        back.applyPatch(PeakPatch.shape().noPopOut().build());
        assertFalse(back.hasPopOut());
    }

    @Test
    void polygonFactoriesStartSharp() {
        assertEquals(PeakShapeKind.TRIANGLE, PeakShape.triangle().getShape());
        assertEquals(0, PeakShape.triangle().getCornerRadius());
        assertEquals(PeakShapeKind.STAR, PeakShape.star().getShape());
        assertEquals(0, PeakShape.hexagon().effectiveRadius(40, 40));
        assertFalse(PeakShapeKind.CIRCLE.isPolygon());
        assertFalse(PeakShapeKind.ROUNDED.isPolygon());
        assertTrue(PeakShapeKind.OCTAGON.isPolygon());
    }

    @Test
    void aReelCanSpinOnStartAfterADelay() {
        PeakReel reel = PeakReel.create("test:reel").spinOnStart(3, 20);
        assertEquals(3, reel.getAutoSpinTarget());
        assertEquals(20, reel.getAutoSpinDelayTicks());

        assertFalse(reel.isAutoSpinPending(100.0));
        assertFalse(reel.isAutoSpinPending(119.5));
        assertTrue(reel.isAutoSpinPending(120.0));
        reel.finishAutoSpin();
        assertFalse(reel.isAutoSpinPending(200.0));

        reel.resetAnchor();
        assertFalse(reel.isAutoSpinPending(300.0));
        assertTrue(reel.isAutoSpinPending(320.0));

        assertEquals(-1, PeakReel.create("test:other").getAutoSpinTarget());
        assertFalse(PeakReel.create("test:none").isAutoSpinPending(10.0));
    }

    @Test
    void spinOnStartSurvivesTheWire() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakReel.create("test:reel").spinOnStart(2, 10).add(PeakLabel.create("test:a", "A")));

        PeakReel back = assertInstanceOf(PeakReel.class, roundTrip(document).findById("test:reel"));

        assertEquals(2, back.getAutoSpinTarget());
        assertEquals(10, back.getAutoSpinDelayTicks());
    }
}
