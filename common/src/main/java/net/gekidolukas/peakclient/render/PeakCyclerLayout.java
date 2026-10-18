package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakAnimState;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakCycler;
import net.gekidolukas.peakclient.ui.PeakCyclerTimeline;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakTransitions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

final class PeakCyclerLayout {
    private PeakCyclerLayout() {}

    static void render(GuiGraphics graphics, Font font, PeakCycler cycler,
                       int contentX, int contentY, int contentWidth, int contentHeight,
                       int absX, int absY, float absScale, float scissorUnitFactor, String documentId, int depth) {
        List<PeakElement<?>> items = new ArrayList<>();
        for (PeakElement<?> child : cycler.getChildren()) {
            if (child.isHidden() || PeakUiRenderer.isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            items.add(child);
        }

        PeakCyclerTimeline<PeakElement<?>> timeline = cycler.timeline();
        if (cycler.takeNextRequest()) timeline.requestNext();
        int requestedIndex = cycler.takeIndexRequest();
        if (requestedIndex >= 0 && requestedIndex < items.size()) timeline.requestShow(items.get(requestedIndex));

        List<PeakCyclerTimeline.Visible<PeakElement<?>>> visible =
                timeline.update(PeakClientTickCounter.exact(), items, cycler.timelineConfig());

        for (PeakCyclerTimeline.Visible<PeakElement<?>> entry : visible) {
            PeakAnimState state = animationState(cycler, entry);
            if (state.hidden() || state.scale() <= 0.001f || state.alpha() <= 0.001f) continue;

            PeakElement<?> child = entry.item();
            PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
            PeakInsets margin = child.getStyle().getMargin();
            int availableWidth = contentWidth - margin.getLeft() - margin.getRight();
            int availableHeight = contentHeight - margin.getTop() - margin.getBottom();
            int offsetX = switch (cycler.getAlignChildren()) {
                case CENTER -> Math.max(0, (availableWidth - footprint.width()) / 2);
                case END -> Math.max(0, availableWidth - footprint.width());
                case START, AUTO -> 0;
            };
            int offsetY = switch (cycler.getMainAxisAlign()) {
                case CENTER, SPACE_BETWEEN, SPACE_AROUND -> Math.max(0, (availableHeight - footprint.height()) / 2);
                case END -> Math.max(0, availableHeight - footprint.height());
                case START -> 0;
            };
            int x = contentX + margin.getLeft() + offsetX + child.getPosition().getOffsetX();
            int y = contentY + margin.getTop() + offsetY + child.getPosition().getOffsetY();

            PeakGroupTransform.paint(graphics, x + footprint.width() / 2f, y + footprint.height() / 2f,
                    PeakEffectTransform.of(state),
                    new PeakGroupTransform.ClipSpace(footprint.width(), footprint.height(), absX, absY, absScale, scissorUnitFactor),
                    () -> PeakUiRenderer.renderChild(graphics, font, child, x, y, absX, absY, absScale,
                            scissorUnitFactor, documentId, depth + 1));
        }
    }

    private static PeakAnimState animationState(PeakCycler cycler, PeakCyclerTimeline.Visible<PeakElement<?>> entry) {
        return switch (entry.phase()) {
            case ENTER -> PeakTransitions.sample(cycler.getEnterAnimation(), entry.progress(), cycler.getSlideDistance());
            case HOLD -> PeakAnimState.VISIBLE;
            case EXIT -> PeakTransitions.sample(cycler.getExitAnimation(), 1f - entry.progress(), cycler.getSlideDistance());
        };
    }
}
