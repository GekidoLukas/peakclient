package net.gekidolukas.peakclient.debug;

import net.gekidolukas.peakclient.render.PeakChibiMetrics;
import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakProgressValueResolver;
import net.gekidolukas.peakclient.render.PeakProgressValues;
import net.gekidolukas.peakclient.render.PeakUiRenderer;
import net.gekidolukas.peakclient.ui.PeakAbstractProgress;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakCarousel;
import net.gekidolukas.peakclient.ui.PeakChibiPlayer;
import net.gekidolukas.peakclient.ui.PeakColorStop;
import net.gekidolukas.peakclient.ui.PeakCompass;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakCycler;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakLinearGradient;
import net.gekidolukas.peakclient.ui.PeakRadialGradient;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakSolidColor;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakMinimap;
import net.gekidolukas.peakclient.ui.PeakReaction;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.gekidolukas.peakclient.render.PeakWorldAnchorLayout;
import net.gekidolukas.peakclient.ui.PeakPopUpContainer;
import net.gekidolukas.peakclient.ui.PeakFeed;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.PeakStagger;
import net.gekidolukas.peakclient.ui.PeakPosition;
import net.gekidolukas.peakclient.ui.PeakProgressBar;
import net.gekidolukas.peakclient.ui.PeakRadialProgress;
import net.gekidolukas.peakclient.ui.PeakSize;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.PeakTableGrid;
import net.gekidolukas.peakclient.ui.PeakTableLayoutPanel;
import net.gekidolukas.peakclient.ui.PeakTimer;
import net.gekidolukas.peakclient.ui.client.PeakClientWaypointRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public final class PeakElementInspector {
    private static final Map<Class<?>, Function<PeakElement<?>, List<String>>> LIVE_VALUES = new HashMap<>();
    private static final int MAX_COLLECTION_ITEMS = 6;
    private static final int MAX_OBJECT_DEPTH = 2;

    static {
        registerLive(PeakTimer.class, e -> List.of(
                kv("value (live)", e.currentValue() + " ticks")
        ));
        registerLive(PeakProgressBar.class, PeakElementInspector::progressLines);
        registerLive(PeakRadialProgress.class, PeakElementInspector::progressLines);
        registerLive(PeakCompass.class, e -> List.of(
                kv("waypoints visible", String.valueOf(PeakClientWaypointRegistry.getVisibleIn(e.getId()).size()))
        ));
        registerLive(PeakMinimap.class, e -> List.of(
                kv("waypoints visible", String.valueOf(PeakClientWaypointRegistry.getVisibleIn(e.getId()).size()))
        ));
        registerLive(PeakTableLayoutPanel.class, e -> {
            PeakTableGrid.Result grid = PeakUiRenderer.lastTableGrid(e.getId());
            if (grid == null) return List.of();
            return List.of(
                    kv("columns (live)", joinSizes(grid.columnWidths())),
                    kv("rows (live)", joinSizes(grid.rowHeights())),
                    kv("content (live)", grid.contentWidth() + "x" + grid.contentHeight())
            );
        });
        registerLive(PeakChibiPlayer.class, e -> {
            PeakChibiMetrics.Sample sample = PeakChibiMetrics.last(e.getId());
            if (sample == null) return List.of();
            return List.of(
                    kv("px per unit", number(sample.physicalPixelsPerUnit()) + "  (unit " + number(sample.unit())
                            + " x abs " + number(sample.absScale()) + " x gui " + number(sample.guiScale())
                            + " x factor " + number(sample.scissorUnitFactor()) + ")"),
                    kv("head scale", number(sample.roundedHeadScale()) + "  (base " + number(sample.headScale()) + ")"),
                    kv("head height px", sample.roundedHeadPixels() + "  (unrounded " + number(sample.headPixels()) + ")"),
                    kv("neck y px", number(sample.neckScreenY())),
                    kv("head top y px", number(sample.headTopScreenY())),
                    kv("arm top y px", number(sample.armTopScreenY())),
                    kv("feet y px", number(sample.feetScreenY()))
            );
        });
        registerLive(PeakPopUpContainer.class, e -> {
            var state = e.computeAnimState();
            return List.of(kv("animState (live)", "hidden=" + state.hidden()
                    + " scale=" + number(state.scale()) + " alpha=" + number(state.alpha())));
        });
        registerLive(PeakCycler.class, e -> {
            var current = e.timeline().getCurrentItem();
            return List.of(kv("showing (live)", current == null ? "-" : current.getId()));
        });
        registerLive(PeakCarousel.class, e -> List.of(kv("queued (live)", String.valueOf(e.track().queued().size()))));
        registerLive(PeakStagger.class, e -> List.of(kv("mode (live)", e.timeline().getMode().name())));
        registerLive(PeakFeed.class, e -> List.of(kv("tracked (live)", String.valueOf(e.motion().trackedCount()))));
        registerLive(PeakSortedList.class, e -> List.of(kv("tracked (live)", String.valueOf(e.motion().trackedCount()))));
        registerLive(PeakReel.class, e -> List.of(kv("spinning (live)", String.valueOf(e.motion().isSpinning()))));
        registerLive(PeakWorldAnchor.class, PeakElementInspector::worldAnchorLines);
    }

    private PeakElementInspector() {}

    private static String joinSizes(int[] sizes) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < sizes.length; i++) {
            if (i > 0) out.append(", ");
            out.append(sizes[i]);
        }
        return sizes.length == 0 ? "-" : out.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends PeakElement<?>> void registerLive(Class<T> type, Function<T, List<String>> inspector) {
        LIVE_VALUES.put(type, (Function<PeakElement<?>, List<String>>) (Function<?, List<String>>) inspector);
    }

    public static List<String> inspect(PeakLayoutEntry entry) {
        PeakElement<?> element = entry.element();
        List<String> lines = new ArrayList<>();
        lines.add("§e" + entry.elementId() + " §7(" + entry.elementType() + ")");
        lines.add(kv("doc", entry.documentId()));

        lines.add(section("geometry"));
        lines.add(kv("screen", entry.absX() + ", " + entry.absY() + "  " + entry.width() + "x" + entry.height()));
        lines.add(kv("layout", entry.localWidth() + "x" + entry.localHeight() + "  abs scale " + number(entry.absScale())));
        PeakSize size = element.getSize();
        lines.add(kv("width", dimension(size.getWidth(), entry.percentBaseWidth())));
        lines.add(kv("height", dimension(size.getHeight(), entry.percentBaseHeight())));
        lines.add(kv("percent base", entry.percentBaseWidth() < 0 && entry.percentBaseHeight() < 0 ? "none (parent has no fixed size)"
                : baseText(entry.percentBaseWidth()) + " x " + baseText(entry.percentBaseHeight())));
        constraintLine(lines, "minWidth", element.getConstraints().getMinWidth(), entry.percentBaseWidth());
        constraintLine(lines, "maxWidth", element.getConstraints().getMaxWidth(), entry.percentBaseWidth());
        constraintLine(lines, "minHeight", element.getConstraints().getMinHeight(), entry.percentBaseHeight());
        constraintLine(lines, "maxHeight", element.getConstraints().getMaxHeight(), entry.percentBaseHeight());

        PeakPosition position = element.getPosition();
        lines.add(kv("position", element.getPositionMode() + " " + position.getAnchor()
                + " " + signed(position.getOffsetX()) + "," + signed(position.getOffsetY())));
        PeakStyle style = element.getStyle();
        lines.add(kv("padding", insets(style.getPadding())));
        lines.add(kv("margin", insets(style.getMargin())));
        if (element.getAlignSelf() != PeakAlign.AUTO) lines.add(kv("alignSelf", element.getAlignSelf().name()));
        if (element.getVerticalAlign() != PeakAlign.AUTO) lines.add(kv("verticalAlign", element.getVerticalAlign().name()));

        lines.add(section("appearance"));
        lines.add(kv("opacity", number(element.getOpacity())));
        lines.add(kv("scale", number(element.getScale())));
        if (element.getRotationDegrees() != 0f) lines.add(kv("rotation", number(element.getRotationDegrees()) + " deg"));
        lines.add(kv("hidden", element.isHidden() + (element.isHidden() ? " (collapse " + element.isCollapseWhenHidden() + ")" : "")));
        if (!style.getBackground().isNone()) lines.add(kv("background", paint(style.getBackground())));
        if (style.getBorderWidth() > 0) lines.add(kv("border", style.getBorderWidth() + "px " + paint(style.getBorderColor())));
        if (style.getBorderRadius() > 0) lines.add(kv("borderRadius", style.getBorderRadius() + "px"));
        if (!style.getShadowColor().isNone()) {
            lines.add(kv("shadow", paint(style.getShadowColor()) + " offset " + signed(style.getShadowOffsetX()) + ","
                    + signed(style.getShadowOffsetY()) + " blur " + style.getShadowBlur()));
        }
        if (!element.getClasses().isEmpty()) lines.add(kv("classes", String.join(", ", element.getClasses())));
        for (Map.Entry<String, String> binding : element.getBindings().entrySet()) {
            lines.add(kv("bind " + binding.getKey(), binding.getValue()));
        }

        PeakInteraction interaction = element.getInteraction();
        if (interaction != null) {
            lines.add(kv("interaction", (interaction.isEnabled() ? "enabled" : "disabled") + ", "
                    + element.getEvents().size() + " event(s)"));
            if (!interaction.getTooltip().getString().isEmpty()) {
                lines.add(kv("tooltip", "\"" + interaction.getTooltip().getString().replace('\n', '|') + "\""));
            }
            if (interaction.isKeepFocus()) lines.add(kv("keepFocus", "true"));
            if (!interaction.getFocusColor().isNone() || interaction.getFocusWidth() > 0) {
                lines.add(kv("focus ring", paint(interaction.getFocusColor()) + " width " + interaction.getFocusWidth()));
            }
            if (interaction.getTooltipStyle() != null) lines.add(kv("tooltip style", "custom"));
            if (interaction.getClickSound() != null) lines.add(kv("click sound", interaction.getClickSound().getSound()));
            if (interaction.getHoverSound() != null) lines.add(kv("hover sound", interaction.getHoverSound().getSound()));
            if (interaction.getTooltipElement() != null) {
                lines.add(kv("tooltip", interaction.getTooltipElement().getClass().getSimpleName()
                        + " (" + interaction.getTooltipElement().getId() + ")"));
            }
        }
        for (PeakReaction reaction : element.getEvents()) {
            lines.add(kv("on " + reaction.getOn().name().toLowerCase(Locale.ROOT), eventDetail(reaction)));
            for (PeakAction action : reaction.getActions()) {
                lines.add(kv("  action", action.type() + " " + action.target() + (action.args().isEmpty() ? "" : " " + action.args())));
            }
        }

        List<String> fieldLines = fieldLines(element);
        Function<PeakElement<?>, List<String>> live = LIVE_VALUES.get(element.getClass());
        if (!fieldLines.isEmpty() || live != null) lines.add(section(entry.elementType()));
        if (element instanceof PeakContainer<?> container) {
            lines.add(kv("children", String.valueOf(container.getChildren().size())));
        }
        lines.addAll(fieldLines);
        if (live != null) {
            try {
                lines.addAll(live.apply(element));
            } catch (Exception e) {
                lines.add("§c(inspector error: " + e + ")");
            }
        }
        return lines;
    }

    private static List<String> fieldLines(PeakElement<?> element) {
        List<Class<?>> chain = new ArrayList<>();
        for (Class<?> current = element.getClass(); current != null && current != PeakElement.class; current = current.getSuperclass()) {
            chain.add(0, current);
        }
        List<String> lines = new ArrayList<>();
        for (Class<?> type : chain) {
            for (Field field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) continue;
                String name = field.getName();
                if (name.equals("id") || name.equals("children")) continue;
                try {
                    field.setAccessible(true);
                    lines.add(kv(name, format(name, field.get(element), 0)));
                } catch (RuntimeException | IllegalAccessException e) {
                    lines.add(kv(name, "?"));
                }
            }
        }
        return lines;
    }

    private static String format(String name, Object value, int depth) {
        if (value == null) return "-";
        if (value instanceof Integer i && name.toLowerCase(Locale.ROOT).endsWith("color")) return color(i);
        if (value instanceof Float f) return number(f);
        if (value instanceof Double d) return number(d);
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof String s) return "\"" + s.replace('§', '&') + "\"";
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof PeakSize.PeakDimension d) return dimensionPlain(d);
        if (value instanceof PeakInsets insets) return insets(insets);
        if (value instanceof PeakPaint paint) return paint(paint);
        if (value instanceof Collection<?> collection) return collection(name, collection, depth);
        if (value instanceof Map<?, ?> map) {
            List<String> parts = new ArrayList<>();
            int shown = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (shown++ >= MAX_COLLECTION_ITEMS) {
                    parts.add("...");
                    break;
                }
                parts.add(entry.getKey() + "=" + format(String.valueOf(entry.getKey()), entry.getValue(), depth + 1));
            }
            return "{" + String.join(", ", parts) + "}";
        }
        if (value.getClass().getName().startsWith("net.gekidolukas")) return object(value, depth);
        return value.toString().replace('§', '&');
    }

    private static String collection(String name, Collection<?> collection, int depth) {
        List<String> parts = new ArrayList<>();
        int shown = 0;
        for (Object item : collection) {
            if (shown++ >= MAX_COLLECTION_ITEMS) {
                parts.add("...");
                break;
            }
            parts.add(format(name, item, depth + 1));
        }
        return "[" + collection.size() + "] " + String.join(", ", parts);
    }

    private static String object(Object value, int depth) {
        Class<?> type = value.getClass();
        if (depth >= MAX_OBJECT_DEPTH) return type.getSimpleName();
        List<String> parts = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) continue;
                try {
                    field.setAccessible(true);
                    parts.add(field.getName() + "=" + format(field.getName(), field.get(value), depth + 1));
                } catch (RuntimeException | IllegalAccessException e) {
                    parts.add(field.getName() + "=?");
                }
            }
        }
        return type.getSimpleName() + "{" + String.join(", ", parts) + "}";
    }

    private static List<String> progressLines(PeakAbstractProgress<?> progress) {
        PeakProgressValues values = PeakProgressValueResolver.resolve(progress);
        return List.of(kv("value (live)", number(values.current()) + " / " + number(values.max())));
    }

    private static void constraintLine(List<String> lines, String name, PeakSize.PeakDimension dimension, int base) {
        if (dimension.mode() == PeakSize.PeakDimension.Mode.AUTO) return;
        lines.add(kv(name, dimension(dimension, base)));
    }

    private static String dimension(PeakSize.PeakDimension dimension, int base) {
        return switch (dimension.mode()) {
            case AUTO -> "auto";
            case PIXELS -> (int) dimension.value() + "px";
            case PERCENT -> number(dimension.value()) + "%" + (base < 0
                    ? " §8(no fixed parent size, counts as auto)"
                    : " §8of " + base + "px = " + Math.round(base * dimension.value() / 100f) + "px");
        };
    }

    private static String dimensionPlain(PeakSize.PeakDimension dimension) {
        return switch (dimension.mode()) {
            case AUTO -> "auto";
            case PIXELS -> (int) dimension.value() + "px";
            case PERCENT -> number(dimension.value()) + "%";
        };
    }

    private static String baseText(int base) {
        return base < 0 ? "-" : base + "px";
    }

    private static String insets(PeakInsets insets) {
        return insets.getTop() + " " + insets.getRight() + " " + insets.getBottom() + " " + insets.getLeft() + " §8(t r b l)";
    }

    private static String paint(PeakPaint paint) {
        if (paint instanceof PeakSolidColor solid) return color(solid.argb());
        if (paint instanceof PeakLinearGradient gradient) {
            List<String> stops = new ArrayList<>();
            for (PeakColorStop stop : gradient.getStops()) stops.add(number(stop.position()) + ":" + color(stop.color()));
            String axis = gradient.hasCustomAngle() ? number(gradient.getAngle()) + " deg" : gradient.getDirection().toString();
            return axis + (gradient.isAnimated() ? " animated/" + gradient.getCycleTicks() + "t " : " ")
                    + String.join(" ", stops);
        }
        if (paint instanceof PeakRadialGradient radial) {
            List<String> stops = new ArrayList<>();
            for (PeakColorStop stop : radial.getStops()) stops.add(number(stop.position()) + ":" + color(stop.color()));
            return "radial " + number(radial.getCenterX()) + "," + number(radial.getCenterY())
                    + (radial.getRadius() > 0f ? " r " + number(radial.getRadius()) : "")
                    + (radial.isAnimated() ? " animated/" + radial.getCycleTicks() + "t " : " ") + String.join(" ", stops);
        }
        return paint.toString();
    }

    private static String color(int argb) {
        return String.format(Locale.ROOT, "#%08X", argb);
    }

    private static String signed(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    private static String number(double value) {
        String text = String.format(Locale.ROOT, "%.3f", value);
        if (text.indexOf('.') >= 0) {
            text = text.replaceAll("0+$", "");
            if (text.endsWith(".")) text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static String kv(String name, String value) {
        return "§7" + name + ": §f" + value;
    }

    private static String eventDetail(PeakReaction reaction) {
        StringBuilder detail = new StringBuilder();
        if (!reaction.getWatch().isEmpty()) detail.append("watch ").append(reaction.getWatch()).append(' ');
        if (reaction.getOn().isThreshold()) detail.append("threshold ").append(reaction.getThreshold()).append(' ');
        if (reaction.getTicks() > 0) detail.append("ticks ").append(reaction.getTicks()).append(' ');
        if (reaction.getCooldownTicks() > 0) detail.append("cooldown ").append(reaction.getCooldownTicks()).append(' ');
        if (reaction.isOnce()) detail.append("once ");
        detail.append(reaction.getActions().size()).append(" action(s)");
        return detail.toString();
    }

    private static List<String> worldAnchorLines(PeakWorldAnchor anchor) {
        PeakWorldAnchorLayout.Info info = PeakWorldAnchorLayout.info(anchor);
        if (info == null) return List.of(kv("anchor (live)", "not evaluated"));
        if (!info.drawn()) return List.of(kv("anchor (live)", "hidden (no target, out of range or off screen)"));
        return List.of(
                kv("screen (live)", String.format(Locale.ROOT, "%.0f, %.0f", info.screenX(), info.screenY())),
                kv("distance (live)", String.format(Locale.ROOT, "%.1f", info.distance())),
                kv("occluded (live)", info.partial() ? "per pixel (depth test)"
                        : info.occluded() + " (factor " + String.format(Locale.ROOT, "%.2f", info.factor()) + ")"),
                kv("edge (live)", String.valueOf(info.clamped())));
    }

    private static String section(String title) {
        return "§8--- " + title + " ---";
    }
}
