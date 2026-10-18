package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakChibiPlayerAssets;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakChibiFitTest {
    private static final float EPSILON = 1e-3f;

    @Test
    void theModelExtentsAreDerivedFromTheBoxes() {
        assertEquals(-17f, PeakChibiPlayerAssets.MODEL_TOP, EPSILON);
        assertEquals(16.25f, PeakChibiPlayerAssets.MODEL_BOTTOM, EPSILON);
        assertEquals(33.25f, PeakChibiPlayerAssets.MODEL_HEIGHT, EPSILON);
        assertEquals(-0.375f, PeakChibiPlayerAssets.MODEL_CENTER_Y, EPSILON);
    }

    @Test
    void theSilhouetteWidthFollowsTheRotation() {
        assertEquals(9f, PeakChibiPlayerAssets.halfWidth(0f), EPSILON);
        assertEquals(9f, PeakChibiPlayerAssets.halfWidth((float) (Math.PI / 2)), EPSILON);
        assertEquals(9f * (float) Math.sqrt(2), PeakChibiPlayerAssets.halfWidth((float) (Math.PI / 4)), EPSILON);
    }

    @Test
    void theLimitingSideDecidesTheScale() {
        float tall = PeakChibiPlayerAssets.pixelsPerModelUnit(20, 200, 0f);
        float wide = PeakChibiPlayerAssets.pixelsPerModelUnit(200, 20, 0f);

        assertEquals(20f / 18f, tall, EPSILON);
        assertEquals(20f / 33.25f, wide, EPSILON);
    }

    @Test
    void theModelNeverOverflowsTheBox() {
        for (int degrees = -180; degrees <= 180; degrees += 15) {
            float radians = (float) Math.toRadians(degrees);
            for (int[] box : new int[][]{ {48, 64}, {10, 80}, {120, 30}, {64, 64}, {200, 400} }) {
                float unit = PeakChibiPlayerAssets.pixelsPerModelUnit(box[0], box[1], radians);

                assertTrue(2f * PeakChibiPlayerAssets.halfWidth(radians) * unit <= box[0] + EPSILON);
                assertTrue(PeakChibiPlayerAssets.MODEL_HEIGHT * unit <= box[1] + EPSILON);
            }
        }
    }

    @Test
    void anEmptyBoxDrawsNothing() {
        assertEquals(0f, PeakChibiPlayerAssets.pixelsPerModelUnit(0, 64, 0f));
        assertEquals(0f, PeakChibiPlayerAssets.pixelsPerModelUnit(48, 0, 0f));
    }

    @Test
    void theDefaultSizeIsAboutTheOldFixedScale() {
        float radians = (float) Math.toRadians(-157.5);

        float unit = PeakChibiPlayerAssets.pixelsPerModelUnit(48, 64, radians);

        assertEquals(30f, unit * 16f, 2f);
    }

    @Test
    void theNaturalSizeMatchesTheReferenceScale() {
        PeakPixelSize natural = PeakChibiPlayerAssets.naturalSize((float) Math.toRadians(-157.5));

        assertEquals(45, natural.width());
        assertEquals(63, natural.height());
    }
}
