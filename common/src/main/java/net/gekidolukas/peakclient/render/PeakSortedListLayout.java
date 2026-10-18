package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakAnimState;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakMotionTracker;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.PeakTransitions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

final class PeakSortedListLayout {
    private PeakSortedListLayout() {}

    static void render(GuiGraphics graphics, Font font, PeakSortedList list,
                       int contentX, int contentY, int contentWidth, int contentHeight,
                       int absX, int absY, float absScale, float scissorUnitFactor, String documentId, int depth) {
        boolean column = list.getDirection() == PeakLayoutDirection.COLUMN;
        List<PeakElement<?>> items = new ArrayList<>();
        for (PeakElement<?> child : list.getChildren()) {
            if (child.isHidden() || PeakUiRenderer.isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            items.add(child);
        }

        List<PeakElement<?>> ordered = list.sorted(items);
        int visibleCount = list.getMaxVisible() > 0 ? Math.min(list.getMaxVisible(), ordered.size()) : ordered.size();

        double now = PeakClientTickCounter.exact();
        PeakMotionTracker<PeakElement<?>> motion = list.motion();
        List<PeakMotionTracker.Departed<PeakElement<?>>> departed = motion.beginFrame(ordered);

        int[] lengths = new int[ordered.size()];
        int visibleTotal = 0;
        for (int i = 0; i < ordered.size(); i++) {
            PeakPixelSize footprint = PeakUiRenderer.footprint(font, ordered.get(i));
            PeakInsets margin = ordered.get(i).getStyle().getMargin();
            lengths[i] = column ? footprint.height() + margin.getTop() + margin.getBottom()
                    : footprint.width() + margin.getLeft() + margin.getRight();
            if (i < visibleCount) visibleTotal += lengths[i];
        }
        visibleTotal += Math.max(0, visibleCount - 1) * list.getGap();

        int available = column ? contentHeight : contentWidth;
        int crossSize = column ? contentWidth : contentHeight;
        float cursor = switch (list.getMainAxisAlign()) {
            case END -> available - visibleTotal;
            case CENTER, SPACE_BETWEEN, SPACE_AROUND -> (available - visibleTotal) / 2f;
            case START -> 0f;
        };
        double appearTicks = list.getEnterAnimation() == PeakPopupAnimation.NONE ? 0.0 : list.getEnterDurationTicks();

        boolean exits = list.getExitAnimation() != PeakPopupAnimation.NONE && list.getExitDurationTicks() > 0;
        List<PeakSortedList.Ghost> ghosts = list.ghosts();
        if (exits) {
            for (PeakMotionTracker.Departed<PeakElement<?>> gone : departed) {
                ghosts.add(new PeakSortedList.Ghost(gone.item(), gone.position(), now));
            }
        } else {
            ghosts.clear();
        }

        List<PeakElement<?>> shown = new ArrayList<>();
        PeakMotionTracker.Frame[] frames = new PeakMotionTracker.Frame[ordered.size()];
        for (int i = 0; i < ordered.size(); i++) {
            PeakElement<?> child = ordered.get(i);
            boolean visible = i < visibleCount;
            frames[i] = motion.track(child, cursor, visible, now, list.getMoveTicks(), appearTicks);
            cursor += lengths[i] + list.getGap();
            if (visible) shown.add(child);
            else if (exits && frames[i].departed()) ghosts.add(new PeakSortedList.Ghost(child, frames[i].position(), now));
        }

        if (!ghosts.isEmpty()) {
            double duration = list.getExitDurationTicks();
            ghosts.removeIf(ghost -> now - ghost.start() >= duration || containsIdentity(shown, ghost.element()));
            for (PeakSortedList.Ghost ghost : ghosts) {
                float progress = (float) Math.min(1.0, (now - ghost.start()) / duration);
                PeakAnimState state = PeakTransitions.sample(list.getExitAnimation(), 1f - progress, list.getSlideDistance());
                drawItem(graphics, font, list, ghost.element(), ghost.position(), state, column, crossSize, contentX, contentY,
                        absX, absY, absScale, scissorUnitFactor, documentId, depth);
            }
        }

        for (int i = 0; i < ordered.size(); i++) {
            if (i >= visibleCount) continue;
            PeakMotionTracker.Frame frame = frames[i];
            PeakAnimState state = frame.appear() >= 0.999f
                    ? PeakAnimState.VISIBLE
                    : PeakTransitions.sample(list.getEnterAnimation(), frame.appear(), list.getSlideDistance());
            drawItem(graphics, font, list, ordered.get(i), frame.position(), state, column, crossSize, contentX, contentY,
                    absX, absY, absScale, scissorUnitFactor, documentId, depth);
        }
    }

    private static boolean containsIdentity(List<PeakElement<?>> items, PeakElement<?> element) {
        for (PeakElement<?> item : items) {
            if (item == element) return true;
        }
        return false;
    }

    private static void drawItem(GuiGraphics graphics, Font font, PeakSortedList list, PeakElement<?> child, float position,
                                 PeakAnimState state, boolean column, int crossSize, int contentX, int contentY,
                                 int absX, int absY, float absScale, float scissorUnitFactor, String documentId, int depth) {
        if (state.scale() <= 0.001f || state.alpha() <= 0.001f) return;

        PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
        PeakInsets margin = child.getStyle().getMargin();
        int crossAvailable = crossSize - (column ? margin.getLeft() + margin.getRight() : margin.getTop() + margin.getBottom());
        int childCross = column ? footprint.width() : footprint.height();
        int crossOffset = switch (list.getAlignChildren()) {
            case CENTER -> Math.max(0, (crossAvailable - childCross) / 2);
            case END -> Math.max(0, crossAvailable - childCross);
            case START, AUTO -> 0;
        };
        int main = Math.round(position);
        int x = (column ? contentX + margin.getLeft() + crossOffset : contentX + main + margin.getLeft())
                + child.getPosition().getOffsetX();
        int y = (column ? contentY + main + margin.getTop() : contentY + margin.getTop() + crossOffset)
                + child.getPosition().getOffsetY();

        PeakGroupTransform.paint(graphics, x + footprint.width() / 2f, y + footprint.height() / 2f,
                PeakEffectTransform.of(state),
                    new PeakGroupTransform.ClipSpace(footprint.width(), footprint.height(), absX, absY, absScale, scissorUnitFactor),
                () -> PeakUiRenderer.renderChild(graphics, font, child, x, y, absX, absY, absScale,
                        scissorUnitFactor, documentId, depth + 1));
    }
}
