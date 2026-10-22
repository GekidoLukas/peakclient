package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLabelText;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakTextWrap;
import net.minecraft.client.gui.Font;

import java.util.List;

public final class PeakLabelRenderer implements PeakElementRenderer<PeakLabel> {

    @Override
    public void renderContent(PeakRenderContext ctx, PeakLabel label, int width, int height) {
        PeakStyledText target = resolveLabelText(label);
        double now = PeakClientTickCounter.exact();
        PeakStyledText shown = label.animation().displayText(target, label.getCountUpTicks(), now);
        List<PeakStyledText> lines = layoutLines(ctx.font(), shown, label.getWrapWidth());

        int budget = label.animation().revealBudget(target, label.getTypewriterCharsPerSecond(), label.getCountUpTicks() > 0, now);
        List<PeakStyledText> visible = budget < 0 ? lines : PeakLabelText.truncateLines(lines, budget);

        int y = 0;
        for (int i = 0; i < lines.size(); i++) {
            PeakStyledText line = visible.get(i);
            if (!line.isEmpty()) {
                if (budget < 0) {
                    PeakImmediateRender.text(ctx.graphics(), ctx.font(), line, 0, y, label.getColor(), label.hasShadow());
                } else {
                    PeakImmediateRender.text(ctx.graphics(), ctx.font(), line, 0, y, label.getColor(), label.hasShadow(),
                            0f, Math.max(1, lines.get(i).width(ctx.font())));
                }
            }
            y += ctx.font().lineHeight + label.getLineSpacing();
        }
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakLabel label) {
        List<PeakStyledText> lines = layoutLines(font, resolveLabelText(label), label.getWrapWidth());

        int width = 0;
        for (PeakStyledText line : lines) width = Math.max(width, line.width(font));
        int height = lines.size() * font.lineHeight + Math.max(0, lines.size() - 1) * label.getLineSpacing();
        return new PeakPixelSize(width, height);
    }

    public static String resolvedText(PeakLabel label) {
        return resolveLabelText(label).plain();
    }

    private static PeakStyledText resolveLabelText(PeakLabel label) {
        return PeakTextResolver.resolved(label, label.getText());
    }

    public static List<PeakStyledText> layoutLines(Font font, PeakStyledText text, int wrapWidth) {
        return PeakTextWrap.layout(line -> line.width(font), text, wrapWidth);
    }
}
