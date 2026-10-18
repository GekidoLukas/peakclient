package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakLabelText;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;

public final class PeakLabelRenderer implements PeakElementRenderer<PeakLabel> {

    @Override
    public void renderContent(PeakRenderContext ctx, PeakLabel label, int width, int height) {
        String target = PeakPlaceholderResolver.resolve(resolveLabelText(label));
        double now = PeakClientTickCounter.exact();
        String shown = label.animation().displayText(target, label.getCountUpTicks(), now);
        List<String> lines = layoutLines(ctx.font(), shown, label.getWrapWidth());

        int budget = label.animation().revealBudget(target, label.getTypewriterCharsPerSecond(), label.getCountUpTicks() > 0, now);
        List<String> visible = budget < 0 ? lines : PeakLabelText.truncateLines(lines, budget);

        int y = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = visible.get(i);
            if (!line.isEmpty()) {
                if (budget < 0) {
                    PeakImmediateRender.text(ctx.graphics(), ctx.font(), line, 0, y, label.getColor(), label.hasShadow());
                } else {
                    PeakImmediateRender.text(ctx.graphics(), ctx.font(), line, 0, y, label.getColor(), label.hasShadow(),
                            0f, Math.max(1, ctx.font().width(lines.get(i))));
                }
            }
            y += ctx.font().lineHeight + label.getLineSpacing();
        }
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakLabel label) {
        String text = PeakPlaceholderResolver.resolve(resolveLabelText(label));
        List<String> lines = layoutLines(font, text, label.getWrapWidth());

        int width = 0;
        for (String line : lines) width = Math.max(width, font.width(line));
        int height = lines.size() * font.lineHeight + Math.max(0, lines.size() - 1) * label.getLineSpacing();
        return new PeakPixelSize(width, height);
    }

    public static String resolvedText(PeakLabel label) {
        return PeakPlaceholderResolver.resolve(resolveLabelText(label));
    }

    private static String resolveLabelText(PeakLabel label) {
        if (label.getTranslationKey() != null) {
            Object[] args = label.getTranslationArgs().toArray();
            return I18n.get(label.getTranslationKey(), args);
        }
        return label.getText();
    }

    public static List<String> layoutLines(Font font, String text, int wrapWidth) {
        List<String> result = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            if (wrapWidth > 0) {
                result.addAll(wrapParagraph(font, paragraph, wrapWidth));
            } else {
                result.add(paragraph);
            }
        }
        if (result.isEmpty()) result.add("");
        return result;
    }

    private static List<String> wrapParagraph(Font font, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) { lines.add(""); return lines; }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        String activeFormatting = "";

        for (String word : words) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (font.width(candidate) > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                activeFormatting = trailingFormatting(currentLine.toString(), activeFormatting);
                currentLine = new StringBuilder(activeFormatting).append(word);
            } else {
                currentLine = new StringBuilder(candidate);
            }
        }
        lines.add(currentLine.toString());
        return lines;
    }

    private static String trailingFormatting(String text, String fallback) {
        String lastColor = fallback;
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                char code = text.charAt(i + 1);
                if (code == 'x' && i + 13 < text.length() && isHexSequenceAt(text, i)) {
                    lastColor = text.substring(i, i + 14);
                    i += 14;
                    continue;
                }
                if ("0123456789abcdef".indexOf(Character.toLowerCase(code)) >= 0) {
                    lastColor = "§" + code;
                    i += 2;
                    continue;
                }
                if (code == 'r') {
                    lastColor = "";
                    i += 2;
                    continue;
                }
                i += 2;
                continue;
            }
            i++;
        }
        return lastColor;
    }

    private static boolean isHexSequenceAt(String text, int start) {
        if (text.charAt(start + 1) != 'x') return false;
        for (int digit = 0; digit < 6; digit++) {
            int separatorIndex = start + 2 + digit * 2;
            if (separatorIndex + 1 >= text.length() || text.charAt(separatorIndex) != '§') return false;
        }
        return true;
    }
}
