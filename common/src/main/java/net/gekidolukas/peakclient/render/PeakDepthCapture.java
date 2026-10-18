package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public final class PeakDepthCapture {
    private static final int KEEP_FRAMES = 3;

    private static TextureTarget target;
    private static long frame;
    private static long wantedFrame = Long.MIN_VALUE;
    private static long capturedFrame = Long.MIN_VALUE;
    private static boolean failed;

    private PeakDepthCapture() {}

    public static void want() {
        wantedFrame = frame;
    }

    public static boolean isValid() {
        return !failed && capturedFrame == frame;
    }

    public static boolean hasFailed() {
        return failed;
    }

    static TextureTarget target() {
        return target;
    }

    public static void capture(Minecraft minecraft) {
        frame++;
        if (failed || frame - wantedFrame > KEEP_FRAMES) return;
        try {
            RenderTarget main = minecraft.getMainRenderTarget();
            if (target == null) {
                target = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            } else if (target.width != main.width || target.height != main.height) {
                target.resize(main.width, main.height, Minecraft.ON_OSX);
            }
            while (GL11.glGetError() != GL11.GL_NO_ERROR) {
                continue;
            }
            target.copyDepthFrom(main);
            main.bindWrite(false);
            if (GL11.glGetError() != GL11.GL_NO_ERROR) {
                failed = true;
                PeakLog.warn("World depth could not be captured, world anchors fall back to line of sight occlusion");
                return;
            }
            capturedFrame = frame;
        } catch (RuntimeException exception) {
            failed = true;
            PeakLog.error("World depth capture failed, world anchors fall back to line of sight occlusion", exception);
        }
    }

    public static void reset() {
        capturedFrame = Long.MIN_VALUE;
        wantedFrame = Long.MIN_VALUE;
        failed = false;
    }
}
