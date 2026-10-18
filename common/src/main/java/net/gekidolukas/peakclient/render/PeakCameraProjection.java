package net.gekidolukas.peakclient.render;

import org.joml.Matrix4fc;
import org.joml.Vector4f;

public final class PeakCameraProjection {
    public record Projection(float x, float y, double distance, boolean inFront, float viewX, float viewY, float viewZ, float ndcZ) {
        public boolean isInside(float width, float height) {
            return inFront && x >= 0f && x <= width && y >= 0f && y <= height;
        }
    }

    private PeakCameraProjection() {}

    public static Projection project(Matrix4fc view, Matrix4fc projection, double cameraX, double cameraY, double cameraZ,
                                     double worldX, double worldY, double worldZ, float guiWidth, float guiHeight) {
        double dx = worldX - cameraX, dy = worldY - cameraY, dz = worldZ - cameraZ;
        Vector4f point = new Vector4f((float) dx, (float) dy, (float) dz, 1f);
        view.transform(point);
        float viewX = point.x, viewY = point.y, viewZ = point.z;
        projection.transform(point);
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        boolean inFront = point.w > 1e-5f;
        float screenX;
        float screenY;
        float ndcZ = 1f;
        if (inFront) {
            ndcZ = point.z / point.w;
            float ndcX = point.x / point.w;
            float ndcY = point.y / point.w;
            screenX = (ndcX * 0.5f + 0.5f) * guiWidth;
            screenY = (0.5f - ndcY * 0.5f) * guiHeight;
        } else {
            screenX = Float.NaN;
            screenY = Float.NaN;
        }
        return new Projection(screenX, screenY, distance, inFront, viewX, viewY, viewZ, ndcZ);
    }
}
