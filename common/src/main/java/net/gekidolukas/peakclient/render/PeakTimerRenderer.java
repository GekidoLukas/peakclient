package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.PeakTimer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;

import java.util.Locale;

public final class PeakTimerRenderer implements PeakElementRenderer<PeakTimer> {
    @Override
    public void renderContent(PeakRenderContext ctx, PeakTimer timer, int width, int height) {
        PeakStyledText text = formatTimer(timer);
        PeakImmediateRender.text(ctx.graphics(),ctx.font(), text, 0, 0, timer.getColor(), timer.hasShadow());
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakTimer timer) {
        return new PeakPixelSize(formatTimer(timer).width(font), font.lineHeight);
    }

    private static PeakStyledText formatTimer(PeakTimer timer) {
        long ticks = Math.max(0, timer.currentValue());
        long totalSeconds = ticks / 20;

        String core;
        String autoSuffix = "";
        switch (timer.getFormat()) {
            case TICKS -> { core = Long.toString(ticks); autoSuffix = suffixFor("ticks"); }
            case SECONDS -> { core = Long.toString(totalSeconds); autoSuffix = suffixFor("seconds"); }
            case MINUTES -> { core = Long.toString(totalSeconds / 60); autoSuffix = suffixFor("minutes"); }
            case HOURS -> { core = Long.toString(totalSeconds / 3600); autoSuffix = suffixFor("hours"); }
            case DAYS -> { core = Long.toString(totalSeconds / 86400); autoSuffix = suffixFor("days"); }
            case MM_SS -> core = String.format(Locale.ROOT, "%02d:%02d", totalSeconds / 60, totalSeconds % 60);
            case HH_MM_SS -> core = String.format(Locale.ROOT, "%02d:%02d:%02d",
                    totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
            case DD_HH_MM_SS -> core = String.format(Locale.ROOT, "%d:%02d:%02d:%02d",
                    totalSeconds / 86400, (totalSeconds % 86400) / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
            case MM_SS_U -> core = minutesSecondsUnitsAlways(totalSeconds);
            case HH_MM_SS_U -> core = hoursMinutesSecondsUnitsAlways(totalSeconds);
            case DD_HH_MM_SS_U -> core = daysHoursMinutesSecondsUnitsAlways(totalSeconds);
            case DYNAMIC -> core = dynamicColonFormat(totalSeconds);
            case DYNAMIC_U -> core = dynamicUnitsFormat(totalSeconds);
            default -> core = Long.toString(ticks);
        }

        PeakStyledText prefix = PeakTextResolver.resolved(timer, "prefix", timer.getPrefix());
        PeakStyledText customSuffix = PeakTextResolver.resolved(timer, "suffix", timer.getSuffix());
        PeakStyledText suffix = customSuffix.isEmpty() ? PeakStyledText.ofLegacy(autoSuffix) : customSuffix;
        return prefix.concat(PeakStyledText.of(core, Style.EMPTY)).concat(suffix);
    }

    private static String suffixFor(String unitKey) {
        String key = "peakclient.timer.unit." + unitKey;
        String translated = I18n.get(key);
        if (!translated.equals(key)) return translated;
        return switch (unitKey) {
            case "ticks" -> "t";
            case "seconds" -> "s";
            case "minutes" -> "m";
            case "hours" -> "h";
            case "days" -> "d";
            default -> "";
        };
    }

    private static String minutesSecondsUnitsAlways(long totalSeconds) {
        return (totalSeconds / 60) + suffixFor("minutes") + " " + (totalSeconds % 60) + suffixFor("seconds");
    }

    private static String hoursMinutesSecondsUnitsAlways(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return hours + suffixFor("hours") + " " + minutes + suffixFor("minutes") + " " + seconds + suffixFor("seconds");
    }

    private static String daysHoursMinutesSecondsUnitsAlways(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return days + suffixFor("days") + " " + hours + suffixFor("hours") + " "
                + minutes + suffixFor("minutes") + " " + seconds + suffixFor("seconds");
    }

    private static String dynamicColonFormat(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) return String.format(Locale.ROOT, "%d:%02d:%02d:%02d", days, hours, minutes, seconds);
        if (hours > 0) return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds);
        if (minutes > 0) return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
        return Long.toString(seconds);
    }

    private static String dynamicUnitsFormat(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) return String.format(Locale.ROOT, "%d%s %02d%s %02d%s %02d%s",
                days, suffixFor("days"), hours, suffixFor("hours"), minutes, suffixFor("minutes"), seconds, suffixFor("seconds"));
        if (hours > 0) return String.format(Locale.ROOT, "%d%s %02d%s %02d%s",
                hours, suffixFor("hours"), minutes, suffixFor("minutes"), seconds, suffixFor("seconds"));
        if (minutes > 0) return String.format(Locale.ROOT, "%d%s %02d%s",
                minutes, suffixFor("minutes"), seconds, suffixFor("seconds"));
        return seconds + suffixFor("seconds");
    }
}
