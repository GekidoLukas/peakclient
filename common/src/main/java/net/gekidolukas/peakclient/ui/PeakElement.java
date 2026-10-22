package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakLog;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PeakElement<SELF extends PeakElement<SELF>> {
    private static final Set<String> BASE_CUSTOM_PATCH_KEYS = Set.of("bindings", "unbindings");
    private static final Set<String> WARNED_PATCH_KEYS = ConcurrentHashMap.newKeySet();

    protected final String id;
    protected final Set<String> classes = new LinkedHashSet<>();
    protected PeakPosition position = PeakPosition.create();
    protected PeakPositionMode positionMode = PeakPositionMode.RELATIVE;
    protected PeakSize size = PeakSize.create();
    protected PeakStyle style = PeakStyle.create();
    protected PeakConstraints constraints = PeakConstraints.create();
    protected PeakAlign alignSelf = PeakAlign.AUTO;
    protected PeakAlign verticalAlign = PeakAlign.AUTO;
    protected int columnSpan = 1;
    protected int rowSpan = 1;
    protected float scale = 1.0f;
    protected boolean hidden = false;
    protected boolean collapseWhenHidden = true;
    protected float opacity = 1.0f;
    protected double sortKey = 0.0;
    protected float rotationDegrees = 0f;
    protected Map<String, String> bindings = new LinkedHashMap<>();
    protected PeakInteraction interaction;
    protected List<PeakReaction> events = new ArrayList<>();
    private transient Set<String> releasedBindings;

    protected PeakElement(String id) {
        this.id = id == null ? null : PeakId.require(id, "Element");
    }

    public String getId() { return id; }
    public Set<String> getClasses() { return classes; }
    public PeakPosition getPosition() { return position; }
    public PeakPositionMode getPositionMode() { return positionMode; }
    public PeakSize getSize() { return size; }
    public PeakStyle getStyle() { return style; }
    public PeakConstraints getConstraints() { return constraints; }
    public PeakAlign getAlignSelf() { return alignSelf; }
    public float getScale() { return scale; }

    public boolean isHidden() { return hidden; }
    public boolean isCollapseWhenHidden() { return collapseWhenHidden; }

    public SELF addClass(String className) { classes.add(className); return self(); }
    public SELF removeClass(String className) { classes.remove(className); return self(); }
    public boolean hasClass(String className) { return classes.contains(className); }

    public SELF position(PeakPosition position) { this.position = position; return self(); }
    public SELF positionMode(PeakPositionMode mode) { this.positionMode = mode; return self(); }
    public SELF absolute(PeakPosition position) { this.position = position; this.positionMode = PeakPositionMode.ABSOLUTE; return self(); }
    public SELF absolute(PeakAnchor anchor, int offsetX, int offsetY) { return absolute(PeakPosition.of(anchor, offsetX, offsetY)); }
    public SELF absolute(PeakAnchor anchor) { return absolute(PeakPosition.of(anchor)); }
    public SELF position(PeakAnchor anchor, int offsetX, int offsetY) { return position(PeakPosition.of(anchor, offsetX, offsetY)); }
    public SELF size(PeakSize size) { this.size = size; return self(); }
    public SELF size(int width, int height) { return size(PeakSize.pixels(width, height)); }
    public SELF width(int pixels) { this.size = PeakSize.of(PeakSize.PeakDimension.pixels(pixels), this.size.getHeight()); return self(); }
    public SELF height(int pixels) { this.size = PeakSize.of(this.size.getWidth(), PeakSize.PeakDimension.pixels(pixels)); return self(); }
    public SELF widthPercent(float percent) { this.size = PeakSize.of(PeakSize.PeakDimension.percent(percent), this.size.getHeight()); return self(); }
    public SELF heightPercent(float percent) { this.size = PeakSize.of(this.size.getWidth(), PeakSize.PeakDimension.percent(percent)); return self(); }
    public SELF style(PeakStyle style) { this.style = style; return self(); }
    public SELF background(PeakPaint paint) { this.style.background(paint); return self(); }
    public SELF background(int argb) { this.style.background(argb); return self(); }
    public SELF padding(int all) { this.style.padding(all); return self(); }
    public SELF padding(int vertical, int horizontal) { this.style.padding(vertical, horizontal, vertical, horizontal); return self(); }
    public SELF padding(int top, int right, int bottom, int left) { this.style.padding(top, right, bottom, left); return self(); }
    public SELF margin(int all) { this.style.margin(all); return self(); }
    public SELF margin(int vertical, int horizontal) { this.style.margin(vertical, horizontal, vertical, horizontal); return self(); }
    public SELF margin(int top, int right, int bottom, int left) { this.style.margin(top, right, bottom, left); return self(); }
    public SELF border(int width, PeakPaint paint) { this.style.borderWidth(width).borderColor(paint); return self(); }
    public SELF border(int width, int argb) { this.style.borderWidth(width).borderColor(argb); return self(); }
    public SELF borderRadius(int radius) { this.style.borderRadius(radius); return self(); }
    public SELF boxShadow(PeakPaint paint, int offsetX, int offsetY, int blur) { this.style.boxShadow(paint, offsetX, offsetY, blur); return self(); }
    public SELF boxShadow(int argb, int offsetX, int offsetY, int blur) { this.style.boxShadow(argb, offsetX, offsetY, blur); return self(); }
    public SELF constraints(PeakConstraints constraints) { this.constraints = constraints; return self(); }
    public SELF minSize(int width, int height) { this.constraints.minSize(width, height); return self(); }
    public SELF maxSize(int width, int height) { this.constraints.maxSize(width, height); return self(); }
    public SELF alignSelf(PeakAlign alignSelf) { this.alignSelf = alignSelf; return self(); }
    public SELF scale(float scale) { this.scale = scale; return self(); }
    public PeakAlign getVerticalAlign() { return verticalAlign; }
    public SELF verticalAlign(PeakAlign verticalAlign) { this.verticalAlign = verticalAlign; return self(); }
    public int getColumnSpan() { return Math.max(1, columnSpan); }
    public int getRowSpan() { return Math.max(1, rowSpan); }
    public SELF colSpan(int columns) { this.columnSpan = Math.max(1, columns); return self(); }
    public SELF rowSpan(int rows) { this.rowSpan = Math.max(1, rows); return self(); }
    public SELF span(int columns, int rows) { this.columnSpan = Math.max(1, columns); this.rowSpan = Math.max(1, rows); return self(); }
    public SELF hidden(boolean hidden) { this.hidden = hidden; return self(); }
    public SELF collapseWhenHidden(boolean collapse) { this.collapseWhenHidden = collapse; return self(); }
    public float getOpacity() { return opacity; }
    public double getSortKey() { return sortKey; }
    public SELF sortKey(double sortKey) { this.sortKey = sortKey; return self(); }
    public SELF opacity(float opacity) { this.opacity = Math.max(0f, Math.min(1f, opacity)); return self(); }
    public Map<String, String> getBindings() { return bindings == null ? Map.of() : bindings; }
    public SELF bind(String field, String expression) {
        if (bindings == null) bindings = new LinkedHashMap<>();
        bindings.put(field, expression);
        if (releasedBindings != null) releasedBindings.remove(field);
        return self();
    }
    public SELF unbind(String field) {
        if (bindings != null) bindings.remove(field);
        return self();
    }
    public PeakInteraction getInteraction() { return interaction; }
    public SELF interaction(PeakInteraction interaction) { this.interaction = interaction; return self(); }
    public List<PeakReaction> getEvents() { return events == null ? List.of() : events; }

    public SELF on(PeakReaction reaction) {
        if (events == null) events = new ArrayList<>();
        events.add(reaction);
        syncInteraction();
        return self();
    }

    public SELF on(PeakEvent event, PeakAction... actions) { return on(PeakReaction.of(event).then(actions)); }
    public SELF onClick(PeakAction... actions) { return on(PeakEvent.CLICK, actions); }
    public SELF onRightClick(PeakAction... actions) { return on(PeakEvent.RIGHT_CLICK, actions); }
    public SELF onDoubleClick(PeakAction... actions) { return on(PeakEvent.DOUBLE_CLICK, actions); }
    public SELF onHoverEnter(PeakAction... actions) { return on(PeakEvent.HOVER_ENTER, actions); }
    public SELF onHoverLeave(PeakAction... actions) { return on(PeakEvent.HOVER_LEAVE, actions); }
    public SELF onFocus(PeakAction... actions) { return on(PeakEvent.FOCUS, actions); }
    public SELF onBlur(PeakAction... actions) { return on(PeakEvent.BLUR, actions); }
    public SELF onChange(PeakAction... actions) { return on(PeakEvent.CHANGE, actions); }
    public SELF onSubmit(PeakAction... actions) { return on(PeakEvent.SUBMIT, actions); }
    public SELF onAppear(PeakAction... actions) { return on(PeakEvent.APPEAR, actions); }
    public SELF onShow(PeakAction... actions) { return on(PeakEvent.SHOW, actions); }
    public SELF onHide(PeakAction... actions) { return on(PeakEvent.HIDE, actions); }
    public SELF onTimerEnd(PeakAction... actions) { return on(PeakEvent.TIMER_END, actions); }
    public SELF onValueChange(String watch, PeakAction... actions) { return on(PeakReaction.of(PeakEvent.VALUE_CHANGE).watch(watch).then(actions)); }
    public SELF onIncrease(String watch, PeakAction... actions) { return on(PeakReaction.of(PeakEvent.VALUE_INCREASE).watch(watch).then(actions)); }
    public SELF onDecrease(String watch, PeakAction... actions) { return on(PeakReaction.of(PeakEvent.VALUE_DECREASE).watch(watch).then(actions)); }
    public SELF onAbove(String watch, double threshold, PeakAction... actions) {
        return on(PeakReaction.of(PeakEvent.VALUE_ABOVE).watch(watch).threshold(threshold).then(actions));
    }
    public SELF onBelow(String watch, double threshold, PeakAction... actions) {
        return on(PeakReaction.of(PeakEvent.VALUE_BELOW).watch(watch).threshold(threshold).then(actions));
    }
    public SELF after(int ticks, PeakAction... actions) { return on(PeakReaction.of(PeakEvent.DELAY).ticks(ticks).then(actions)); }
    public SELF every(int ticks, PeakAction... actions) { return on(PeakReaction.of(PeakEvent.INTERVAL).ticks(ticks).then(actions)); }

    public List<PeakAction> actionsFor(PeakEvent event) {
        List<PeakAction> result = null;
        for (PeakReaction reaction : getEvents()) {
            if (reaction.getOn() != event || reaction.getActions().isEmpty()) continue;
            if (result == null) result = new ArrayList<>();
            result.addAll(reaction.getActions());
        }
        return result == null ? List.of() : result;
    }

    public boolean hasEvent(PeakEvent event) {
        for (PeakReaction reaction : getEvents()) {
            if (reaction.getOn() == event && !reaction.getActions().isEmpty()) return true;
        }
        return false;
    }

    public void syncInteraction() {
        if (interaction != null) return;
        for (PeakReaction reaction : getEvents()) {
            if (reaction.getOn().isInteraction()) {
                interaction = PeakInteraction.create();
                return;
            }
        }
    }

    public SELF tooltip(String tooltip) { ensureInteraction().tooltip(tooltip); return self(); }
    public SELF tooltip(Component tooltip) { ensureInteraction().tooltip(tooltip); return self(); }
    public SELF tooltip(PeakElement<?> element) { ensureInteraction().tooltip(element); return self(); }
    public SELF enabled(boolean enabled) { ensureInteraction().enabled(enabled); return self(); }
    public SELF hoverStyle(PeakStyle style) { ensureInteraction().hoverStyle(style); return self(); }
    public SELF pressedStyle(PeakStyle style) { ensureInteraction().pressedStyle(style); return self(); }
    public SELF activeStyle(PeakStyle style) { ensureInteraction().activeStyle(style); return self(); }
    public SELF keepFocus(boolean keep) { ensureInteraction().keepFocus(keep); return self(); }
    public SELF focusColor(PeakPaint paint) { ensureInteraction().focusColor(paint); return self(); }
    public SELF focusColor(int color) { ensureInteraction().focusColor(color); return self(); }
    public SELF focusWidth(int width) { ensureInteraction().focusWidth(width); return self(); }
    public SELF tooltipStyle(PeakTooltipStyle style) { ensureInteraction().tooltipStyle(style); return self(); }
    public SELF silent(boolean silent) { ensureInteraction().silent(silent); return self(); }
    public SELF invalid(boolean invalid) { ensureInteraction().invalid(invalid); return self(); }
    public SELF required(boolean required) { ensureInteraction().required(required); return self(); }
    public SELF invalidColor(PeakPaint paint) { ensureInteraction().invalidColor(paint); return self(); }
    public SELF invalidColor(int color) { ensureInteraction().invalidColor(color); return self(); }
    public SELF errorElement(String elementId) { ensureInteraction().errorElement(elementId); return self(); }
    public SELF clickSound(String sound) { ensureInteraction().clickSound(PeakSoundSpec.of(sound)); return self(); }
    public SELF clickSound(String sound, float volume, float pitch) { ensureInteraction().clickSound(PeakSoundSpec.of(sound, volume, pitch)); return self(); }
    public SELF hoverSound(String sound) { ensureInteraction().hoverSound(PeakSoundSpec.of(sound)); return self(); }
    public SELF hoverSound(String sound, float volume, float pitch) { ensureInteraction().hoverSound(PeakSoundSpec.of(sound, volume, pitch)); return self(); }

    private PeakInteraction ensureInteraction() {
        if (interaction == null) interaction = PeakInteraction.create();
        return interaction;
    }

    public float getRotationDegrees() { return rotationDegrees; }
    public SELF rotationDegrees(float degrees) { this.rotationDegrees = degrees; return self(); }

    @SuppressWarnings("unchecked")
    protected SELF self() { return (SELF) this; }

    public abstract String elementType();

    public final void applyPatch(JsonObject patch) {
        applyPatch(patch, true);
    }

    public final void applyResolvedPatch(JsonObject patch) {
        applyPatch(patch, false);
    }

    public final boolean consumeReleasedBinding(String path) {
        return releasedBindings != null && releasedBindings.remove(path);
    }

    static boolean bindingCovers(String patchKey, String bindingPath) {
        return bindingPath.equals(patchKey)
                || bindingPath.startsWith(patchKey + ".")
                || patchKey.startsWith(bindingPath + ".");
    }

    private void releaseBoundFields(JsonObject patch) {
        if (bindings == null || bindings.isEmpty()) return;
        JsonObject rebound = patch.get("bindings") instanceof JsonObject object ? object : null;
        for (String key : patch.keySet()) {
            if (BASE_CUSTOM_PATCH_KEYS.contains(key)) continue;
            for (String path : new ArrayList<>(bindings.keySet())) {
                if (!bindingCovers(key, path)) continue;
                if (rebound != null && rebound.has(path)) continue;
                bindings.remove(path);
                if (releasedBindings == null) releasedBindings = new LinkedHashSet<>();
                releasedBindings.add(path);
            }
        }
    }

    private void applyPatch(JsonObject patch, boolean releaseBindings) {
        beforePatch(patch);
        PeakFieldTable table = PeakFieldTable.of(getClass());
        Set<String> customKeys = customPatchKeys();
        for (Map.Entry<String, JsonElement> entry : patch.entrySet()) {
            String key = entry.getKey();
            try {
                if (customKeys.contains(key)) {
                    applyCustomPatch(key, entry.getValue());
                } else if (key.indexOf('.') >= 0) {
                    if (!PeakFieldTable.applyPath(this, key, entry.getValue())) warnUnknownPatchKey(key);
                } else if (!table.apply(this, key, entry.getValue())) {
                    warnUnknownPatchKey(key);
                }
            } catch (Exception e) {
                PeakLog.warn("Could not apply patch key '{}' on {} '{}': {}", key, getClass().getSimpleName(), id, e.toString());
            }
        }
        if (releaseBindings) releaseBoundFields(patch);
        syncInteraction();
        afterPatch(patch);
    }

    protected Set<String> customPatchKeys() {
        return BASE_CUSTOM_PATCH_KEYS;
    }

    protected void applyCustomPatch(String key, JsonElement value) {
        if (key.equals("bindings")) {
            if (bindings == null) bindings = new LinkedHashMap<>();
            value.getAsJsonObject().entrySet().forEach(e -> {
                bindings.put(e.getKey(), e.getValue().getAsString());
                if (releasedBindings != null) releasedBindings.remove(e.getKey());
            });
        } else if (key.equals("unbindings") && bindings != null) {
            value.getAsJsonArray().forEach(e -> bindings.remove(e.getAsString()));
        }
    }

    protected void beforePatch(JsonObject patch) {}

    protected void afterPatch(JsonObject patch) {}

    public final boolean isPatchKeyKnown(String key) {
        if (key.indexOf('.') >= 0) return PeakFieldTable.hasPath(getClass(), key);
        return customPatchKeys().contains(key) || PeakFieldTable.of(getClass()).has(key);
    }

    private void warnUnknownPatchKey(String key) {
        String description = getClass().getSimpleName() + "." + key;
        if (WARNED_PATCH_KEYS.add(description)) {
            PeakLog.warn("Patch key '{}' does not exist on {} (element '{}') and was ignored", key, getClass().getSimpleName(), id);
        }
    }
}
