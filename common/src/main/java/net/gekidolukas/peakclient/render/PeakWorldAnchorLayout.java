package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.debug.PeakDebugState;
import net.gekidolukas.peakclient.ui.PeakAnchorEdge;
import net.gekidolukas.peakclient.ui.PeakAnchorScale;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakOcclusion;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.gekidolukas.peakclient.ui.client.PeakOcclusionTracker;
import net.gekidolukas.peakclient.ui.client.PeakWorldAnchorTargets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class PeakWorldAnchorLayout {
    private static final float EDGE_ARROW_GAP = 2f;
    private static final float MIN_VISIBLE_ALPHA = 0.01f;
    private static final Map<PeakWorldAnchor, Info> INFOS = Collections.synchronizedMap(new WeakHashMap<>());

    private PeakWorldAnchorLayout() {}

    public record Info(float screenX, float screenY, double distance, boolean occluded, float factor, boolean clamped, boolean drawn, boolean partial) {}

    private record Placement(PeakWorldAnchor anchor, double distance, float localX, float localY, float scale, float alpha,
                             boolean clamped, float directionX, float directionY, PeakPixelSize size,
                             boolean partial, float ndcZ) {}

    public static Info info(PeakWorldAnchor anchor) {
        return INFOS.get(anchor);
    }

    static void renderAll(GuiGraphics graphics, Font font, List<PeakWorldAnchor> anchors, int absX, int absY, float absScale,
                          float unitFactor, String documentId, int depth) {
        if (!PeakCameraState.isAvailable() || PeakMenuInteractionState.isActive()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        List<Placement> placements = new ArrayList<>();
        for (PeakWorldAnchor anchor : anchors) {
            Placement placement = place(anchor, font, minecraft, absX, absY, absScale, unitFactor);
            if (placement != null) placements.add(placement);
        }
        placements.sort(Comparator.comparingDouble(Placement::distance).reversed());
        for (Placement placement : placements) {
            draw(graphics, font, placement, absX, absY, absScale, unitFactor, documentId, depth);
        }
    }

    private static Placement place(PeakWorldAnchor anchor, Font font, Minecraft minecraft, int absX, int absY, float absScale,
                                   float unitFactor) {
        PeakWorldAnchorTargets.Resolved resolved = PeakWorldAnchorTargets.resolve(anchor, PeakCameraState.partialTick());
        if (resolved == null) {
            record(anchor, null);
            return null;
        }

        Vec3 camera = PeakCameraState.position();
        Vec3 point = resolved.point();
        float guiWidth = minecraft.getWindow().getGuiScaledWidth();
        float guiHeight = minecraft.getWindow().getGuiScaledHeight();
        PeakCameraProjection.Projection projection = PeakCameraProjection.project(PeakCameraState.view(), PeakCameraState.projection(),
                camera.x, camera.y, camera.z, point.x, point.y, point.z, guiWidth, guiHeight);

        float distanceAlpha = PeakAnchorMath.distanceAlpha(projection.distance(), anchor.getMinDistance(),
                anchor.getMaxDistance(), anchor.getFadeDistance());
        if (distanceAlpha <= MIN_VISIBLE_ALPHA) {
            record(anchor, null);
            return null;
        }

        PeakPixelSize size = PeakUiRenderer.footprint(font, anchor);
        float guiPerLocal = absScale * unitFactor;
        boolean inside = projection.isInside(guiWidth, guiHeight);

        if (inside) {
            float scale = anchor.getScaleMode() == PeakAnchorScale.PERSPECTIVE
                    ? PeakAnchorMath.perspectiveScale(projection.distance(), anchor.getReferenceDistance(), anchor.getMinScale(),
                    anchor.getMaxScale()) : 1f;
            boolean wantsPartial = anchor.getOcclusion().isPartial() && !PeakDepthCapture.hasFailed();
            if (wantsPartial) PeakDepthCapture.want();
            boolean partial = wantsPartial && !PeakOffscreenOpacity.isActive();
            if (partial && !PeakDepthCapture.isValid()) {
                record(anchor, null);
                return null;
            }
            float occlusion = partial ? 1f : PeakOcclusionTracker.factor(anchor, camera, point, resolved.entity());
            float alpha = distanceAlpha * occlusion;
            float ndcZ = partial ? biasedDepth(anchor, projection, camera, point, guiWidth, guiHeight) : 0f;
            Placement placement = new Placement(anchor, projection.distance(), toLocal(projection.x(), absX, absScale, unitFactor),
                    toLocal(projection.y(), absY, absScale, unitFactor), scale, alpha, false, 0f, 0f, size, partial, ndcZ);
            record(anchor, new Info(projection.x(), projection.y(), projection.distance(),
                    !partial && PeakOcclusionTracker.isOccluded(anchor), occlusion, false, alpha > MIN_VISIBLE_ALPHA, partial));
            return alpha > MIN_VISIBLE_ALPHA ? placement : null;
        }

        if (anchor.getEdge() != PeakAnchorEdge.CLAMP) {
            record(anchor, null);
            return null;
        }
        float contentWidth = anchor.isEdgeShowContent() ? size.width() * guiPerLocal : 0f;
        float contentHeight = anchor.isEdgeShowContent() ? size.height() * guiPerLocal : 0f;
        float arrow = anchor.getEdgeArrowSize() * guiPerLocal;
        float inset = anchor.getEdgeMargin() + arrow * 2f + EDGE_ARROW_GAP;
        PeakEdgeClamp.Result clamp = PeakEdgeClamp.clamp(projection.viewX(), projection.viewY(), guiWidth, guiHeight,
                inset + contentWidth / 2f, inset + contentHeight / 2f);
        record(anchor, new Info(clamp.x(), clamp.y(), projection.distance(), false, 1f, true, true, false));
        return new Placement(anchor, projection.distance(), toLocal(clamp.x(), absX, absScale, unitFactor),
                toLocal(clamp.y(), absY, absScale, unitFactor), 1f, distanceAlpha, true, clamp.directionX(), clamp.directionY(), size, false, 0f);
    }

    private static void draw(GuiGraphics graphics, Font font, Placement placement, int absX, int absY, float absScale,
                             float unitFactor, String documentId, int depth) {
        PeakWorldAnchor anchor = placement.anchor();
        PeakPixelSize size = placement.size();
        float pointX = placement.localX();
        float pointY = placement.localY();

        if (placement.clamped()) {
            if (anchor.isEdgeShowContent()) {
                drawContent(graphics, font, anchor, Math.round(pointX - size.width() / 2f), Math.round(pointY - size.height() / 2f),
                        pointX, pointY, 1f, placement.alpha(), absX, absY, absScale, unitFactor, documentId, depth);
            }
            drawArrow(graphics, placement, absScale, unitFactor);
            return;
        }

        int x = Math.round(switch (anchor.getPivot()) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> pointX;
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> pointX - size.width() / 2f;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> pointX - size.width();
        }) + anchor.getScreenOffsetX();
        int y = Math.round(switch (anchor.getPivot()) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> pointY;
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> pointY - size.height() / 2f;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> pointY - size.height();
        }) + anchor.getScreenOffsetY();
        if (placement.partial()) {
            drawPartial(graphics, font, placement, x, y, absX, absY, absScale, unitFactor, documentId, depth);
            return;
        }
        drawContent(graphics, font, anchor, x, y, pointX, pointY, placement.scale(), placement.alpha(), absX, absY, absScale,
                unitFactor, documentId, depth);
    }

    private static void drawPartial(GuiGraphics graphics, Font font, Placement placement, int x, int y, int absX, int absY,
                                    float absScale, float unitFactor, String documentId, int depth) {
        PeakWorldAnchor anchor = placement.anchor();
        Minecraft minecraft = Minecraft.getInstance();
        PeakOcclusion occlusion = anchor.getOcclusion();
        float hiddenAlpha = occlusion.getBehavior() == PeakOcclusion.Behavior.HIDE ? 0f : occlusion.getDimTo();
        PeakOffscreenOpacity.begin(minecraft, graphics);
        boolean finished = false;
        try {
            drawContent(graphics, font, anchor, x, y, placement.localX(), placement.localY(), placement.scale(), 1f, absX, absY,
                    absScale, unitFactor, documentId, depth);
            finished = true;
        } finally {
            if (!finished) PeakOffscreenOpacity.end(minecraft, graphics, 1f);
        }
        PeakDepthComposite.present(minecraft, graphics, placement.ndcZ(), Math.min(1f, placement.alpha()), hiddenAlpha,
                occlusion.getTranslucentAlpha());
    }

    private static float biasedDepth(PeakWorldAnchor anchor, PeakCameraProjection.Projection projection, Vec3 camera, Vec3 point,
                                     float guiWidth, float guiHeight) {
        double distance = projection.distance();
        double shift = Math.min(anchor.getOcclusion().getDepthBias(), Math.max(0.0, distance - 0.3));
        if (shift <= 0.0 || distance <= 1e-6) return projection.ndcZ();
        Vec3 toCamera = camera.subtract(point).scale(shift / distance);
        Vec3 shifted = point.add(toCamera);
        return PeakCameraProjection.project(PeakCameraState.view(), PeakCameraState.projection(), camera.x, camera.y, camera.z,
                shifted.x, shifted.y, shifted.z, guiWidth, guiHeight).ndcZ();
    }

    private static void drawContent(GuiGraphics graphics, Font font, PeakWorldAnchor anchor, int x, int y, float pivotX, float pivotY,
                                    float scale, float alpha, int absX, int absY, float absScale, float unitFactor,
                                    String documentId, int depth) {
        PeakEffectTransform transform = new PeakEffectTransform(0f, 0f, scale, 0f, Math.min(1f, alpha));
        PeakGroupTransform.paint(graphics, pivotX, pivotY, transform,
                () -> PeakUiRenderer.renderChild(graphics, font, anchor, x, y, absX, absY, absScale, unitFactor, documentId, depth + 1));
    }

    private static void drawArrow(GuiGraphics graphics, Placement placement, float absScale, float unitFactor) {
        PeakWorldAnchor anchor = placement.anchor();
        int arrowSize = anchor.getEdgeArrowSize();
        if (arrowSize <= 0) return;
        float directionX = placement.directionX(), directionY = placement.directionY();
        float halfWidth = anchor.isEdgeShowContent() ? placement.size().width() / 2f : 0f;
        float halfHeight = anchor.isEdgeShowContent() ? placement.size().height() / 2f : 0f;
        float reachX = Math.abs(directionX) < 1e-4f ? Float.MAX_VALUE : halfWidth / Math.abs(directionX);
        float reachY = Math.abs(directionY) < 1e-4f ? Float.MAX_VALUE : halfHeight / Math.abs(directionY);
        float reach = anchor.isEdgeShowContent() ? Math.min(reachX, reachY) : 0f;
        float centerX = placement.localX() + directionX * (reach + arrowSize * 0.5f + EDGE_ARROW_GAP);
        float centerY = placement.localY() + directionY * (reach + arrowSize * 0.5f + EDGE_ARROW_GAP);

        float perpendicularX = -directionY, perpendicularY = directionX;
        float[] points = {
                centerX + directionX * arrowSize, centerY + directionY * arrowSize,
                centerX - directionX * arrowSize * 0.5f + perpendicularX * arrowSize * 0.8f,
                centerY - directionY * arrowSize * 0.5f + perpendicularY * arrowSize * 0.8f,
                centerX - directionX * arrowSize * 0.5f - perpendicularX * arrowSize * 0.8f,
                centerY - directionY * arrowSize * 0.5f - perpendicularY * arrowSize * 0.8f};
        float cross = (points[2] - points[0]) * (points[5] - points[1]) - (points[3] - points[1]) * (points[4] - points[0]);
        if (cross < 0f) {
            float swapX = points[2], swapY = points[3];
            points[2] = points[4];
            points[3] = points[5];
            points[4] = swapX;
            points[5] = swapY;
        }
        PeakPaint paint = anchor.getEdgeArrowColor().withAlpha(Math.min(1f, placement.alpha()));
        int boxX = Math.round(centerX - arrowSize), boxY = Math.round(centerY - arrowSize);
        PeakPolygonRenderer.fill(graphics, paint, points, boxX, boxY, boxX + arrowSize * 2, boxY + arrowSize * 2);
    }

    private static float toLocal(float screen, int abs, float absScale, float unitFactor) {
        return (screen / unitFactor - abs) / absScale;
    }

    private static void record(PeakWorldAnchor anchor, Info info) {
        if (!PeakDebugState.isEnabled()) return;
        if (info == null) INFOS.put(anchor, new Info(Float.NaN, Float.NaN, 0, false, 0f, false, false, false));
        else INFOS.put(anchor, info);
    }
}
