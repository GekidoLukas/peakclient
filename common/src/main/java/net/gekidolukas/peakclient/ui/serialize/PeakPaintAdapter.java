package net.gekidolukas.peakclient.ui.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.gekidolukas.peakclient.ui.PeakArgb;
import net.gekidolukas.peakclient.ui.PeakColorStop;
import net.gekidolukas.peakclient.ui.PeakGradientDirection;
import net.gekidolukas.peakclient.ui.PeakLinearGradient;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakRadialGradient;
import net.gekidolukas.peakclient.ui.PeakSolidColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PeakPaintAdapter extends TypeAdapter<PeakPaint> {
    private static final String STOPS = "stops";
    private static final String DIRECTION = "direction";
    private static final String CYCLE = "cycle";
    private static final String ANGLE = "angle";
    private static final String TYPE = "type";
    private static final String RADIAL = "radial";
    private static final String CENTER = "center";
    private static final String RADIUS = "radius";

    @Override
    public void write(JsonWriter out, PeakPaint value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        writeTree(out, toTree(value));
    }

    private static void writeTree(JsonWriter out, JsonElement element) throws IOException {
        if (element.isJsonArray()) {
            out.beginArray();
            for (JsonElement item : element.getAsJsonArray()) writeTree(out, item);
            out.endArray();
        } else if (element.isJsonObject()) {
            out.beginObject();
            for (var entry : element.getAsJsonObject().entrySet()) {
                out.name(entry.getKey());
                writeTree(out, entry.getValue());
            }
            out.endObject();
        } else if (element.isJsonNull()) {
            out.nullValue();
        } else {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) out.value(primitive.getAsBoolean());
            else if (primitive.isNumber()) out.value(primitive.getAsNumber());
            else out.value(primitive.getAsString());
        }
    }

    @Override
    public PeakPaint read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return fromTree(JsonParser.parseReader(in));
    }

    public static PeakPaint parse(String expression) {
        if (expression == null) throw new IllegalArgumentException("A paint expression must not be null");
        String text = expression.trim();
        if (text.isEmpty()) throw new IllegalArgumentException("A paint expression must not be empty");
        if (text.startsWith("{")) {
            try {
                return fromTree(JsonParser.parseString(text));
            } catch (JsonParseException e) {
                throw new IllegalArgumentException("Not a paint expression: '" + expression + "' (" + e.getMessage() + ")");
            }
        }
        Integer argb = PeakArgb.parse(text);
        if (argb == null) throw new IllegalArgumentException("Not a color: '" + expression + "'");
        return PeakPaint.solid(argb);
    }

    public static String toExpression(PeakPaint paint) {
        if (paint instanceof PeakSolidColor solid) return PeakArgb.format(solid.argb());
        return toTree(paint).toString();
    }

    public static JsonElement toTree(PeakPaint paint) {
        if (paint instanceof PeakSolidColor solid) return new JsonPrimitive(solid.argb());
        if (paint instanceof PeakRadialGradient radial) return radialTree(radial);
        PeakLinearGradient gradient = (PeakLinearGradient) paint;
        JsonObject object = new JsonObject();
        object.add(STOPS, stopsTree(gradient.getStops()));
        if (gradient.hasCustomAngle()) {
            object.addProperty(ANGLE, gradient.getAngle());
        } else if (gradient.getDirection() != PeakGradientDirection.LEFT_TO_RIGHT) {
            object.addProperty(DIRECTION, gradient.getDirection().name());
        }
        if (gradient.getCycleTicks() > 0) object.addProperty(CYCLE, gradient.getCycleTicks());
        return object;
    }

    private static JsonArray stopsTree(List<PeakColorStop> stops) {
        JsonArray array = new JsonArray();
        for (PeakColorStop stop : stops) {
            JsonArray pair = new JsonArray();
            pair.add(stop.position());
            pair.add(stop.color());
            array.add(pair);
        }
        return array;
    }

    private static JsonObject radialTree(PeakRadialGradient gradient) {
        JsonObject object = new JsonObject();
        object.addProperty(TYPE, RADIAL);
        object.add(STOPS, stopsTree(gradient.getStops()));
        if (gradient.getCenterX() != PeakRadialGradient.DEFAULT_CENTER || gradient.getCenterY() != PeakRadialGradient.DEFAULT_CENTER) {
            JsonArray center = new JsonArray();
            center.add(gradient.getCenterX());
            center.add(gradient.getCenterY());
            object.add(CENTER, center);
        }
        if (gradient.getRadius() > 0f) object.addProperty(RADIUS, gradient.getRadius());
        if (gradient.getCycleTicks() > 0) object.addProperty(CYCLE, gradient.getCycleTicks());
        return object;
    }

    public static PeakPaint fromTree(JsonElement element) {
        if (element == null || element.isJsonNull()) return PeakPaint.NONE;
        if (element.isJsonPrimitive()) return new PeakSolidColor(colorOf(element));
        if (!element.isJsonObject()) throw new JsonParseException("A paint is a number, a color string or a gradient object");

        JsonObject object = element.getAsJsonObject();
        JsonElement stopsElement = object.get(STOPS);
        if (stopsElement == null || !stopsElement.isJsonArray() || stopsElement.getAsJsonArray().isEmpty()) {
            throw new JsonParseException("A gradient needs a non-empty '" + STOPS + "' array");
        }
        List<PeakColorStop> stops = new ArrayList<>();
        for (JsonElement entry : stopsElement.getAsJsonArray()) stops.add(stopOf(entry));

        int cycleTicks = 0;
        JsonElement cycleElement = object.get(CYCLE);
        if (cycleElement != null && cycleElement.isJsonPrimitive()) cycleTicks = cycleElement.getAsInt();

        JsonElement typeElement = object.get(TYPE);
        if (typeElement != null && typeElement.isJsonPrimitive()) {
            String type = typeElement.getAsString().trim().toLowerCase(Locale.ROOT);
            if (type.equals(RADIAL)) return radialOf(object, stops, cycleTicks);
            if (!type.equals("linear")) throw new JsonParseException("Unknown gradient type '" + type + "'");
        }

        PeakGradientDirection direction = PeakGradientDirection.LEFT_TO_RIGHT;
        JsonElement directionElement = object.get(DIRECTION);
        if (directionElement != null && directionElement.isJsonPrimitive()) {
            direction = directionOf(directionElement.getAsString());
        }
        Float angle = null;
        JsonElement angleElement = object.get(ANGLE);
        if (angleElement != null && angleElement.isJsonPrimitive()) angle = angleElement.getAsFloat();
        return PeakLinearGradient.of(stops, direction, angle, cycleTicks);
    }

    private static PeakRadialGradient radialOf(JsonObject object, List<PeakColorStop> stops, int cycleTicks) {
        float centerX = PeakRadialGradient.DEFAULT_CENTER, centerY = PeakRadialGradient.DEFAULT_CENTER;
        JsonElement centerElement = object.get(CENTER);
        if (centerElement != null && centerElement.isJsonArray() && centerElement.getAsJsonArray().size() == 2) {
            centerX = centerElement.getAsJsonArray().get(0).getAsFloat();
            centerY = centerElement.getAsJsonArray().get(1).getAsFloat();
        }
        float radius = 0f;
        JsonElement radiusElement = object.get(RADIUS);
        if (radiusElement != null && radiusElement.isJsonPrimitive()) radius = radiusElement.getAsFloat();
        return PeakRadialGradient.of(stops, centerX, centerY, radius, cycleTicks);
    }

    private static PeakColorStop stopOf(JsonElement entry) {
        if (entry.isJsonArray() && entry.getAsJsonArray().size() == 2) {
            JsonArray pair = entry.getAsJsonArray();
            return new PeakColorStop(pair.get(0).getAsFloat(), colorOf(pair.get(1)));
        }
        if (entry.isJsonObject()) {
            JsonObject object = entry.getAsJsonObject();
            if (object.has("position") && object.has("color")) {
                return new PeakColorStop(object.get("position").getAsFloat(), colorOf(object.get("color")));
            }
        }
        throw new JsonParseException("A gradient stop is [position, color]");
    }

    private static int colorOf(JsonElement element) {
        if (!element.isJsonPrimitive()) throw new JsonParseException("A color is a number or a color string");
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) return primitive.getAsInt();
        Integer argb = PeakArgb.parse(primitive.getAsString());
        if (argb == null) throw new JsonParseException("Not a color: '" + primitive.getAsString() + "'");
        return PeakArgb.normalize(argb);
    }

    private static PeakGradientDirection directionOf(String text) {
        String name = text.trim().toUpperCase(Locale.ROOT);
        return switch (name) {
            case "VERTICAL", "TOP_TO_BOTTOM" -> PeakGradientDirection.TOP_TO_BOTTOM;
            case "HORIZONTAL", "LEFT_TO_RIGHT" -> PeakGradientDirection.LEFT_TO_RIGHT;
            default -> throw new JsonParseException("Unknown gradient direction '" + text + "'");
        };
    }
}
