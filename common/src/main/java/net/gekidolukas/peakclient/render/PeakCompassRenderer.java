package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakCompass;
import net.gekidolukas.peakclient.ui.PeakCompassOverlapMode;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakCompassStackDirection;
import net.gekidolukas.peakclient.ui.PeakCompassTickPosition;
import net.gekidolukas.peakclient.ui.PeakEntityTrackingMode;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakWaypoint;
import net.gekidolukas.peakclient.ui.client.PeakClientWaypointRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Style;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PeakCompassRenderer implements PeakElementRenderer<PeakCompass> {
    private static final String[] COMPASS_DIRS8 = { "south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast" };
    private static final int ICON_EDGE_GAP = 1;

    @Override
    public void renderContent(PeakRenderContext ctx, PeakCompass compass, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PeakPaintRenderer.fill(ctx.graphics(), compass.getBackgroundColor(), 0, 0, width, height);

        float playerYaw = mc.player.getYRot();
        float halfFov = compass.getFieldOfViewDegrees() / 2f;

        if (compass.isShowTickMarks()) {
            PeakPaint tickColor = compass.getTickColor();
            float skipModulo = compass.getTickPosition() == PeakCompassTickPosition.TOP ? 45f : 90f;
            for (float bearing = 0; bearing < 360f; bearing += compass.getTickIntervalDegrees()) {
                float relative = normalizeAngle(bearing - playerYaw);
                if (Math.abs(relative) > halfFov) continue;
                if (bearing % skipModulo < 0.01f) continue;
                int x = Math.round(width / 2f + (relative / halfFov) * (width / 2f));
                if (compass.getTickPosition() == PeakCompassTickPosition.BOTTOM) {
                    PeakPaintRenderer.fill(ctx.graphics(), tickColor, x, height - 3, x + 1, height, 0, 0, width, height);
                } else {
                    PeakPaintRenderer.fill(ctx.graphics(), tickColor, x, 0, x + 1, 3, 0, 0, width, height);
                }
            }
        }

        for (int i = 0; i < 8; i++) {
            boolean isCardinal = i % 2 == 0;
            if (isCardinal && !compass.isShowCardinalLabels()) continue;
            if (!isCardinal && !compass.isShowIntercardinalLabels()) continue;

            float bearing = i * 45f;
            float relative = normalizeAngle(bearing - playerYaw);
            if (Math.abs(relative) > halfFov) continue;

            int x = Math.round(width / 2f + (relative / halfFov) * (width / 2f));
            String label = I18n.get("peakclient.direction." + COMPASS_DIRS8[i]);

            if (isCardinal) {
                PeakImmediateRender.text(ctx.graphics(),ctx.font(), label, x - ctx.font().width(label) / 2, 1, compass.getTickColor(), true);
            } else {
                float scale = 0.7f;
                ctx.graphics().pose().pushPose();
                ctx.graphics().pose().translate(x - ctx.font().width(label) * scale / 2f, 1, 0);
                ctx.graphics().pose().scale(scale, scale, 1f);
                PeakImmediateRender.text(ctx.graphics(),ctx.font(), label, 0, 0, compass.getTickColor(), true);
                ctx.graphics().pose().popPose();
            }
        }

        renderWaypoints(ctx.graphics(), ctx.font(), compass, mc, width, height, playerYaw, halfFov);
    }
    @Override
    public PeakPixelSize measureContent(Font font, PeakCompass compass) { return PeakPixelSize.ZERO; }

    private record RawWaypointEntry(PeakWaypoint waypoint, double distance, int x, PeakStyledText label) {}

    private static final class WaypointCandidate {
        final PeakWaypoint representative;
        final double distance;
        int x;
        PeakStyledText labelText = PeakStyledText.EMPTY;
        int labelWidth;
        int left, right;
        int clusterId = -1;
        int row = 0;

        WaypointCandidate(PeakWaypoint representative, double distance) {
            this.representative = representative;
            this.distance = distance;
        }
    }

    private static float normalizeAngle(float degrees) {
        float normalized = degrees % 360f;
        if (normalized > 180f) normalized -= 360f;
        if (normalized < -180f) normalized += 360f;
        return normalized;
    }

    static void renderWaypoints(GuiGraphics graphics, Font font, PeakCompass compass, Minecraft mc,
                                int width, int height, float playerYaw, float halfFov) {
        updateTrackedWaypoints(compass, mc);

        List<RawWaypointEntry> raw = new ArrayList<>();
        for (PeakWaypoint waypoint : PeakClientWaypointRegistry.getVisibleIn(compass.getId())) {
            double dx = waypoint.getX() - mc.player.getX();
            double dz = waypoint.getZ() - mc.player.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (compass.getMaxDistance() > 0 && distance > compass.getMaxDistance()) continue;

            float bearing = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float relative = normalizeAngle(bearing - playerYaw);
            if (Math.abs(relative) > halfFov) continue;

            int x = Math.round(width / 2f + (relative / halfFov) * (width / 2f));
            PeakStyledText label = resolveWaypointLabel(waypoint, mc);
            raw.add(new RawWaypointEntry(waypoint, distance, x, label));
        }
        if (raw.isEmpty()) return;

        List<WaypointCandidate> candidates = compass.isGroupSameNameWaypoints()
                ? mergeByNameAndProximity(raw, compass.getSameNameMergeDistance())
                : rawToCandidate(raw);

        int markerSize = compass.getWaypointIconSize();
        int spacing = Math.max(1, compass.getStackSpacing());
        for (WaypointCandidate candidate : candidates) {
            candidate.labelWidth = candidate.labelText.width(font);
            int halfWidth = Math.max(markerSize, candidate.labelWidth) / 2 + spacing;
            candidate.left = candidate.x - halfWidth;
            candidate.right = candidate.x + halfWidth;
        }

        if (compass.getOverlapMode() == PeakCompassOverlapMode.STACK) {
            List<WaypointCandidate> byLeft = new ArrayList<>(candidates);
            byLeft.sort(Comparator.comparingInt(candidate -> candidate.left));
            int clusterId = -1;
            int currentRight = Integer.MIN_VALUE;
            for (WaypointCandidate candidate : byLeft) {
                if (clusterId == -1 || candidate.left > currentRight) {
                    clusterId++;
                    currentRight = candidate.right;
                } else {
                    currentRight = Math.max(currentRight, candidate.right);
                }
                candidate.clusterId = clusterId;
            }

            Map<Integer, List<WaypointCandidate>> byCluster = new LinkedHashMap<>();
            for (WaypointCandidate candidate : candidates) byCluster.computeIfAbsent(candidate.clusterId, key -> new ArrayList<>()).add(candidate);
            for (List<WaypointCandidate> cluster : byCluster.values()) {
                cluster.sort(Comparator.comparingDouble(candidate -> candidate.distance));
                List<Integer> rowRightEdges = new ArrayList<>();
                for (WaypointCandidate candidate : cluster) {
                    int assignedRow = -1;
                    for (int row = 0; row < rowRightEdges.size(); row++) {
                        if (candidate.left >= rowRightEdges.get(row)) { assignedRow = row; break; }
                    }
                    if (assignedRow == -1) { assignedRow = rowRightEdges.size(); rowRightEdges.add(candidate.right); }
                    else rowRightEdges.set(assignedRow, candidate.right);
                    candidate.row = assignedRow;
                }
            }
        } else {
            for (WaypointCandidate candidate : candidates) { candidate.clusterId = 0; candidate.row = 0; }
        }

        int overflowCount = 0;
        int rowStep = markerSize + spacing;
        boolean stackUp = compass.getStackDirection() == PeakCompassStackDirection.UP;

        Map<Integer, List<WaypointCandidate>> byCluster = new LinkedHashMap<>();
        for (WaypointCandidate candidate : candidates) byCluster.computeIfAbsent(candidate.clusterId, key -> new ArrayList<>()).add(candidate);

        for (List<WaypointCandidate> cluster : byCluster.values()) {
            List<WaypointCandidate> visible = new ArrayList<>();
            for (WaypointCandidate candidate : cluster) {
                if (compass.getOverlapMode() == PeakCompassOverlapMode.STACK && candidate.row >= compass.getMaxStackedWaypoints()) {
                    overflowCount++;
                    continue;
                }
                visible.add(candidate);
            }
            if (visible.isEmpty()) continue;

            int clusterXSum = 0;
            for (WaypointCandidate candidate : visible) {
                int markerY = stackUp ? (height - markerSize - candidate.row * rowStep) : (candidate.row * rowStep);
                int effectiveOffset = candidate.representative.getLabelYOffset() != PeakWaypoint.AUTO_OFFSET
                        ? candidate.representative.getLabelYOffset()
                        : compass.getWaypointLabelYOffset() != PeakWaypoint.AUTO_OFFSET
                        ? compass.getWaypointLabelYOffset()
                        : 0;
                int labelY = stackUp
                        ? markerY - font.lineHeight - effectiveOffset
                        : markerY + markerSize + effectiveOffset;

                if (!candidate.labelText.isEmpty()) {
                    PeakImmediateRender.text(graphics, font, candidate.labelText, candidate.x - candidate.labelWidth / 2, labelY,
                            candidate.representative.getColor(), true);
                }
                clusterXSum += candidate.x;
            }

            LinkedHashMap<String, WaypointCandidate> distinctIcons = new LinkedHashMap<>();
            for (WaypointCandidate candidate : visible) {
                String key = iconKey(candidate.representative);
                if (key == null) continue;
                distinctIcons.putIfAbsent(key, candidate);
            }
            if (distinctIcons.isEmpty()) continue;

            int clusterCenterX = clusterXSum / visible.size();
            int iconGap = 1;
            List<WaypointCandidate> iconOwners = new ArrayList<>(distinctIcons.values());
            int[] sizes = new int[iconOwners.size()];
            int totalWidth = 0;
            for (int i = 0; i < iconOwners.size(); i++) {
                float scale = distanceIconScale(compass, iconOwners.get(i).distance) * iconOwners.get(i).representative.getIconScale();
                sizes[i] = Math.max(1, Math.round(markerSize * scale));
                totalWidth += sizes[i] + (i > 0 ? iconGap : 0);
            }

            int startX = clusterCenterX - totalWidth / 2;
            int cursorX = startX;
            for (int i = 0; i < iconOwners.size(); i++) {
                int size = sizes[i];
                int iconY = stackUp ? ICON_EDGE_GAP : (height - ICON_EDGE_GAP - size);
                renderWaypointIcon(graphics, mc, iconOwners.get(i).representative, cursorX, iconY, size);
                cursorX += size + iconGap;
            }
        }

        if (overflowCount > 0) {
            String overflowText = "+" + overflowCount;
            PeakImmediateRender.text(graphics, font, overflowText, width - font.width(overflowText) - 2, 2,
                    compass.getTickColor(), true);
        }
    }

    private static void updateTrackedWaypoints(PeakCompass compass, Minecraft mc) {
        for (PeakWaypoint waypoint : PeakClientWaypointRegistry.getVisibleIn(compass.getId())) {
            if (waypoint.getEntityTrackingMode() == PeakEntityTrackingMode.NONE || waypoint.getTrackedEntityUuid() == null) continue;
            Entity tracked = resolveEntityByUuid(mc, waypoint.getTrackedEntityUuid());
            if (tracked != null) {
                waypoint.updateLiveTrackedPosition(tracked.getX(), tracked.getY(), tracked.getZ());
            }
        }
    }

    private static Entity resolveEntityByUuid(Minecraft mc, String uuidString) {
        if (mc.level == null) return null;
        try {
            UUID uuid = UUID.fromString(uuidString);
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e.getUUID().equals(uuid)) return e;
            }
        } catch (IllegalArgumentException ignored) { }
        return null;
    }

    private static PeakStyledText waypointLabel(PeakWaypoint waypoint, String fallback) {
        return waypoint.getLabel() == null ? PeakStyledText.ofLegacy(fallback) : PeakTextResolver.resolved(waypoint, waypoint.getLabel());
    }

    private static PeakStyledText resolveWaypointLabel(PeakWaypoint waypoint, Minecraft mc) {
        if (waypoint.getEntityTrackingMode() != PeakEntityTrackingMode.NONE && waypoint.getTrackedEntityUuid() != null) {
            Entity tracked = resolveEntityByUuid(mc, waypoint.getTrackedEntityUuid());
            if (tracked != null) {
                return PeakStyledText.of(tracked.getDisplayName());
            }
            return waypointLabel(waypoint, "?");
        }
        return waypointLabel(waypoint, "");
    }

    private static List<WaypointCandidate> mergeByNameAndProximity(List<RawWaypointEntry> raw, int mergeDistance) {
        Map<String, List<RawWaypointEntry>> byName = new LinkedHashMap<>();
        for (RawWaypointEntry entry : raw) {
            String key = (entry.label() != null && !entry.label().isEmpty()) ? entry.label().plain() : ("#" + entry.waypoint().getId());
            byName.computeIfAbsent(key, name -> new ArrayList<>()).add(entry);
        }

        List<WaypointCandidate> result = new ArrayList<>();
        for (List<RawWaypointEntry> group : byName.values()) {
            group.sort(Comparator.comparingInt(RawWaypointEntry::x));

            List<RawWaypointEntry> currentRun = new ArrayList<>();
            int lastX = Integer.MIN_VALUE;

            for (RawWaypointEntry entry : group) {
                if (!currentRun.isEmpty() && entry.x() - lastX > mergeDistance) {
                    result.add(finalizeNameRun(currentRun));
                    currentRun = new ArrayList<>();
                }
                currentRun.add(entry);
                lastX = entry.x();
            }
            if (!currentRun.isEmpty()) result.add(finalizeNameRun(currentRun));
        }
        return result;
    }

    private static WaypointCandidate finalizeNameRun(List<RawWaypointEntry> run) {
        RawWaypointEntry nearest = run.get(0);
        int xSum = 0;
        for (RawWaypointEntry entry : run) {
            xSum += entry.x();
            if (entry.distance() < nearest.distance()) nearest = entry;
        }
        PeakStyledText label = run.size() > 1 ? withCount(nearest.label(), run.size()) : nearest.label();

        WaypointCandidate candidate = new WaypointCandidate(nearest.waypoint(), nearest.distance());
        candidate.x = xSum / run.size();
        candidate.labelText = label != null ? label : PeakStyledText.EMPTY;
        return candidate;
    }

    private static PeakStyledText withCount(PeakStyledText label, int count) {
        Style style = label.isEmpty() ? Style.EMPTY : label.styleAt(label.length() - 1);
        return label.concat(PeakStyledText.of(" x" + count, style));
    }

    private static List<WaypointCandidate> rawToCandidate(List<RawWaypointEntry> raw) {
        List<WaypointCandidate> result = new ArrayList<>();
        for (RawWaypointEntry entry : raw) {
            WaypointCandidate candidate = new WaypointCandidate(entry.waypoint(), entry.distance());
            candidate.x = entry.x();
            candidate.labelText = entry.label() != null ? entry.label() : PeakStyledText.EMPTY;
            result.add(candidate);
        }
        return result;
    }

    private static String iconKey(PeakWaypoint waypoint) {
        if (waypoint.getEntityTrackingMode() == PeakEntityTrackingMode.PLAYER_FACE) return "face:" + waypoint.getTrackedEntityUuid();
        return waypoint.getIcon();
    }

    private static float distanceIconScale(PeakCompass compass, double distance) {
        double minD = compass.getIconScaleMinDistance();
        double maxD = compass.getIconScaleMaxDistance();
        if (maxD <= minD) return compass.getMaxIconScale();
        double t = Math.max(0, Math.min(1, (distance - minD) / (maxD - minD)));
        return (float) (compass.getMaxIconScale() + (compass.getMinIconScale() - compass.getMaxIconScale()) * t);
    }

    static void renderWaypointIcon(GuiGraphics graphics, Minecraft mc, PeakWaypoint waypoint, int x, int y, int size) {
        if (waypoint.getEntityTrackingMode() == PeakEntityTrackingMode.PLAYER_FACE) {
            Entity tracked = resolveEntityByUuid(mc, waypoint.getTrackedEntityUuid());
            if (tracked instanceof AbstractClientPlayer player) {
                PeakPlayerSkinRenderer.drawFace(graphics, player.getSkin(), x, y, size);
            }
            return;
        }
        if (waypoint.getIcon() != null) {
            try {
                ResourceLocation texture = PeakValueResolver.requireLocation(waypoint.getIcon());
                PeakImmediateRender.blit(graphics, texture, x, y, 0, 0, size, size, size, size);
            } catch (Exception ignored) { }
        }
    }
}
