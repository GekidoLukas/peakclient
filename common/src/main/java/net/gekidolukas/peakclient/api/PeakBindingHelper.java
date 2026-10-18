package net.gekidolukas.peakclient.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakGradientDirection;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakSolidColor;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

import java.util.Optional;
import java.util.regex.Pattern;

public final class PeakBindingHelper {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([\\w.]+):([\\w./-]+)(?:=([^#}]+))?(?:#([^}]+))?}");

    private PeakBindingHelper() {}

    public static final class Paths {
        public static final String STYLE_BACKGROUND = "style.background";
        public static final String STYLE_BORDER_COLOR = "style.borderColor";
        public static final String STYLE_BORDER_WIDTH = "style.borderWidth";
        public static final String STYLE_BORDER_RADIUS = "style.borderRadius";
        public static final String STYLE_SHADOW_COLOR = "style.shadowColor";
        public static final String OPACITY = "opacity";
        public static final String HIDDEN = "hidden";
        public static final String SCALE = "scale";
        public static final String COLOR = "color";
        public static final String TEXT = "text";
        public static final String FILL_COLOR = "fillColor";
        public static final String BACKGROUND_COLOR = "backgroundColor";

        private Paths() {}
    }

    public static String paint(PeakPaint paint) {
        return paint.toExpression();
    }

    public static String paint(int argb) {
        return PeakPaint.solid(argb).toExpression();
    }

    public static Gradient gradient() {
        return new Gradient();
    }

    public static String placeholder(String key) {
        requireKey(key);
        return "{" + key + "}";
    }

    public static String placeholder(String key, String fallback) {
        requireKey(key);
        return "{" + key + "#" + fallback + "}";
    }

    public static String placeholder(PeakValueKey<?> key) {
        return key.placeholder();
    }

    public static String placeholder(PeakValueKey<?> key, String fallback) {
        return key.placeholder(fallback);
    }

    public static String colorPlaceholder(PeakValueKey<?> key, int fallbackArgb) {
        return key.placeholder(PeakArgb.format(PeakArgb.normalize(fallbackArgb)));
    }

    public static String colorPlaceholder(PeakValueKey<?> key, PeakSolidColor fallback) {
        return colorPlaceholder(key, fallback.argb());
    }

    public static String builtin(String name) {
        return "{minecraft:" + name + "}";
    }

    public static String builtin(String name, String argument) {
        return "{minecraft:" + name + "=" + argument + "}";
    }

    public static String builtin(String name, String argument, String fallback) {
        return "{minecraft:" + name + "=" + argument + "#" + fallback + "}";
    }

    public static String text(Object... parts) {
        StringBuilder text = new StringBuilder();
        for (Object part : parts) {
            if (part instanceof PeakValueKey<?> key) text.append(key.placeholder());
            else text.append(part);
        }
        return text.toString();
    }

    public static String number(PeakValueKey<?> key, Number fallback) {
        return key.placeholder(String.valueOf(fallback));
    }

    public static String bool(PeakValueKey<?> key, boolean fallback) {
        return key.placeholder(String.valueOf(fallback));
    }

    public static boolean containsPlaceholder(String expression) {
        return expression != null && PLACEHOLDER.matcher(expression).find();
    }

    public static Optional<String> validatePaint(String expression) {
        if (expression == null || expression.isBlank()) return Optional.of("The expression is empty");
        String probe = PLACEHOLDER.matcher(expression).replaceAll("#FFFFFF");
        try {
            PeakPaintAdapter.parse(probe);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.of(e.getMessage());
        }
    }

    private static void requireKey(String key) {
        int separator = key == null ? -1 : key.indexOf(':');
        if (separator <= 0 || separator == key.length() - 1) {
            throw new IllegalArgumentException("A placeholder key must look like 'namespace:key', got '" + key + "'");
        }
    }

    public static final class Gradient {
        private final JsonArray stops = new JsonArray();
        private PeakGradientDirection direction = PeakGradientDirection.LEFT_TO_RIGHT;
        private Float angle;
        private boolean radial;
        private float centerX = 0.5f;
        private float centerY = 0.5f;
        private float radius;
        private int cycleTicks;

        private Gradient() {}

        public Gradient stop(float position, PeakSolidColor color) {
            return stop(position, PeakArgb.format(PeakArgb.normalize(color.argb())));
        }

        public Gradient stop(float position, int argb) {
            return stop(position, PeakArgb.format(PeakArgb.normalize(argb)));
        }

        public Gradient stop(float position, PeakValueKey<?> key) {
            return stop(position, key.placeholder());
        }

        public Gradient stop(float position, PeakValueKey<?> key, int fallbackArgb) {
            return stop(position, colorPlaceholder(key, fallbackArgb));
        }

        public Gradient stop(float position, String colorOrPlaceholder) {
            JsonArray pair = new JsonArray();
            pair.add(position);
            pair.add(colorOrPlaceholder);
            stops.add(pair);
            return this;
        }

        public Gradient evenly(Object... colors) {
            if (colors.length < 2) throw new IllegalArgumentException("A gradient needs at least two colors");
            for (int i = 0; i < colors.length; i++) {
                float position = (float) i / (colors.length - 1);
                Object color = colors[i];
                if (color instanceof PeakSolidColor solid) stop(position, solid);
                else if (color instanceof Integer argb) stop(position, argb.intValue());
                else if (color instanceof PeakValueKey<?> key) stop(position, key);
                else stop(position, String.valueOf(color));
            }
            return this;
        }

        public Gradient angle(float degrees) {
            this.angle = degrees;
            return this;
        }

        public Gradient radial() {
            this.radial = true;
            return this;
        }

        public Gradient center(float x, float y) {
            this.centerX = x;
            this.centerY = y;
            return this;
        }

        public Gradient radius(float shorterSideFraction) {
            this.radius = shorterSideFraction;
            return this;
        }

        public Gradient vertical() {
            this.direction = PeakGradientDirection.TOP_TO_BOTTOM;
            this.angle = null;
            return this;
        }

        public Gradient horizontal() {
            this.direction = PeakGradientDirection.LEFT_TO_RIGHT;
            this.angle = null;
            return this;
        }

        public Gradient animated(int cycleTicks) {
            this.cycleTicks = Math.max(0, cycleTicks);
            return this;
        }

        public String expression() {
            if (stops.isEmpty()) throw new IllegalStateException("A gradient needs at least one stop");
            JsonObject object = new JsonObject();
            if (radial) {
                object.addProperty("type", "radial");
                object.add("stops", stops);
                if (centerX != 0.5f || centerY != 0.5f) {
                    JsonArray center = new JsonArray();
                    center.add(centerX);
                    center.add(centerY);
                    object.add("center", center);
                }
                if (radius > 0f) object.addProperty("radius", radius);
                if (cycleTicks > 0) object.addProperty("cycle", cycleTicks);
                return object.toString();
            }
            object.add("stops", stops);
            if (angle != null) object.addProperty("angle", angle);
            else if (direction != PeakGradientDirection.LEFT_TO_RIGHT) object.addProperty("direction", direction.name());
            if (cycleTicks > 0) object.addProperty("cycle", cycleTicks);
            return object.toString();
        }

        @Override
        public String toString() {
            return expression();
        }
    }
}
