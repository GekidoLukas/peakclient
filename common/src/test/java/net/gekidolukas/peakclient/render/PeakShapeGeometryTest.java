package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakShapeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakShapeGeometryTest {
    private static float min(float[] points, int offset) {
        float value = Float.MAX_VALUE;
        for (int i = offset; i < points.length; i += 2) value = Math.min(value, points[i]);
        return value;
    }

    private static float max(float[] points, int offset) {
        float value = -Float.MAX_VALUE;
        for (int i = offset; i < points.length; i += 2) value = Math.max(value, points[i]);
        return value;
    }

    @Test
    void sharpPolygonsHaveTheExpectedVertexCountAndFillTheBox() {
        Object[][] cases = {
                {PeakShapeKind.TRIANGLE, 3}, {PeakShapeKind.DIAMOND, 4}, {PeakShapeKind.PENTAGON, 5},
                {PeakShapeKind.HEXAGON, 6}, {PeakShapeKind.HEXAGON_FLAT, 6}, {PeakShapeKind.OCTAGON, 8},
                {PeakShapeKind.STAR, 10}
        };
        for (Object[] testCase : cases) {
            float[] points = PeakShapeGeometry.outline((PeakShapeKind) testCase[0], 10f, 20f, 70f, 60f, 0f);
            assertEquals((Integer) testCase[1] * 2, points.length, testCase[0].toString());
            assertEquals(10f, min(points, 0), 1e-3, testCase[0].toString());
            assertEquals(70f, max(points, 0), 1e-3, testCase[0].toString());
            assertEquals(20f, min(points, 1), 1e-3, testCase[0].toString());
            assertEquals(60f, max(points, 1), 1e-3, testCase[0].toString());
        }
    }

    @Test
    void roundingAddsPointsAndStaysInsideTheBox() {
        for (PeakShapeKind kind : PeakShapeKind.values()) {
            if (!kind.isPolygon()) continue;
            float[] sharp = PeakShapeGeometry.outline(kind, 0f, 0f, 50f, 50f, 0f);
            float[] rounded = PeakShapeGeometry.outline(kind, 0f, 0f, 50f, 50f, 6f);
            assertTrue(rounded.length > sharp.length, kind.toString());
            assertTrue(min(rounded, 0) >= -1e-3f && max(rounded, 0) <= 50f + 1e-3f, kind.toString());
            assertTrue(min(rounded, 1) >= -1e-3f && max(rounded, 1) <= 50f + 1e-3f, kind.toString());
        }
    }

    @Test
    void aTriangleIsPointingUp() {
        float[] points = PeakShapeGeometry.outline(PeakShapeKind.TRIANGLE, 0f, 0f, 40f, 40f, 0f);
        assertEquals(20f, points[0], 1e-3);
        assertEquals(0f, points[1], 1e-3);
        assertEquals(40f, points[3], 1e-3);
        assertEquals(40f, points[5], 1e-3);
    }

    @Test
    void offsetMovesTheVerticesOutwardAlongTheBisector() {
        float[] diamond = PeakShapeGeometry.outline(PeakShapeKind.DIAMOND, 0f, 0f, 10f, 10f, 0f);
        float[] grown = PeakShapeGeometry.offset(diamond, 1f);
        assertEquals(5f, grown[0], 1e-3);
        assertEquals(-Math.sqrt(2.0), grown[1], 1e-3);
        float[] shrunk = PeakShapeGeometry.offset(diamond, -1f);
        assertEquals(Math.sqrt(2.0), shrunk[1], 1e-3);
    }

    @Test
    void roundedCornersOfADegenerateRadiusAreLimitedByTheEdges() {
        float[] points = PeakShapeGeometry.outline(PeakShapeKind.DIAMOND, 0f, 0f, 10f, 10f, 100f);
        assertTrue(min(points, 0) >= -1e-3f && max(points, 0) <= 10f + 1e-3f);
        assertTrue(min(points, 1) >= -1e-3f && max(points, 1) <= 10f + 1e-3f);
    }
}
