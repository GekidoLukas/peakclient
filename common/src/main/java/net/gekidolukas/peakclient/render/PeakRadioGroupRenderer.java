package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakLayoutDirection;
import net.gekidolukas.peakclient.ui.PeakOption;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakRadioGroup;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakRadioLayout;
import net.gekidolukas.peakclient.ui.PeakRect;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class PeakRadioGroupRenderer implements PeakElementRenderer<PeakRadioGroup> {
    public static PeakRadioLayout layout(Font font, PeakRadioGroup group) {
        List<PeakOption> options = group.getOptions();
        int[] widths = new int[options.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = PeakTextResolver.resolved(options.get(i), options.get(i).getText()).width(font);
        }
        return PeakRadioLayout.of(widths, font.lineHeight, group.getDirection() == PeakLayoutDirection.ROW, group.getGap());
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakRadioGroup group) {
        PeakRadioLayout layout = layout(font, group);
        return new PeakPixelSize(layout.width(), layout.height());
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakRadioGroup group, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        boolean enabled = group.getInteraction() == null || group.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(group);
        boolean ring = focused && PeakMenuInteractionState.focusVisible(group);
        boolean invalid = PeakMenuInteractionState.isInvalid(group);

        PeakRadioLayout layout = layout(font, group);
        List<PeakOption> options = group.getOptions();
        int selected = group.selectedIndex();
        int cursor = selected >= 0 ? selected : group.firstEnabledIndex();

        for (int i = 0; i < options.size(); i++) {
            PeakOption option = options.get(i);
            PeakRect cell = layout.cell(i);
            boolean optionEnabled = enabled && option.isEnabled();
            int circleY = cell.y() + (layout.rowHeight() - PeakRadioLayout.CIRCLE) / 2;

            PeakPaint border = PeakPaint.solid(PeakInputColors.BORDER);
            if (ring && i == cursor) border = PeakMenuInteractionState.focusColor(group);
            else if (invalid) border = PeakMenuInteractionState.invalidColor(group);
            else if (!optionEnabled) border = PeakPaint.solid(0xFF505050);

            disc(graphics, cell.x(), circleY, PeakRadioLayout.CIRCLE, border);
            disc(graphics, cell.x() + 1, circleY + 1, PeakRadioLayout.CIRCLE - 2, PeakPaint.solid(PeakInputColors.FIELD_BACKGROUND));
            if (i == selected) {
                disc(graphics, cell.x() + 3, circleY + 3, PeakRadioLayout.CIRCLE - 6, PeakPaint.solid(optionEnabled ? 0xFF55FF55 : 0xFF557755));
            }

            PeakStyledText text = PeakTextResolver.resolved(option, option.getText());
            if (!text.isEmpty()) {
                int textY = cell.y() + Math.max(0, (layout.rowHeight() - font.lineHeight) / 2) + 1;
                PeakImmediateRender.text(graphics, font, text, cell.x() + PeakRadioLayout.CIRCLE + PeakRadioLayout.TEXT_GAP,
                        textY, optionEnabled ? group.getColor() : PeakPaint.solid(PeakInputColors.TEXT_DISABLED), true);
            }
        }
    }

    private static void disc(GuiGraphics graphics, int x, int y, int diameter, PeakPaint paint) {
        double radius = diameter / 2.0;
        for (int row = 0; row < diameter; row++) {
            double dy = row + 0.5 - radius;
            double halfWidth = Math.sqrt(Math.max(0.0, radius * radius - dy * dy));
            int x1 = (int) Math.round(radius - halfWidth);
            int x2 = (int) Math.round(radius + halfWidth);
            if (x2 > x1) {
                PeakPaintRenderer.fill(graphics, paint, x + x1, y + row, x + x2, y + row + 1, x, y, x + diameter, y + diameter);
            }
        }
    }
}
