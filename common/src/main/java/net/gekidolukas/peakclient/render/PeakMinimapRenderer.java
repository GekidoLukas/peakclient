package net.gekidolukas.peakclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.gekidolukas.peakclient.ui.PeakBorderShape;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakMinimap;
import net.gekidolukas.peakclient.ui.PeakMinimapPlayerIconMode;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPoint;
import net.gekidolukas.peakclient.ui.PeakWaypoint;
import net.gekidolukas.peakclient.ui.PeakWorldBorderSource;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomWorldBorderState;
import net.gekidolukas.peakclient.ui.client.PeakClientWaypointRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.border.WorldBorder;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class PeakMinimapRenderer implements PeakElementRenderer<PeakMinimap> {
    private record Camera(double x, double z) {}


    @Override
    public void renderContent(PeakRenderContext ctx, PeakMinimap minimap, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        GuiGraphics graphics = ctx.graphics();
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        double blocksPerPixelX = (minimap.getViewRadiusBlocks() * 2) / width;
        double blocksPerPixelZ = (minimap.getViewRadiusBlocks() * 2) / height;

        int scissorLeft = Math.round(ctx.absX() * ctx.scissorUnitFactor());
        int scissorTop = Math.round(ctx.absY() * ctx.scissorUnitFactor());
        int scissorRight = Math.round((ctx.absX() + width * ctx.absScale()) * ctx.scissorUnitFactor());
        int scissorBottom = Math.round((ctx.absY() + height * ctx.absScale()) * ctx.scissorUnitFactor());
        graphics.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);

        Camera camera = resolveCamera(minimap, playerX, playerZ);
        double camX = camera.x(), camZ = camera.z();

        drawMapImage(graphics, minimap, camX, camZ, width, height, blocksPerPixelX, blocksPerPixelZ);
        drawWorldBorder(graphics, minimap, camX, camZ, width, height, blocksPerPixelX, blocksPerPixelZ);
        drawWaypoints(graphics, ctx.font(), minimap, playerX, playerZ, camX, camZ, width, height, blocksPerPixelX, blocksPerPixelZ);

        drawPlayerIcon(graphics, mc, minimap, width, height);
        graphics.disableScissor();

        drawBorderOverlay(graphics, minimap, width, height);
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakMinimap minimap) {
        return PeakPixelSize.ZERO;
    }

    private Camera resolveCamera(PeakMinimap minimap, double playerX, double playerZ) {
        if (minimap.getImageWidth() <= 0 || minimap.getImageHeight() <= 0) return new Camera(playerX, playerZ);
        double blocksPerImgPixelX = (minimap.getWorldMaxX() - minimap.getWorldMinX()) / minimap.getImageWidth();
        double blocksPerImgPixelZ = (minimap.getWorldMaxZ() - minimap.getWorldMinZ()) / minimap.getImageHeight();
        if (blocksPerImgPixelX <= 0 || blocksPerImgPixelZ <= 0) return new Camera(playerX, playerZ);
        double radius = minimap.getViewRadiusBlocks();
        return new Camera(
                snapCamera(playerX, minimap.getWorldMinX(), blocksPerImgPixelX, radius),
                snapCamera(playerZ, minimap.getWorldMinZ(), blocksPerImgPixelZ, radius)
        );
    }

    private static double snapCamera(double player, double worldMin, double blocksPerImgPixel, double viewRadius) {
        double radiusInPixels = viewRadius / blocksPerImgPixel;
        double playerInPixels = (player - worldMin) / blocksPerImgPixel;
        return worldMin + (Math.round(playerInPixels - radiusInPixels) + radiusInPixels) * blocksPerImgPixel;
    }

    private int worldXToScreen(double worldX, double playerX, int width, double blocksPerPixelX) {
        return (int) Math.round(width / 2.0 + (worldX - playerX) / blocksPerPixelX);
    }
    private int worldZToScreen(double worldZ, double playerZ, int height, double blocksPerPixelZ) {
        return (int) Math.round(height / 2.0 + (worldZ - playerZ) / blocksPerPixelZ);
    }

    private void drawMapImage(GuiGraphics graphics, PeakMinimap minimap, double playerX, double playerZ,
                              int width, int height, double blocksPerPixelX, double blocksPerPixelZ) {
        PeakPaintRenderer.fill(graphics, minimap.getOutOfImageColor(), 0, 0, width, height);

        try {
            ResourceLocation texture = PeakValueResolver.requireLocation(minimap.getTexture());
            double blocksPerImgPixelX = (minimap.getWorldMaxX() - minimap.getWorldMinX()) / minimap.getImageWidth();
            double blocksPerImgPixelZ = (minimap.getWorldMaxZ() - minimap.getWorldMinZ()) / minimap.getImageHeight();
            if (blocksPerImgPixelX <= 0 || blocksPerImgPixelZ <= 0) return;

            double visibleMinWorldX = playerX - width / 2.0 * blocksPerPixelX;
            double visibleMaxWorldX = playerX + width / 2.0 * blocksPerPixelX;
            double visibleMinWorldZ = playerZ - height / 2.0 * blocksPerPixelZ;
            double visibleMaxWorldZ = playerZ + height / 2.0 * blocksPerPixelZ;

            double clippedMinWorldX = Math.max(visibleMinWorldX, minimap.getWorldMinX());
            double clippedMaxWorldX = Math.min(visibleMaxWorldX, minimap.getWorldMaxX());
            double clippedMinWorldZ = Math.max(visibleMinWorldZ, minimap.getWorldMinZ());
            double clippedMaxWorldZ = Math.min(visibleMaxWorldZ, minimap.getWorldMaxZ());
            if (clippedMaxWorldX <= clippedMinWorldX || clippedMaxWorldZ <= clippedMinWorldZ) return;

            int destX = worldXToScreen(clippedMinWorldX, playerX, width, blocksPerPixelX);
            int destRight = worldXToScreen(clippedMaxWorldX, playerX, width, blocksPerPixelX);
            int destY = worldZToScreen(clippedMinWorldZ, playerZ, height, blocksPerPixelZ);
            int destBottom = worldZToScreen(clippedMaxWorldZ, playerZ, height, blocksPerPixelZ);

            int srcX = (int) Math.round((clippedMinWorldX - minimap.getWorldMinX()) / blocksPerImgPixelX);
            int srcY = (int) Math.round((clippedMinWorldZ - minimap.getWorldMinZ()) / blocksPerImgPixelZ);
            int srcRight = (int) Math.round((clippedMaxWorldX - minimap.getWorldMinX()) / blocksPerImgPixelX);
            int srcBottom = (int) Math.round((clippedMaxWorldZ - minimap.getWorldMinZ()) / blocksPerImgPixelZ);
            int sourceWidth = srcRight - srcX;
            int sourceHeight = srcBottom - srcY;
            if (sourceWidth <= 0 || sourceHeight <= 0) return;

            PeakImmediateRender.blit(graphics, texture, destX, destY, destRight - destX, destBottom - destY,
                    srcX, srcY, sourceWidth, sourceHeight, minimap.getImageWidth(), minimap.getImageHeight());
        } catch (Exception ignored) {
        }
    }

    private void drawPlayerIcon(GuiGraphics graphics, Minecraft mc, PeakMinimap minimap, int width, int height) {
        if (minimap.getPlayerIconMode() == PeakMinimapPlayerIconMode.NONE) return;

        int size = minimap.getPlayerIconSize();
        int cx = width / 2, cy = height / 2;
        float rotationDegrees = mc.player.getYRot() - minimap.getPlayerIconRotationOffset();

        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0);
        graphics.pose().mulPose(new Quaternionf().rotateZ((float) Math.toRadians(rotationDegrees)));
        graphics.pose().translate(-size / 2f, -size / 2f, 0);

        if (minimap.getPlayerIconMode() == PeakMinimapPlayerIconMode.PLAYER_HEAD
                && mc.player instanceof AbstractClientPlayer player) {
            PeakPlayerSkinRenderer.drawFace(graphics, player.getSkin(), 0, 0, size);
        } else if (minimap.getPlayerIconTexture() != null) {
            try {
                ResourceLocation texture = PeakValueResolver.requireLocation(minimap.getPlayerIconTexture());
                PeakImmediateRender.blit(graphics, texture, 0, 0, 0, 0, size, size, size, size);
            } catch (Exception ignored) { }
        }

        graphics.pose().popPose();
    }

    private void drawWaypoints(GuiGraphics graphics, Font font, PeakMinimap minimap, double playerX, double playerZ,
                               double camX, double camZ,
                               int width, int height, double blocksPerPixelX, double blocksPerPixelZ) {
        int cx = width / 2, cy = height / 2;

        for (PeakWaypoint waypoint : PeakClientWaypointRegistry.getVisibleIn(minimap.getId())) {
            int screenX = worldXToScreen(waypoint.getX(), camX, width, blocksPerPixelX);
            int screenY = worldZToScreen(waypoint.getZ(), camZ, height, blocksPerPixelZ);
            boolean onScreen = screenX >= 0 && screenX <= width && screenY >= 0 && screenY <= height;

            double distance = Math.hypot(waypoint.getX() - playerX, waypoint.getZ() - playerZ);
            int drawX, drawY;
            float scale;
            boolean showLabel;

            if (onScreen) {
                drawX = screenX; drawY = screenY;
                scale = waypoint.getIconScale();
                showLabel = waypoint.isLabelOnMap();
            } else {
                if (minimap.getEdgeIndicatorMaxDistance() <= 0 || distance > minimap.getEdgeIndicatorMaxDistance()) {
                    continue;
                }
                scale = waypoint.getIconScale() * distanceEdgeIconScale(minimap, distance);
                float margin = 6 * scale / 2f + 2;
                PeakPoint edgePosition = clampToEdge(cx, cy, screenX, screenY, width, height, margin);
                drawX = edgePosition.x(); drawY = edgePosition.y();
                showLabel = false;
            }

            drawWaypointIcon(graphics, font, waypoint, drawX, drawY, scale, showLabel, minimap.getWaypointLabelScale());
        }
    }

    private PeakPoint clampToEdge(int cx, int cy, int targetX, int targetY, int width, int height, float margin) {
        double dx = targetX - cx;
        double dy = targetY - cy;
        double halfWidth = width / 2.0 - margin;
        double halfHeight = height / 2.0 - margin;

        double scaleX = dx != 0 ? Math.abs(halfWidth / dx) : Double.MAX_VALUE;
        double scaleY = dy != 0 ? Math.abs(halfHeight / dy) : Double.MAX_VALUE;
        double t = Math.min(scaleX, scaleY);

        return new PeakPoint(
                (int) Math.round(cx + dx * t),
                (int) Math.round(cy + dy * t)
        );
    }

    private float distanceEdgeIconScale(PeakMinimap minimap, double distance) {
        double minD = minimap.getEdgeIconScaleMinDistance();
        double maxD = minimap.getEdgeIconScaleMaxDistance();
        if (maxD <= minD) return minimap.getEdgeIconMaxScale();
        double t = Math.max(0, Math.min(1, (distance - minD) / (maxD - minD)));
        return (float) (minimap.getEdgeIconMaxScale() + (minimap.getEdgeIconMinScale() - minimap.getEdgeIconMaxScale()) * t);
    }

    private void drawWaypointIcon(GuiGraphics graphics, Font font, PeakWaypoint waypoint, int x, int y, float iconScale,
                                  boolean showLabel, float labelScale) {
        int baseSize = 6;
        int size = Math.max(2, Math.round(baseSize * iconScale));

        if (waypoint.getIcon() != null) {
            try {
                ResourceLocation texture = PeakValueResolver.requireLocation(waypoint.getIcon());
                PeakImmediateRender.blit(graphics, texture, x - size / 2, y - size / 2, 0, 0, size, size, size, size);
            } catch (Exception ignored) { }
        } else {
            PeakPaintRenderer.fill(graphics, waypoint.getColor(), x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        }

        if (showLabel && waypoint.getLabel() != null && !waypoint.getLabel().isEmpty()) {
            int labelWidth = Math.round(font.width(waypoint.getLabel()) * labelScale);
            int textX = x - labelWidth / 2;
            int textY = y + size / 2 + 1;

            graphics.pose().pushPose();
            graphics.pose().translate(textX, textY, 0);
            graphics.pose().scale(labelScale, labelScale, 1f);
            PeakImmediateRender.text(graphics, font, waypoint.getLabel(), 0, 0, waypoint.getColor(), true);
            graphics.pose().popPose();
        }
    }

    private void drawWorldBorder(GuiGraphics graphics, PeakMinimap minimap, double playerX, double playerZ,
                                 int width, int height, double blocksPerPixelX, double blocksPerPixelZ) {
        if (minimap.getWorldBorderSource() == PeakWorldBorderSource.NONE) return;

        double minX, minZ, maxX, maxZ;
        if (minimap.getWorldBorderSource() == PeakWorldBorderSource.VANILLA) {
            WorldBorder border = Minecraft.getInstance().level.getWorldBorder();
            minX = border.getMinX(); minZ = border.getMinZ();
            maxX = border.getMaxX(); maxZ = border.getMaxZ();
        } else {
            if (!PeakClientCustomWorldBorderState.isActive()) return;
            minX = PeakClientCustomWorldBorderState.currentMinX();
            minZ = PeakClientCustomWorldBorderState.currentMinZ();
            maxX = PeakClientCustomWorldBorderState.currentMaxX();
            maxZ = PeakClientCustomWorldBorderState.currentMaxZ();
        }

        if (minimap.getWorldBorderShape() == PeakBorderShape.SQUARE) {
            drawSquareBorder(graphics, minimap, playerX, playerZ, width, height, blocksPerPixelX, blocksPerPixelZ, minX, minZ, maxX, maxZ);
        } else {
            double centerX = (minX + maxX) / 2.0;
            double centerZ = (minZ + maxZ) / 2.0;
            double radius = Math.min(maxX - minX, maxZ - minZ) / 2.0;
            drawCircleBorder(graphics, minimap, playerX, playerZ, width, height, blocksPerPixelX, blocksPerPixelZ, centerX, centerZ, radius);
        }
    }

    private void drawSquareBorder(GuiGraphics graphics, PeakMinimap minimap, double playerX, double playerZ,
                                  int width, int height, double blocksPerPixelX, double blocksPerPixelZ,
                                  double minX, double minZ, double maxX, double maxZ) {
        int screenMinX = worldXToScreen(minX, playerX, width, blocksPerPixelX);
        int screenMaxX = worldXToScreen(maxX, playerX, width, blocksPerPixelX);
        int screenMinY = worldZToScreen(minZ, playerZ, height, blocksPerPixelZ);
        int screenMaxY = worldZToScreen(maxZ, playerZ, height, blocksPerPixelZ);

        if (!minimap.getOutOfBorderColor().isNone()) {
            PeakPaint outsideColor = minimap.getOutOfBorderColor();
            int clampedSxMin = Math.max(0, screenMinX), clampedSxMax = Math.min(width, screenMaxX);
            int clampedSyMin = Math.max(0, screenMinY), clampedSyMax = Math.min(height, screenMaxY);

            if (screenMinY > 0) PeakPaintRenderer.fill(graphics, outsideColor, 0, 0, width, Math.min(height, screenMinY), 0, 0, width, height);
            if (screenMaxY < height) PeakPaintRenderer.fill(graphics, outsideColor, 0, Math.max(0, screenMaxY), width, height, 0, 0, width, height);
            if (screenMinX > 0) PeakPaintRenderer.fill(graphics, outsideColor, 0, clampedSyMin, Math.min(width, screenMinX), clampedSyMax, 0, 0, width, height);
            if (screenMaxX < width) PeakPaintRenderer.fill(graphics, outsideColor, Math.max(0, screenMaxX), clampedSyMin, width, clampedSyMax, 0, 0, width, height);
        }

        int thickness = Math.max(1, minimap.getWorldBorderThickness());
        PeakPaint color = minimap.getWorldBorderColor();

        int edgeXStart = clamp(screenMinX - thickness / 2, 0, width);
        int edgeXEnd = clamp(screenMaxX + thickness / 2, 0, width);
        int edgeYStart = clamp(screenMinY - thickness / 2, 0, height);
        int edgeYEnd = clamp(screenMaxY + thickness / 2, 0, height);

        if (edgeXEnd > edgeXStart) {
            if (screenMinY >= -thickness && screenMinY <= height + thickness) {
                PeakPaintRenderer.fill(graphics, color, edgeXStart, screenMinY - thickness / 2, edgeXEnd, screenMinY + thickness / 2, 0, 0, width, height);
            }
            if (screenMaxY >= -thickness && screenMaxY <= height + thickness) {
                PeakPaintRenderer.fill(graphics, color, edgeXStart, screenMaxY - thickness / 2, edgeXEnd, screenMaxY + thickness / 2, 0, 0, width, height);
            }
        }
        if (edgeYEnd > edgeYStart) {
            if (screenMinX >= -thickness && screenMinX <= width + thickness) {
                PeakPaintRenderer.fill(graphics, color, screenMinX - thickness / 2, edgeYStart, screenMinX + thickness / 2, edgeYEnd, 0, 0, width, height);
            }
            if (screenMaxX >= -thickness && screenMaxX <= width + thickness) {
                PeakPaintRenderer.fill(graphics, color, screenMaxX - thickness / 2, edgeYStart, screenMaxX + thickness / 2, edgeYEnd, 0, 0, width, height);
            }
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void drawCircleBorder(GuiGraphics graphics, PeakMinimap minimap, double playerX, double playerZ,
                                  int width, int height, double blocksPerPixelX, double blocksPerPixelZ,
                                  double centerX, double centerZ, double radius) {
        float cx = worldXToScreen(centerX, playerX, width, blocksPerPixelX);
        float cy = worldZToScreen(centerZ, playerZ, height, blocksPerPixelZ);
        float radiusPx = (float) (radius / ((blocksPerPixelX + blocksPerPixelZ) / 2.0));

        if (!minimap.getOutOfBorderColor().isNone()) {
            float farRadius = (float) Math.hypot(width, height) + 16;
            fillRing(graphics, cx, cy, radiusPx, farRadius, minimap.getOutOfBorderColor(), width, height);
        }
        fillRing(graphics, cx, cy, radiusPx - minimap.getWorldBorderThickness(), radiusPx, minimap.getWorldBorderColor(), width, height);
    }

    private void drawBorderOverlay(GuiGraphics graphics, PeakMinimap minimap, int width, int height) {
        if (minimap.getBorderTexture() != null) {
            try {
                ResourceLocation texture = PeakValueResolver.requireLocation(minimap.getBorderTexture());
                PeakUiRenderer.blitNineSlice(graphics, texture, 0, 0, width, height,
                        minimap.getBorderTextureWidth(), minimap.getBorderTextureHeight(), minimap.getBorderTextureCorner());
            } catch (Exception ignored) { }
        } else {
            PeakPaintRenderer.drawFrame(graphics, minimap.getBorderColor(), width, height, 2);
        }
    }

    private void fillRing(GuiGraphics graphics, float cx, float cy, float innerRadius, float outerRadius, PeakPaint paint,
                          int boxWidth, int boxHeight) {
        int segments = 48;
        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        PeakPaintRenderer.Sampler sampler = PeakPaintRenderer.sampler(paint, 0, 0, boxWidth, boxHeight);

        for (int i = 0; i < segments; i++) {
            float startAngle = (float) (2 * Math.PI * i / segments);
            float endAngle = (float) (2 * Math.PI * (i + 1) / segments);
            float outerStartX = cx + outerRadius * (float) Math.cos(startAngle), outerStartY = cy + outerRadius * (float) Math.sin(startAngle);
            float outerEndX = cx + outerRadius * (float) Math.cos(endAngle), outerEndY = cy + outerRadius * (float) Math.sin(endAngle);
            float innerStartX = cx + innerRadius * (float) Math.cos(startAngle), innerStartY = cy + innerRadius * (float) Math.sin(startAngle);
            float innerEndX = cx + innerRadius * (float) Math.cos(endAngle), innerEndY = cy + innerRadius * (float) Math.sin(endAngle);

            sampler.vertex(buffer, matrix, innerStartX, innerStartY);
            sampler.vertex(buffer, matrix, outerStartX, outerStartY);
            sampler.vertex(buffer, matrix, outerEndX, outerEndY);
            sampler.vertex(buffer, matrix, innerStartX, innerStartY);
            sampler.vertex(buffer, matrix, outerEndX, outerEndY);
            sampler.vertex(buffer, matrix, innerEndX, innerEndY);
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        PeakRenderStats.countShape();
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
