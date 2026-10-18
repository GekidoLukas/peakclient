package net.gekidolukas.peakclient.render;

import java.util.Locale;
import net.gekidolukas.peakclient.ui.PeakBarChart;
import net.gekidolukas.peakclient.ui.PeakChartEntry;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakValueLabelPosition;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class PeakBarChartRenderer implements PeakElementRenderer<PeakBarChart> {

    @Override
    public void renderContent(PeakRenderContext ctx, PeakBarChart chart, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        List<PeakChartEntry> entries = chart.getEntries();
        if (entries.isEmpty()) return;

        PeakPaintRenderer.fill(graphics, chart.getBackgroundColor(), 0, 0, width, height);

        double max = chart.getMaxValue() > 0 ? chart.getMaxValue() : entries.stream().mapToDouble(PeakChartEntry::value).max().orElse(1);
        if (max <= 0) max = 1;

        int categoryLabelHeight = chart.isShowCategoryLabels() ? font.lineHeight + 2 : 0;

        int outsideValueLabelHeight = (chart.isShowValueLabels() && chart.getValueLabelPosition() == PeakValueLabelPosition.OUTSIDE)
                ? font.lineHeight + 2 : 0;

        int plotTop = chart.getTopPadding() + outsideValueLabelHeight;
        int plotBottom = height - categoryLabelHeight;
        int plotHeight = Math.max(1, plotBottom - plotTop);

        int spacing = chart.getBarSpacing();
        int totalSpacing = spacing * (entries.size() + 1);
        int barWidth = Math.max(1, (width - totalSpacing) / entries.size());

        int x = spacing;
        for (PeakChartEntry entry : entries) {
            double ratio = Math.max(0, Math.min(1, entry.value() / max));
            int barHeight = (int) Math.round(plotHeight * ratio);
            int barY = plotBottom - barHeight;

            PeakPaintRenderer.fill(graphics, entry.color(), x, barY, x + barWidth, plotBottom);

            if (chart.isShowValueLabels()) {
                String valueText = trimNumber(entry.value());
                int textWidth = font.width(valueText);
                int labelX = x + (barWidth - textWidth) / 2;
                int labelY = chart.getValueLabelPosition() == PeakValueLabelPosition.OUTSIDE
                        ? Math.max(0, barY - font.lineHeight - 1)
                        : Math.min(barY + 2, plotBottom - font.lineHeight);
                PeakImmediateRender.text(graphics, font, valueText, labelX, labelY, chart.getValueLabelColor(), true);
            }
            if (chart.isShowCategoryLabels() && entry.label() != null) {
                int textWidth = font.width(entry.label());
                PeakImmediateRender.text(graphics, font, entry.label(), x + (barWidth - textWidth) / 2, plotBottom + 2, chart.getCategoryLabelColor(), true);
            }

            x += barWidth + spacing;
        }
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakBarChart chart) {
        return PeakPixelSize.ZERO;
    }

    private static String trimNumber(double value) {
        if (value == Math.rint(value)) return String.valueOf((long) value);
        return String.format(Locale.ROOT, "%.1f", value);
    }
}