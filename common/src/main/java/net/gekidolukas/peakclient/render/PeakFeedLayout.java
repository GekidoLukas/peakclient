package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakAnimState;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakFeed;
import net.gekidolukas.peakclient.ui.PeakFeedModel;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakMotionTracker;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakTransitions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PeakFeedLayout {
    private PeakFeedLayout() {}

    static void render(GuiGraphics graphics, Font font, PeakFeed feed,
                       int contentX, int contentY, int contentWidth, int contentHeight,
                       int absX, int absY, float absScale, float scissorUnitFactor, String documentId, int depth) {
        boolean column = feed.getDirection() == PeakLayoutDirection.COLUMN;
        List<PeakElement<?>> items = new ArrayList<>();
        for (PeakElement<?> child : feed.getChildren()) {
            if (child.isHidden() || PeakUiRenderer.isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            items.add(child);
        }

        double now = PeakClientTickCounter.exact();
        List<PeakFeedModel.Entry<PeakElement<?>>> entries = feed.model().update(now, items, feed.modelConfig());

        List<PeakFeedModel.Entry<PeakElement<?>>> shown = new ArrayList<>();
        for (PeakFeedModel.Entry<PeakElement<?>> entry : entries) {
            if (entry.expired()) {
                if (feed.isAutoRemove()) PeakUiRenderer.requestRemoval(entry.item().getId());
            } else {
                shown.add(entry);
            }
        }
        if (feed.isNewestFirst()) Collections.reverse(shown);

        List<PeakElement<?>> shownItems = new ArrayList<>(shown.size());
        for (PeakFeedModel.Entry<PeakElement<?>> entry : shown) shownItems.add(entry.item());
        PeakMotionTracker<PeakElement<?>> motion = feed.motion();
        motion.beginFrame(shownItems);

        int[] lengths = new int[shown.size()];
        int total = 0;
        for (int i = 0; i < shown.size(); i++) {
            PeakPixelSize footprint = PeakUiRenderer.footprint(font, shown.get(i).item());
            PeakInsets margin = shown.get(i).item().getStyle().getMargin();
            lengths[i] = column ? footprint.height() + margin.getTop() + margin.getBottom()
                    : footprint.width() + margin.getLeft() + margin.getRight();
            total += lengths[i];
        }
        total += Math.max(0, shown.size() - 1) * feed.getGap();

        int available = column ? contentHeight : contentWidth;
        int crossSize = column ? contentWidth : contentHeight;
        float cursor = switch (feed.getMainAxisAlign()) {
            case END -> available - total;
            case CENTER, SPACE_BETWEEN, SPACE_AROUND -> (available - total) / 2f;
            case START -> 0f;
        };

        for (int i = 0; i < shown.size(); i++) {
            PeakFeedModel.Entry<PeakElement<?>> entry = shown.get(i);
            PeakElement<?> child = entry.item();
            PeakMotionTracker.Frame frame = motion.track(child, cursor, true, now, feed.getMoveTicks(), 0.0);
            cursor += lengths[i] + feed.getGap();

            PeakAnimState state = switch (entry.phase()) {
                case ENTER -> PeakTransitions.sample(feed.getEnterAnimation(), entry.progress(), feed.getSlideDistance());
                case HOLD -> PeakAnimState.VISIBLE;
                case EXIT -> PeakTransitions.sample(feed.getExitAnimation(), 1f - entry.progress(), feed.getSlideDistance());
            };
            if (state.hidden() || state.scale() <= 0.001f || state.alpha() <= 0.001f) continue;

            PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
            PeakInsets margin = child.getStyle().getMargin();
            int crossAvailable = crossSize - (column ? margin.getLeft() + margin.getRight() : margin.getTop() + margin.getBottom());
            int childCross = column ? footprint.width() : footprint.height();
            int crossOffset = switch (feed.getAlignChildren()) {
                case CENTER -> Math.max(0, (crossAvailable - childCross) / 2);
                case END -> Math.max(0, crossAvailable - childCross);
                case START, AUTO -> 0;
            };
            int main = Math.round(frame.position());
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
}
