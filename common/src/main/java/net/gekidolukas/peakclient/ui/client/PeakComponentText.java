package net.gekidolukas.peakclient.ui.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;
import java.util.Optional;

public final class PeakComponentText {
    private PeakComponentText() {}

    public static String toLegacy(Component component) {
        StringBuilder out = new StringBuilder();
        component.visit((style, content) -> {
            if (content.isEmpty()) return Optional.empty();
            if (out.length() > 0) out.append("§r");
            appendStyle(out, style);
            out.append(content);
            return Optional.empty();
        }, Style.EMPTY);
        return out.toString();
    }

    static void appendStyle(StringBuilder out, Style style) {
        TextColor color = style.getColor();
        if (color != null) appendColor(out, color.getValue());
        if (style.isBold()) out.append("§l");
        if (style.isItalic()) out.append("§o");
        if (style.isUnderlined()) out.append("§n");
        if (style.isStrikethrough()) out.append("§m");
        if (style.isObfuscated()) out.append("§k");
    }

    static void appendColor(StringBuilder out, int rgb) {
        int value = rgb & 0xFFFFFF;
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.isColor() && formatting.getColor() != null && formatting.getColor() == value) {
                out.append('§').append(formatting.getChar());
                return;
            }
        }
        out.append("§x");
        String hex = String.format(Locale.ROOT, "%06x", value);
        for (int i = 0; i < hex.length(); i++) out.append('§').append(hex.charAt(i));
    }
}
