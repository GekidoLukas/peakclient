package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakConstraints;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakFlow;
import net.gekidolukas.peakclient.ui.PeakFlowLines;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakSpacer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

final class PeakFlowLayout {
    private record Prepared(List<PeakElement<?>> children, List<PeakInsets> margins, List<PeakFlowLines.Item> items) {}

    private PeakFlowLayout() {}

    static PeakPixelSize measure(Font font, PeakFlow flow) {
        boolean row = flow.getDirection() == PeakLayoutDirection.ROW;
        return measureWithLimit(font, flow, measureLimit(flow, row));
    }

    static PeakPixelSize measureWithLimit(Font font, PeakFlow flow, int contentLimit) {
        boolean row = flow.getDirection() == PeakLayoutDirection.ROW;
        Prepared prepared = PeakUiRenderer.withBases(row ? contentLimit : -1, row ? -1 : contentLimit,
                () -> prepare(font, flow, row));
        List<PeakFlowLines.Line> lines = PeakFlowLines.wrap(prepared.items(), contentLimit, flow.getGap(), flow.getMaxLines());

        int main = 0, cross = 0;
        for (PeakFlowLines.Line line : lines) {
            main = Math.max(main, line.main());
            cross += line.cross();
        }
        cross += Math.max(0, lines.size() - 1) * flow.getLineGap();

        PeakInsets padding = flow.getStyle().getPadding();
        int width = row ? main : cross;
        int height = row ? cross : main;
        return new PeakPixelSize(width + padding.getLeft() + padding.getRight(), height + padding.getTop() + padding.getBottom());
    }

    static void render(GuiGraphics graphics, Font font, PeakFlow flow, int flowX, int flowY, int contentWidth, int contentHeight,
                       int absX, int absY, float absScale, float scissorUnitFactor, String documentId, int depth) {
        boolean row = flow.getDirection() == PeakLayoutDirection.ROW;
        Prepared prepared = prepare(font, flow, row);
        int limit = row ? contentWidth : contentHeight;
        int crossSize = row ? contentHeight : contentWidth;
        PeakFlowLines.Result result = PeakFlowLines.layout(prepared.items(), limit, limit, crossSize, flow.getGap(),
                flow.getLineGap(), flow.getMaxLines(), flow.getMainAxisAlign(), flow.getAlignChildren(), flow.getLineAlign(),
                flow.isReverse());

        for (PeakFlowLines.Placement placement : result.placements()) {
            PeakElement<?> child = prepared.children().get(placement.index());
            PeakInsets margin = prepared.margins().get(placement.index());
            int x = flowX + (row ? placement.mainOffset() : placement.crossOffset()) + margin.getLeft()
                    + child.getPosition().getOffsetX();
            int y = flowY + (row ? placement.crossOffset() : placement.mainOffset()) + margin.getTop()
                    + child.getPosition().getOffsetY();
            PeakUiRenderer.renderChild(graphics, font, child, x, y, absX, absY, absScale, scissorUnitFactor, documentId, depth + 1);
        }
    }

    private static Prepared prepare(Font font, PeakFlow flow, boolean row) {
        List<PeakElement<?>> children = new ArrayList<>();
        List<PeakInsets> margins = new ArrayList<>();
        List<PeakFlowLines.Item> items = new ArrayList<>();
        boolean pendingBreak = false;
        for (PeakElement<?> child : flow.getChildren()) {
            if (PeakUiRenderer.isCollapsed(child) || child.getPositionMode() == PeakPositionMode.ABSOLUTE) continue;
            if (child instanceof PeakSpacer spacer && spacer.isLineBreak()) {
                pendingBreak = true;
                continue;
            }
            PeakPixelSize footprint = PeakUiRenderer.footprint(font, child);
            PeakInsets margin = child.getStyle().getMargin();
            int width = footprint.width() + margin.getLeft() + margin.getRight();
            int height = footprint.height() + margin.getTop() + margin.getBottom();
            children.add(child);
            margins.add(margin);
            items.add(new PeakFlowLines.Item(row ? width : height, row ? height : width, pendingBreak));
            pendingBreak = false;
        }
        return new Prepared(children, margins, items);
    }

    private static int measureLimit(PeakFlow flow, boolean row) {
        int baseMain = row ? PeakUiRenderer.parentBaseWidth() : PeakUiRenderer.parentBaseHeight();
        int fixed = PeakUiRenderer.resolveDimension(row ? flow.getSize().getWidth() : flow.getSize().getHeight(), baseMain);
        PeakConstraints constraints = flow.getConstraints();
        int maxConstraint = PeakUiRenderer.resolveDimension(row ? constraints.getMaxWidth() : constraints.getMaxHeight(), baseMain);
        PeakInsets margin = flow.getStyle().getMargin();
        int marginMain = row ? margin.getLeft() + margin.getRight() : margin.getTop() + margin.getBottom();

        int outer;
        if (fixed >= 0) outer = fixed;
        else if (flow.getWrapSize() > 0) outer = flow.getWrapSize();
        else if (maxConstraint >= 0) outer = maxConstraint;
        else if (baseMain >= 0) outer = baseMain - marginMain;
        else return -1;

        PeakInsets padding = flow.getStyle().getPadding();
        int paddingMain = row ? padding.getLeft() + padding.getRight() : padding.getTop() + padding.getBottom();
        return Math.max(0, outer - paddingMain);
    }
}
