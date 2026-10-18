package net.gekidolukas.peakclient.ui.patch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakAlign;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakConstraints;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakInsets;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakReaction;
import net.gekidolukas.peakclient.ui.PeakPosition;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakScrollbarStyle;
import net.gekidolukas.peakclient.ui.PeakSize;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.util.List;

public abstract class PeakBasePatch<SELF extends PeakBasePatch<SELF>> {
    protected final JsonObject data = new JsonObject();

    protected abstract SELF self();

    public SELF position(PeakPosition position) { data.add("position", PeakUiSerializer.GSON.toJsonTree(position)); return self(); }
    public SELF positionMode(PeakPositionMode mode) { data.addProperty("positionMode", mode.name()); return self(); }
    public SELF absolute(PeakPosition position) { position(position); return positionMode(PeakPositionMode.ABSOLUTE); }
    public SELF absolute(PeakAnchor anchor, int offsetX, int offsetY) { return absolute(PeakPosition.of(anchor, offsetX, offsetY)); }
    public SELF absolute(PeakAnchor anchor) { return absolute(PeakPosition.of(anchor)); }
    public SELF size(PeakSize size) { data.add("size", PeakUiSerializer.GSON.toJsonTree(size)); return self(); }
    public SELF size(int width, int height) { return size(PeakSize.pixels(width, height)); }
    public SELF width(int pixels) { return field("size.width", PeakSize.PeakDimension.pixels(pixels)); }
    public SELF height(int pixels) { return field("size.height", PeakSize.PeakDimension.pixels(pixels)); }
    public SELF widthPercent(float percent) { return field("size.width", PeakSize.PeakDimension.percent(percent)); }
    public SELF heightPercent(float percent) { return field("size.height", PeakSize.PeakDimension.percent(percent)); }
    public SELF style(PeakStyle style) { data.add("style", PeakUiSerializer.GSON.toJsonTree(style)); return self(); }
    public SELF sortKey(double sortKey) { data.addProperty("sortKey", sortKey); return self(); }
    public SELF background(PeakPaint paint) { return field("style.background", paint); }
    public SELF background(int value) { data.addProperty("style.background", value); return self(); }
    public SELF padding(int all) { return field("style.padding", PeakInsets.all(all)); }
    public SELF padding(int vertical, int horizontal) { return field("style.padding", PeakInsets.of(vertical, horizontal, vertical, horizontal)); }
    public SELF padding(int top, int right, int bottom, int left) { return field("style.padding", PeakInsets.of(top, right, bottom, left)); }
    public SELF margin(int all) { return field("style.margin", PeakInsets.all(all)); }
    public SELF margin(int vertical, int horizontal) { return field("style.margin", PeakInsets.of(vertical, horizontal, vertical, horizontal)); }
    public SELF margin(int top, int right, int bottom, int left) { return field("style.margin", PeakInsets.of(top, right, bottom, left)); }
    public SELF border(int width, PeakPaint paint) {
        data.addProperty("style.borderWidth", width);
        return field("style.borderColor", paint);
    }
    public SELF border(int width, int argb) {
        data.addProperty("style.borderWidth", width);
        data.addProperty("style.borderColor", argb);
        return self();
    }
    public SELF borderRadius(int radius) { data.addProperty("style.borderRadius", Math.max(0, radius)); return self(); }
    public SELF boxShadow(PeakPaint paint, int offsetX, int offsetY, int blur) {
        field("style.shadowColor", paint);
        data.addProperty("style.shadowOffsetX", offsetX);
        data.addProperty("style.shadowOffsetY", offsetY);
        data.addProperty("style.shadowBlur", Math.max(0, blur));
        return self();
    }
    public SELF boxShadow(int argb, int offsetX, int offsetY, int blur) {
        data.addProperty("style.shadowColor", argb);
        data.addProperty("style.shadowOffsetX", offsetX);
        data.addProperty("style.shadowOffsetY", offsetY);
        data.addProperty("style.shadowBlur", Math.max(0, blur));
        return self();
    }
    public SELF field(String path, Object value) {
        data.add(path, PeakUiSerializer.GSON.toJsonTree(value));
        return self();
    }
    public SELF interaction(PeakInteraction interaction) { return field("interaction", interaction); }
    public SELF enabled(boolean enabled) { data.addProperty("interaction.enabled", enabled); return self(); }
    public SELF tooltip(String tooltip) {
        data.addProperty("interaction.tooltip", tooltip);
        data.addProperty("interaction.elementTooltip", false);
        return self();
    }
    public SELF tooltip(PeakElement<?> element) {
        data.addProperty("interaction.tooltip", "");
        data.addProperty("interaction.elementTooltip", true);
        return field("interaction.tooltipElement", element);
    }
    public SELF events(PeakReaction... reactions) { return field("events", List.of(reactions)); }
    public SELF clearEvents() { return field("events", List.of()); }
    public SELF onClick(PeakAction... actions) { return events(PeakReaction.of(PeakEvent.CLICK).then(actions)); }
    public SELF colSpan(int columns) { data.addProperty("columnSpan", Math.max(1, columns)); return self(); }
    public SELF rowSpan(int rows) { data.addProperty("rowSpan", Math.max(1, rows)); return self(); }
    public SELF scrollbar(PeakScrollbarStyle style) { return field("scrollbar", style); }
    public SELF scrollbarVisible(boolean visible) { data.addProperty("scrollbar.visible", visible); return self(); }
    public SELF scrollbarThumbColor(int color) { data.addProperty("scrollbar.thumbColor", color); return self(); }
    public SELF scrollbarThumbColor(PeakPaint paint) { return field("scrollbar.thumbColor", paint); }
    public SELF constraints(PeakConstraints constraints) { data.add("constraints", PeakUiSerializer.GSON.toJsonTree(constraints)); return self(); }
    public SELF alignSelf(PeakAlign align) { data.addProperty("alignSelf", align.name()); return self(); }
    public SELF verticalAlign(PeakAlign align) { data.addProperty("verticalAlign", align.name()); return self(); }
    public SELF scale(float scale) { data.addProperty("scale", scale); return self(); }
    public SELF hidden(boolean hidden) { data.addProperty("hidden", hidden); return self(); }
    public SELF collapseWhenHidden(boolean collapse) { data.addProperty("collapseWhenHidden", collapse); return self(); }
    public SELF opacity(float opacity) { data.addProperty("opacity", opacity); return self(); }
    public SELF rotationDegrees(float degrees) { data.addProperty("rotationDegrees", degrees); return self(); }
    public SELF bind(String field, String expression) {
        JsonObject bindings = data.has("bindings") ? data.getAsJsonObject("bindings") : new JsonObject();
        bindings.addProperty(field, expression);
        data.add("bindings", bindings);
        return self();
    }
    public SELF unbind(String field) {
        JsonArray unbindings = data.has("unbindings") ? data.getAsJsonArray("unbindings") : new JsonArray();
        unbindings.add(field);
        data.add("unbindings", unbindings);
        return self();
    }
    public SELF classes(String... classNames) {
        JsonArray classNamesJson = new JsonArray();
        for (String className : classNames) classNamesJson.add(className);
        data.add("classes", classNamesJson);
        return self();
    }

    public JsonObject build() { return data; }
}
