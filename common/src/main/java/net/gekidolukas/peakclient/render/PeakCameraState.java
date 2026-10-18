package net.gekidolukas.peakclient.render;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class PeakCameraState {
    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static Vec3 position = Vec3.ZERO;
    private static float partialTick;
    private static boolean captured;

    private PeakCameraState() {}

    public static void capture(Camera camera, Matrix4f view, Matrix4f projection, float tickFraction) {
        VIEW.set(view);
        PROJECTION.set(projection);
        position = camera.getPosition();
        partialTick = tickFraction;
        captured = true;
    }

    public static boolean isAvailable() {
        return captured;
    }

    public static Matrix4f view() {
        return VIEW;
    }

    public static Matrix4f projection() {
        return PROJECTION;
    }

    public static Vec3 position() {
        return position;
    }

    public static float partialTick() {
        return partialTick;
    }

    public static void clear() {
        captured = false;
        position = Vec3.ZERO;
    }
}
