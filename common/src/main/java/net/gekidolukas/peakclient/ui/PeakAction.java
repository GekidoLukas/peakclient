package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.patch.PeakBasePatch;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PeakAction {
    public static final int MAX_TARGET_LENGTH = 64;
    public static final int MAX_ARGUMENTS = 32;
    public static final int MAX_ARGUMENT_LENGTH = 2048;

    private PeakActionType type = PeakActionType.CLOSE_MENU;
    private String target = "";
    private Map<String, String> args = new LinkedHashMap<>();
    private JsonObject patch;
    private List<String> collect;
    private Boolean collectAll;
    private Boolean validate;

    public static PeakAction close() {
        return of(PeakActionType.CLOSE_MENU, "");
    }

    public static PeakAction back() {
        return of(PeakActionType.BACK, "");
    }

    public static PeakAction open(String menuId) {
        return of(PeakActionType.OPEN_MENU, PeakId.require(menuId, "Menu"));
    }

    public static PeakAction replaceWith(String menuId) {
        return of(PeakActionType.OPEN_MENU, PeakId.require(menuId, "Menu")).arg("replace", "true");
    }

    public static PeakAction send(String actionId) {
        return of(PeakActionType.SEND, PeakId.require(actionId, "Action"));
    }

    public static PeakAction send(String actionId, Map<String, String> args) {
        PeakAction action = of(PeakActionType.SEND, PeakId.require(actionId, "Action"));
        action.args.putAll(args);
        return action;
    }

    public static PeakAction patch(String elementId, PeakBasePatch<?> patch) {
        PeakAction action = of(PeakActionType.PATCH, PeakId.require(elementId, "Element"));
        action.patch = patch.build().deepCopy();
        return action;
    }

    public static PeakAction copyText(String text) {
        return of(PeakActionType.COPY_TEXT, text);
    }

    public static PeakAction format(String boxId, PeakFormat format) {
        return of(PeakActionType.FORMAT, PeakId.require(boxId, "Element")).arg("kind", format.name());
    }

    public static PeakAction formatColor(String boxId, int rgb) {
        return of(PeakActionType.FORMAT, PeakId.require(boxId, "Element")).arg("kind", PeakFormat.COLOR.name())
                .arg("color", Integer.toString(rgb & 0xFFFFFF));
    }

    public static PeakAction trigger(String elementId, PeakAnimationEffect effect) {
        return trigger(elementId, effect, 12, 1f, 0);
    }

    public static PeakAction trigger(String elementId, PeakAnimationEffect effect, int color) {
        return trigger(elementId, effect, 14, 1f, color);
    }

    public static PeakAction trigger(String elementId, PeakAnimationEffect effect, int ticks, float intensity, int color) {
        return patch(elementId, PeakPatch.animated().trigger(effect, ticks, intensity, color));
    }

    public static PeakAction sound(String sound) {
        return of(PeakActionType.SOUND, sound);
    }

    public static PeakAction sound(String sound, float volume, float pitch) {
        return of(PeakActionType.SOUND, sound).arg("volume", Float.toString(volume)).arg("pitch", Float.toString(pitch));
    }

    public PeakSoundSpec soundSpec() {
        PeakSoundSpec spec = PeakSoundSpec.of(target());
        try {
            if (args().containsKey("volume")) spec.volume(Float.parseFloat(args().get("volume")));
            if (args().containsKey("pitch")) spec.pitch(Float.parseFloat(args().get("pitch")));
        } catch (NumberFormatException ignored) {
            return spec;
        }
        return spec;
    }

    public static PeakAction toggle(String elementId) {
        return of(PeakActionType.TOGGLE, PeakId.require(elementId, "Element"));
    }

    public static PeakAction setColor(String pickerId, int argb) {
        return of(PeakActionType.SET_COLOR, PeakId.require(pickerId, "Element")).arg("color", Integer.toString(argb));
    }

    public int colorValue() {
        try {
            return Integer.parseInt(args().getOrDefault("color", "-1"));
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF;
        }
    }

    public PeakFormat formatKind() {
        String kind = args().get("kind");
        if (kind == null) return null;
        try {
            return PeakFormat.valueOf(kind);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int formatColorValue() {
        try {
            return Integer.parseInt(args().getOrDefault("color", "0")) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static PeakAction of(PeakActionType type, String target) {
        PeakAction action = new PeakAction();
        action.type = type;
        action.target = target == null ? "" : target;
        return action;
    }

    public PeakAction arg(String key, String value) {
        args.put(key, value);
        return this;
    }

    public PeakAction collect(String... elementIds) {
        if (collect == null) collect = new ArrayList<>();
        collect.addAll(Arrays.asList(elementIds));
        return this;
    }

    public PeakAction collectAll() {
        this.collectAll = Boolean.TRUE;
        return this;
    }

    public PeakAction validate() {
        this.validate = Boolean.TRUE;
        return this;
    }

    public boolean validates() { return Boolean.TRUE.equals(validate); }

    public List<String> collected() { return collect == null ? List.of() : collect; }
    public boolean collectsAll() { return Boolean.TRUE.equals(collectAll); }

    public PeakActionType type() { return type; }
    public String target() { return target == null ? "" : target; }
    public Map<String, String> args() { return args == null ? Map.of() : args; }
    public JsonObject patch() { return patch; }

    public boolean replace() {
        return "true".equals(args().get("replace"));
    }
}
