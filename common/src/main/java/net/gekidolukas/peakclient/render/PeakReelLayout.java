package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakReelMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PeakReelLayout {
    private static final int MAX_DRAWN = 256;

    private PeakReelLayout() {}

    static void render(GuiGraphics graphics, Font font, PeakReel reel,
                       int contentX, int contentY, int contentWidth, int contentHeight,
                       int absX, int absY, float absScale, float scissorUnitFactor, String documentId, int depth) {
        List<PeakElement<?>> items = new ArrayList<>();
        for (PeakElement<?> child : reel.getChildren()) {
            if (child.isHidden() || PeakUiRenderer.isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            items.add(child);
        }
        int count = items.size();
        if (count == 0) return;

        boolean horizontal = reel.getMovement().isHorizontal();
        boolean reversed = reel.getMovement().isReversed();
        int viewport = horizontal ? contentWidth : contentHeight;
        int crossSize = horizontal ? contentHeight : contentWidth;
        int gap = reel.getGap();

        double now = PeakClientTickCounter.exact();
        PeakReelMotion motion = reel.motion();
        motion.ensureInitialized(Math.min(reel.getStartIndex(), count - 1));
        List<String> ids = new ArrayList<>(count);
        for (PeakElement<?> item : items) ids.add(item.getId());
        List<String> known = reel.knownIds();
        if (known != null && !known.equals(ids)) {
            Map<String, Integer> newIndices = new HashMap<>();
            for (int i = 0; i < ids.size(); i++) newIndices.put(ids.get(i), i);
            motion.remap(known.size(), count, oldSlot -> newIndices.getOrDefault(known.get(Math.min(oldSlot, known.size() - 1)), -1), now);
        }
        reel.rememberIds(ids);

        if (reel.isAutoSpinPending(now)) {
            int target = resolveTarget(items, reel.getAutoSpinTargetId(), reel.getAutoSpinTarget(), count);
            if (target >= 0) {
                motion.spin(count, target, now, reel.getSpinTicks(), reel.getMinLoops(), reel.getEasing());
            }
            reel.finishAutoSpin();
        }
        PeakReel.SpinRequest spin = reel.takeSpinRequest();
        if (spin != null) {
            int target = resolveTarget(items, spin.targetId(), spin.target(), count);
            if (target >= 0) {
                int ticks = spin.ticks() > 0 ? spin.ticks() : reel.getSpinTicks();
                int loops = spin.loops() >= 0 ? spin.loops() : reel.getMinLoops();
                motion.spin(count, target, now, ticks, loops, reel.getEasing());
            }
        }
        int jump = reel.takeJumpRequest();
        if (jump >= 0) motion.jump(Math.min(jump, count - 1));
        if (reel.takeStopRequest()) {
            int next = (int) Math.ceil(motion.position(now) - 1e-6);
            motion.spin(count, (int) PeakReelMotion.mod(next, count), now, 20, 0, reel.getEasing());
        }
        motion.normalize(count);
        double position = motion.position(now);

        int[] lengths = new int[count];
        double[] centers = new double[count];
        double ring = 0.0;
        for (int i = 0; i < count; i++) {
            lengths[i] = Math.max(1, mainLength(font, items.get(i), horizontal));
            centers[i] = ring + lengths[i] / 2.0;
            ring += lengths[i] + gap;
        }

        int base = (int) Math.floor(position);
        double fraction = position - base;
        int slot = (int) PeakReelMotion.mod(base, count);
        double nextCenter = slot + 1 < count ? centers[slot + 1] : centers[0] + ring;
        double center = centers[slot] + fraction * (nextCenter - centers[slot]);
        double half = viewport / 2.0;

        boolean fading = (reel.getFadeStart() > 0 || reel.getFadeEnd() > 0) && viewport > 0;
        Minecraft minecraft = Minecraft.getInstance();
        if (fading) PeakOffscreenOpacity.begin(minecraft, graphics);
        try {
            int drawn = 0;
            for (int i = 0; i < count && drawn < MAX_DRAWN; i++) {
                PeakElement<?> child = items.get(i);
                double reach = half + lengths[i] / 2.0;
                int firstWrap = (int) Math.ceil((center - reach - centers[i]) / ring);
                int lastWrap = (int) Math.floor((center + reach - centers[i]) / ring);
                for (int wrap = firstWrap; wrap <= lastWrap && drawn < MAX_DRAWN; wrap++) {
                    double offset = centers[i] + wrap * ring - center;
                    double itemCenter = half + (reversed ? -offset : offset);
                    int mainStart = (int) Math.round(itemCenter - lengths[i] / 2.0);

                    PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
                    PeakInsets margin = child.getStyle().getMargin();
                    int crossAvailable = crossSize - (horizontal ? margin.getTop() + margin.getBottom() : margin.getLeft() + margin.getRight());
                    int childCross = horizontal ? footprint.height() : footprint.width();
                    int crossOffset = switch (reel.getAlignChildren()) {
                        case CENTER -> Math.max(0, (crossAvailable - childCross) / 2);
                        case END -> Math.max(0, crossAvailable - childCross);
                        case START, AUTO -> 0;
                    };
                    int x = horizontal
                            ? contentX + mainStart + margin.getLeft() + child.getPosition().getOffsetX()
                            : contentX + margin.getLeft() + crossOffset + child.getPosition().getOffsetX();
                    int y = horizontal
                            ? contentY + margin.getTop() + crossOffset + child.getPosition().getOffsetY()
                            : contentY + mainStart + margin.getTop() + child.getPosition().getOffsetY();
                    PeakUiRenderer.renderChild(graphics, font, child, x, y, absX, absY, absScale, scissorUnitFactor, documentId, depth + 1);
                    drawn++;
                }
            }
        } finally {
            if (fading) {
                PeakEdgeFade.finish(minecraft, graphics, absX, absY, absScale, scissorUnitFactor,
                        contentX, contentY, contentWidth, contentHeight, reel.getFadeStart(), reel.getFadeEnd(), horizontal);
            }
        }
    }

    private static int resolveTarget(List<PeakElement<?>> items, String targetId, int index, int count) {
        if (targetId != null && !targetId.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId().equals(targetId)) return i;
            }
            PeakLog.warn("Reel spin target '{}' is not a visible child of the reel - ignored", targetId);
            return -1;
        }
        return Math.max(0, Math.min(index, count - 1));
    }

    private static int mainLength(Font font, PeakElement<?> child, boolean horizontal) {
        PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
        PeakInsets margin = child.getStyle().getMargin();
        return horizontal ? footprint.width() + margin.getLeft() + margin.getRight()
                : footprint.height() + margin.getTop() + margin.getBottom();
    }
}
