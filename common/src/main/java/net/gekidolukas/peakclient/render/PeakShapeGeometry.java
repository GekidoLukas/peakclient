package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakShapeKind;

final class PeakShapeGeometry {
    private static final int CORNER_STEPS = 6;
    private static final double STAR_INNER_RATIO = 0.45;

    private PeakShapeGeometry() {}

    static float[] outline(PeakShapeKind kind, float x1, float y1, float x2, float y2, float cornerRadius) {
        float[] unit = switch (kind) {
            case TRIANGLE -> regular(3, -90.0);
            case DIAMOND -> regular(4, -90.0);
            case PENTAGON -> regular(5, -90.0);
            case HEXAGON -> regular(6, -90.0);
            case HEXAGON_FLAT -> regular(6, 0.0);
            case OCTAGON -> regular(8, -90.0 + 22.5);
            case STAR -> star(5);
            case ROUNDED, CIRCLE -> regular(4, -90.0);
        };
        float[] points = fitToBox(unit, x1, y1, x2, y2);
        return cornerRadius > 0f ? rounded(points, cornerRadius) : points;
    }

    private static float[] regular(int count, double startDegrees) {
        float[] points = new float[count * 2];
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(startDegrees + 360.0 * i / count);
            points[i * 2] = (float) Math.cos(angle);
            points[i * 2 + 1] = (float) Math.sin(angle);
        }
        return points;
    }

    private static float[] star(int tips) {
        float[] points = new float[tips * 4];
        for (int i = 0; i < tips * 2; i++) {
            double radius = i % 2 == 0 ? 1.0 : STAR_INNER_RATIO;
            double angle = Math.toRadians(-90.0 + 180.0 * i / tips);
            points[i * 2] = (float) (Math.cos(angle) * radius);
            points[i * 2 + 1] = (float) (Math.sin(angle) * radius);
        }
        return points;
    }

    private static float[] fitToBox(float[] points, float x1, float y1, float x2, float y2) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < points.length; i += 2) {
            minX = Math.min(minX, points[i]);
            maxX = Math.max(maxX, points[i]);
            minY = Math.min(minY, points[i + 1]);
            maxY = Math.max(maxY, points[i + 1]);
        }
        float scaleX = (x2 - x1) / Math.max(1e-6f, maxX - minX);
        float scaleY = (y2 - y1) / Math.max(1e-6f, maxY - minY);
        float[] fitted = new float[points.length];
        for (int i = 0; i < points.length; i += 2) {
            fitted[i] = x1 + (points[i] - minX) * scaleX;
            fitted[i + 1] = y1 + (points[i + 1] - minY) * scaleY;
        }
        return fitted;
    }

    static float[] rounded(float[] points, float radius) {
        int count = points.length / 2;
        float[] result = new float[count * (CORNER_STEPS + 1) * 2];
        int written = 0;
        for (int i = 0; i < count; i++) {
            float px = points[i * 2], py = points[i * 2 + 1];
            int previous = (i + count - 1) % count, next = (i + 1) % count;
            float ux = points[previous * 2] - px, uy = points[previous * 2 + 1] - py;
            float vx = points[next * 2] - px, vy = points[next * 2 + 1] - py;
            float lengthU = (float) Math.hypot(ux, uy), lengthV = (float) Math.hypot(vx, vy);
            if (lengthU < 1e-4f || lengthV < 1e-4f) {
                result[written++] = px;
                result[written++] = py;
                continue;
            }
            ux /= lengthU; uy /= lengthU;
            vx /= lengthV; vy /= lengthV;
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, ux * vx + uy * vy)));
            if (angle < 0.01 || angle > Math.PI - 0.01) {
                result[written++] = px;
                result[written++] = py;
                continue;
            }
            double tanHalf = Math.tan(angle / 2.0);
            float distance = (float) Math.min(radius / tanHalf, Math.min(lengthU, lengthV) / 2.0);
            float arcRadius = (float) (distance * tanHalf);
            float bisectorX = ux + vx, bisectorY = uy + vy;
            float bisectorLength = (float) Math.hypot(bisectorX, bisectorY);
            bisectorX /= bisectorLength;
            bisectorY /= bisectorLength;
            float centerDistance = (float) (arcRadius / Math.sin(angle / 2.0));
            float centerX = px + bisectorX * centerDistance, centerY = py + bisectorY * centerDistance;
            double fromAngle = Math.atan2(py + uy * distance - centerY, px + ux * distance - centerX);
            double toAngle = Math.atan2(py + vy * distance - centerY, px + vx * distance - centerX);
            double sweep = toAngle - fromAngle;
            while (sweep > Math.PI) sweep -= 2.0 * Math.PI;
            while (sweep < -Math.PI) sweep += 2.0 * Math.PI;
            for (int step = 0; step <= CORNER_STEPS; step++) {
                double current = fromAngle + sweep * step / CORNER_STEPS;
                result[written++] = centerX + arcRadius * (float) Math.cos(current);
                result[written++] = centerY + arcRadius * (float) Math.sin(current);
            }
        }
        float[] trimmed = new float[written];
        System.arraycopy(result, 0, trimmed, 0, written);
        return trimmed;
    }

    static float[] offset(float[] points, float amount) {
        int count = points.length / 2;
        float[] result = new float[points.length];
        for (int i = 0; i < count; i++) {
            int previous = (i + count - 1) % count, next = (i + 1) % count;
            float[] before = normal(points[previous * 2], points[previous * 2 + 1], points[i * 2], points[i * 2 + 1]);
            float[] after = normal(points[i * 2], points[i * 2 + 1], points[next * 2], points[next * 2 + 1]);
            float sumX = before[0] + after[0], sumY = before[1] + after[1];
            float divisor = Math.max(0.5f, 1f + before[0] * after[0] + before[1] * after[1]);
            float offsetX = amount * sumX / divisor, offsetY = amount * sumY / divisor;
            float length = (float) Math.hypot(offsetX, offsetY);
            float limit = Math.abs(amount) * 2f;
            if (length > limit && length > 0f) {
                offsetX *= limit / length;
                offsetY *= limit / length;
            }
            result[i * 2] = points[i * 2] + offsetX;
            result[i * 2 + 1] = points[i * 2 + 1] + offsetY;
        }
        return result;
    }

    private static float[] normal(float fromX, float fromY, float toX, float toY) {
        float dx = toX - fromX, dy = toY - fromY;
        float length = (float) Math.hypot(dx, dy);
        if (length < 1e-6f) return new float[]{0f, 0f};
        return new float[]{dy / length, -dx / length};
    }

    static float[] translate(float[] points, float dx, float dy) {
        float[] result = new float[points.length];
        for (int i = 0; i < points.length; i += 2) {
            result[i] = points[i] + dx;
            result[i + 1] = points[i + 1] + dy;
        }
        return result;
    }

    static float[] scaled(float[] points, float scale) {
        float[] result = new float[points.length];
        for (int i = 0; i < points.length; i++) result[i] = points[i] * scale;
        return result;
    }

    static float centerX(float[] points) {
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (int i = 0; i < points.length; i += 2) {
            min = Math.min(min, points[i]);
            max = Math.max(max, points[i]);
        }
        return (min + max) / 2f;
    }

    static float centerY(float[] points) {
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (int i = 1; i < points.length; i += 2) {
            min = Math.min(min, points[i]);
            max = Math.max(max, points[i]);
        }
        return (min + max) / 2f;
    }
}
