package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPlayerIconView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class PeakPlayerSkinRenderer {
    private static final int TEX = 64;
    private static final int CANVAS_WIDTH = 16;

    private PeakPlayerSkinRenderer() {}

    public static int viewHeight(PeakPlayerIconView view) {
        return switch (view) {
            case FACE -> 8;
            case BUST -> 12;
            case UPPER_BODY -> 20;
            case FULL_BODY -> 32;
        };
    }

    public static int viewWidth(PeakPlayerIconView view) {
        return view == PeakPlayerIconView.FACE ? 8 : CANVAS_WIDTH;
    }

    public static void drawFace(GuiGraphics graphics, PlayerSkin skin, int x, int y, int size) {
        draw(graphics, skin, PeakPlayerIconView.FACE, x, y, size);
    }

    public static void draw(GuiGraphics graphics, PlayerSkin skin, PeakPlayerIconView view, int x, int y, int height) {
        if (skin == null || height <= 0) return;
        float scale = height / (float) viewHeight(view);
        drawScaled(graphics, skin, view, x, y, scale);
    }

    public static void drawFitted(GuiGraphics graphics, PlayerSkin skin, PeakPlayerIconView view,
                                  int x, int y, int boxWidth, int boxHeight) {
        if (skin == null || boxWidth <= 0 || boxHeight <= 0) return;
        float scale = Math.min(boxWidth / (float) viewWidth(view), boxHeight / (float) viewHeight(view));
        int drawWidth = Math.round(viewWidth(view) * scale);
        int drawHeight = Math.round(viewHeight(view) * scale);
        drawScaled(graphics, skin, view, x + (boxWidth - drawWidth) / 2, y + (boxHeight - drawHeight) / 2, scale);
    }

    private static void drawScaled(GuiGraphics graphics, PlayerSkin skin, PeakPlayerIconView view,
                                   int x, int y, float scale) {
        ResourceLocation composite = PeakPlayerSkinComposite.getOrCreate(skin.texture());
        boolean layered = composite == null;
        ResourceLocation texture = layered ? skin.texture() : composite;
        int canvasHeight = viewHeight(view);

        if (view == PeakPlayerIconView.FACE) {
            part(graphics, texture, x, y, scale, canvasHeight, 0, 0, 8, 8, 8, 8);
            if (layered) part(graphics, texture, x, y, scale, canvasHeight, 0, 0, 8, 8, 40, 8);
            return;
        }

        boolean slim = skin.model() == PlayerSkin.Model.SLIM;
        int armWidth = slim ? 3 : 4;
        int rightArmX = 4 - armWidth;
        int leftArmX = 12;
        int headX = 4;

        part(graphics, texture, x, y, scale, canvasHeight, headX, 0, 8, 8, 8, 8);
        part(graphics, texture, x, y, scale, canvasHeight, 4, 8, 8, 12, 20, 20);
        part(graphics, texture, x, y, scale, canvasHeight, rightArmX, 8, armWidth, 12, 44, 20);
        part(graphics, texture, x, y, scale, canvasHeight, leftArmX, 8, armWidth, 12, 36, 52);
        part(graphics, texture, x, y, scale, canvasHeight, 4, 20, 4, 12, 4, 20);
        part(graphics, texture, x, y, scale, canvasHeight, 8, 20, 4, 12, 20, 52);

        if (!layered) return;

        part(graphics, texture, x, y, scale, canvasHeight, headX, 0, 8, 8, 40, 8);
        part(graphics, texture, x, y, scale, canvasHeight, 4, 8, 8, 12, 20, 36);
        part(graphics, texture, x, y, scale, canvasHeight, rightArmX, 8, armWidth, 12, 44, 36);
        part(graphics, texture, x, y, scale, canvasHeight, leftArmX, 8, armWidth, 12, 52, 52);
        part(graphics, texture, x, y, scale, canvasHeight, 4, 20, 4, 12, 4, 36);
        part(graphics, texture, x, y, scale, canvasHeight, 8, 20, 4, 12, 4, 52);
    }

    private static void part(GuiGraphics graphics, ResourceLocation texture, int originX, int originY, float scale,
                             int canvasHeight, int cx, int cy, int w, int h, int srcX, int srcY) {
        int visibleHeight = Math.min(h, canvasHeight - cy);
        if (visibleHeight <= 0) return;

        int destLeft = originX + Math.round(cx * scale);
        int destRight = originX + Math.round((cx + w) * scale);
        int destTop = originY + Math.round(cy * scale);
        int destBottom = originY + Math.round((cy + visibleHeight) * scale);
        if (destRight <= destLeft || destBottom <= destTop) return;

        PeakImmediateRender.blit(graphics, texture, destLeft, destTop, destRight - destLeft, destBottom - destTop, srcX, srcY, w, visibleHeight, TEX, TEX);
    }
}
