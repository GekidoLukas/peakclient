package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public final class PeakDepthCapture {
    private static final int KEEP_FRAMES = 3;

    private static TextureTarget target;
    private static TextureTarget opaqueTarget;
    private static long frame;
    private static long wantedFrame = Long.MIN_VALUE;
    private static long capturedFrame = Long.MIN_VALUE;
    private static long opaqueFrame = Long.MIN_VALUE;
    private static boolean failed;
    private static boolean opaqueFailed;

    private PeakDepthCapture() {}

    public static void want() {
        wantedFrame = frame;
    }

    public static boolean isValid() {
        return !failed && capturedFrame == frame;
    }

    public static boolean hasOpaque() {
        return !failed && !opaqueFailed && opaqueFrame == frame && capturedFrame == frame;
    }

    public static boolean hasFailed() {
        return failed;
    }

    static TextureTarget target() {
        return target;
    }

    static TextureTarget opaqueTarget() {
        return opaqueTarget;
    }

    public static void captureOpaque(Minecraft minecraft) {
        long upcoming = frame + 1;
        if (failed || opaqueFailed || upcoming - wantedFrame > KEEP_FRAMES || opaqueFrame == upcoming) return;
        try {
            RenderTarget main = minecraft.getMainRenderTarget();
            opaqueTarget = ensure(opaqueTarget, main);
            clearErrors();
            opaqueTarget.copyDepthFrom(main);
            main.bindWrite(false);
            if (GL11.glGetError() != GL11.GL_NO_ERROR) {
                opaqueFailed = true;
                PeakLog.warn("Opaque world depth could not be captured, world anchors treat translucent blocks as solid");
                return;
            }
            opaqueFrame = upcoming;
        } catch (RuntimeException exception) {
            opaqueFailed = true;
            PeakLog.error("Opaque world depth capture failed, world anchors treat translucent blocks as solid", exception);
        }
    }

    public static void capture(Minecraft minecraft) {
        frame++;
        if (failed || frame - wantedFrame > KEEP_FRAMES) return;
        try {
            RenderTarget main = minecraft.getMainRenderTarget();
            target = ensure(target, main);
            clearErrors();
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
        opaqueFrame = Long.MIN_VALUE;
        wantedFrame = Long.MIN_VALUE;
        failed = false;
        opaqueFailed = false;
    }

    private static TextureTarget ensure(TextureTarget existing, RenderTarget main) {
        if (existing == null) return new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
        if (existing.width != main.width || existing.height != main.height) existing.resize(main.width, main.height, Minecraft.ON_OSX);
        return existing;
    }

    private static void clearErrors() {
        while (GL11.glGetError() != GL11.GL_NO_ERROR) {
            continue;
        }
    }
}
