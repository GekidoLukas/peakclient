package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakImmediateRender;
import net.gekidolukas.peakclient.render.PeakItemElementRenderer;
import net.gekidolukas.peakclient.render.PeakLabelRenderer;
import net.gekidolukas.peakclient.render.PeakTextResolver;
import net.gekidolukas.peakclient.render.PeakTooltipLabelRenderer;
import net.gekidolukas.peakclient.render.PeakUiRenderer;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakItemSlot;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPoint;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.gekidolukas.peakclient.ui.PeakTooltipStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class PeakMenuTooltips {
    private PeakMenuTooltips() {}

    static PeakPoint position(int mouseX, int mouseY, int boxWidth, int boxHeight, int screenWidth, int screenHeight) {
        int x = mouseX + 12;
        if (x + boxWidth > screenWidth) x = Math.max(2, mouseX - 12 - boxWidth);
        int y = mouseY - 12;
        if (y + boxHeight > screenHeight) y = screenHeight - boxHeight - 2;
        y = Math.max(2, y);
        return new PeakPoint(x, y);
    }

    static void renderHovered(GuiGraphics graphics, Minecraft minecraft, PeakMenuSettings settings, PeakElement<?> element,
                              int mouseX, int mouseY, int screenWidth, int screenHeight) {
        PeakInteraction interaction = element.getInteraction();
        boolean ownTooltip = interaction != null && interaction.hasTooltip();
        if (element instanceof PeakItemSlot itemSlot && itemSlot.isItemTooltip() && !ownTooltip) {
            renderItem(graphics, minecraft, settings, itemSlot, mouseX, mouseY, screenWidth, screenHeight);
        } else {
            render(graphics, minecraft, settings, interaction, mouseX, mouseY, screenWidth, screenHeight);
        }
    }

    static void renderItem(GuiGraphics graphics, Minecraft minecraft, PeakMenuSettings settings, PeakItemSlot slot,
                           int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (minecraft.player == null) return;
        ItemStack stack = PeakItemElementRenderer.resolveItemStack(minecraft, slot.getSource());
        if (stack == null || stack.isEmpty()) return;

        List<PeakStyledText> lines = new ArrayList<>();
        for (Component component : Screen.getTooltipFromItem(minecraft, stack)) lines.add(PeakStyledText.of(component));
        if (lines.isEmpty()) return;

        Font font = minecraft.font;
        PeakTooltipStyle style = PeakTheme.tooltip(settings, slot.getInteraction());
        int textWidth = 0;
        for (PeakStyledText line : lines) textWidth = Math.max(textWidth, line.width(font));
        int contentHeight = lines.size() * font.lineHeight + (lines.size() > 1 ? 2 : 0);
        int inset = style.getPadding() + 1;
        PeakPoint tooltipPosition = position(mouseX, mouseY, textWidth + inset * 2, contentHeight + inset * 2, screenWidth, screenHeight);
        int x = tooltipPosition.x() + inset;
        int y = tooltipPosition.y() + inset;

        PeakTooltipLabelRenderer.drawTooltipBox(graphics, x, y, textWidth, contentHeight, style);
        for (int i = 0; i < lines.size(); i++) {
            int lineY = y + i * font.lineHeight + (i > 0 ? 2 : 0);
            PeakImmediateRender.text(graphics, font, lines.get(i), x, lineY, style.getTextColor(), style.hasShadow());
        }
    }

    static void render(GuiGraphics graphics, Minecraft minecraft, PeakMenuSettings settings, PeakInteraction interaction,
                       int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (interaction == null) return;
        Font font = minecraft.font;

        PeakElement<?> custom = interaction.getTooltipElement();
        if (custom != null) {
            PeakPixelSize size = PeakUiRenderer.measureFloating(font, custom);
            PeakPoint tooltipPosition = position(mouseX, mouseY, size.width(), size.height(), screenWidth, screenHeight);
            PeakUiRenderer.renderFloating(graphics, minecraft, custom, tooltipPosition.x(), tooltipPosition.y());
            return;
        }

        PeakStyledText text = PeakTextResolver.resolved(interaction, interaction.getTooltip());
        if (text.isEmpty()) return;
        PeakTooltipStyle style = PeakTheme.tooltip(settings, interaction);
        List<PeakStyledText> lines = PeakLabelRenderer.layoutLines(font, text, style.getMaxWidth());
        int textWidth = 0;
        for (PeakStyledText line : lines) textWidth = Math.max(textWidth, line.width(font));
        int contentHeight = lines.size() * font.lineHeight;
        int inset = style.getPadding() + 1;
        PeakPoint tooltipPosition = position(mouseX, mouseY, textWidth + inset * 2, contentHeight + inset * 2, screenWidth, screenHeight);
        int x = tooltipPosition.x() + inset;
        int y = tooltipPosition.y() + inset;

        PeakTooltipLabelRenderer.drawTooltipBox(graphics, x, y, textWidth, contentHeight, style);
        for (int i = 0; i < lines.size(); i++) {
            PeakImmediateRender.text(graphics, font, lines.get(i), x, y + i * font.lineHeight, style.getTextColor(), style.hasShadow());
        }
    }
}
