package net.gekidolukas.peakclient.ui.serialize;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.gekidolukas.peakclient.ui.PeakBarChart;
import net.gekidolukas.peakclient.ui.PeakBlockElement;
import net.gekidolukas.peakclient.ui.PeakBlockSource;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakChibiPlayer;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakCompass;
import net.gekidolukas.peakclient.ui.PeakAnimated;
import net.gekidolukas.peakclient.ui.PeakCarousel;
import net.gekidolukas.peakclient.ui.PeakConditional;
import net.gekidolukas.peakclient.ui.PeakCycler;
import net.gekidolukas.peakclient.ui.PeakDiv;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEntityElement;
import net.gekidolukas.peakclient.ui.PeakEntitySource;
import net.gekidolukas.peakclient.ui.PeakIconBar;
import net.gekidolukas.peakclient.ui.PeakImage;
import net.gekidolukas.peakclient.ui.PeakItemElement;
import net.gekidolukas.peakclient.ui.PeakItemSlot;
import net.gekidolukas.peakclient.ui.PeakItemSource;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMinimap;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakPieChart;
import net.gekidolukas.peakclient.ui.PeakPlayerIcon;
import net.gekidolukas.peakclient.ui.PeakPopUpContainer;
import net.gekidolukas.peakclient.ui.PeakFeed;
import net.gekidolukas.peakclient.ui.PeakFlow;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakShape;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.PeakStagger;
import net.gekidolukas.peakclient.ui.PeakProgressBar;
import net.gekidolukas.peakclient.ui.PeakRadialProgress;
import net.gekidolukas.peakclient.ui.PeakRadioGroup;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakSpacer;
import net.gekidolukas.peakclient.ui.PeakSpinner;
import net.gekidolukas.peakclient.ui.PeakTableLayoutPanel;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTimer;
import net.gekidolukas.peakclient.ui.PeakTooltipLabel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PeakElementTypeAdapterFactory implements TypeAdapterFactory {

    private static final Map<String, Class<? extends PeakElement<?>>> BY_NAME = new HashMap<>();
    private static final Map<Class<?>, String> BY_CLASS = new HashMap<>();
    private static final Map<Class<?>, Supplier<PeakElement<?>>> PROTOTYPES = new HashMap<>();
    private static final Set<String> KEPT_KEYS = Set.of("type", "id", "version");

    static {
        register("document", PeakDocument.class, () -> new PeakDocument(""));
        register("div", PeakDiv.class, () -> new PeakDiv(""));
        register("label", PeakLabel.class, () -> new PeakLabel("", ""));
        register("image", PeakImage.class, () -> new PeakImage("", ""));
        register("panel", PeakPanel.class, () -> new PeakPanel(""));
        register("button", PeakButton.class, () -> new PeakButton(""));
        register("text_box", PeakTextBox.class, () -> new PeakTextBox(""));
        register("rich_text_box", PeakRichTextBox.class, () -> new PeakRichTextBox(""));
        register("checkbox", PeakCheckbox.class, () -> new PeakCheckbox(""));
        register("slider", PeakSlider.class, () -> new PeakSlider(""));
        register("color_picker", PeakColorPicker.class, () -> new PeakColorPicker(""));
        register("radio_group", PeakRadioGroup.class, () -> new PeakRadioGroup(""));
        register("number_field", PeakNumberField.class, () -> new PeakNumberField(""));
        register("dropdown", PeakDropdown.class, () -> new PeakDropdown(""));
        register("item_slot", PeakItemSlot.class, () -> new PeakItemSlot("", PeakItemSource.inventorySlot(0)));
        register("spinner", PeakSpinner.class, () -> new PeakSpinner(""));
        register("table", PeakTableLayoutPanel.class, () -> new PeakTableLayoutPanel(""));
        register("item", PeakItemElement.class, () -> new PeakItemElement("", PeakItemSource.inventorySlot(0)));
        register("timer", PeakTimer.class, () -> new PeakTimer("", 0L));
        register("entity", PeakEntityElement.class, () -> new PeakEntityElement("", PeakEntitySource.player()));
        register("block", PeakBlockElement.class, () -> new PeakBlockElement("", PeakBlockSource.targetedBlock()));
        register("progress_bar", PeakProgressBar.class, () -> new PeakProgressBar(""));
        register("radial_progress", PeakRadialProgress.class, () -> new PeakRadialProgress(""));
        register("tooltip_label", PeakTooltipLabel.class, () -> new PeakTooltipLabel("", ""));
        register("icon_bar", PeakIconBar.class, () -> new PeakIconBar(""));
        register("spacer", PeakSpacer.class, () -> new PeakSpacer("", 0, 0));
        register("compass", PeakCompass.class, () -> new PeakCompass(""));
        register("conditional", PeakConditional.class, () -> new PeakConditional(""));
        register("popup", PeakPopUpContainer.class, () -> new PeakPopUpContainer(""));
        register("carousel", PeakCarousel.class, () -> new PeakCarousel(""));
        register("cycler", PeakCycler.class, () -> new PeakCycler(""));
        register("animated", PeakAnimated.class, () -> new PeakAnimated(""));
        register("stagger", PeakStagger.class, () -> new PeakStagger(""));
        register("feed", PeakFeed.class, () -> new PeakFeed(""));
        register("sorted_list", PeakSortedList.class, () -> new PeakSortedList(""));
        register("reel", PeakReel.class, () -> new PeakReel(""));
        register("flow", PeakFlow.class, () -> new PeakFlow(""));
        register("world_anchor", PeakWorldAnchor.class, () -> new PeakWorldAnchor(""));
        register("shape", PeakShape.class, () -> new PeakShape(""));
        register("minimap", PeakMinimap.class, () -> new PeakMinimap("", ""));
        register("player_icon", PeakPlayerIcon.class, () -> PeakPlayerIcon.clientPlayer(""));
        register("bar_chart", PeakBarChart.class, () -> new PeakBarChart(""));
        register("pie_chart", PeakPieChart.class, () -> new PeakPieChart(""));
        register("chibi_player", PeakChibiPlayer.class, PeakChibiPlayer::clientPlayer);
    }

    private static void register(String name, Class<? extends PeakElement<?>> clazz, Supplier<PeakElement<?>> prototype) {
        BY_NAME.put(name, clazz);
        BY_CLASS.put(clazz, name);
        PROTOTYPES.put(clazz, prototype);
    }

    public static Set<Class<?>> registeredClasses() {
        return Set.copyOf(BY_CLASS.keySet());
    }

    private final boolean prune;
    private final Map<Class<?>, JsonObject> prototypeJson = new ConcurrentHashMap<>();

    public PeakElementTypeAdapterFactory(boolean prune) {
        this.prune = prune;
    }

    private JsonObject prototypeOf(Class<?> clazz, TypeAdapter<PeakElement<?>> delegate) {
        JsonObject cached = prototypeJson.get(clazz);
        if (cached != null) return cached;
        JsonObject created = delegate.toJsonTree(PROTOTYPES.get(clazz).get()).getAsJsonObject();
        prototypeJson.put(clazz, created);
        return created;
    }

    private static void pruneDefaults(JsonObject object, JsonObject defaults, boolean topLevel) {
        for (String key : new ArrayList<>(object.keySet())) {
            if (topLevel && KEPT_KEYS.contains(key)) continue;
            JsonElement defaultValue = defaults.get(key);
            if (defaultValue == null) continue;
            JsonElement value = object.get(key);
            if (value.equals(defaultValue)) {
                object.remove(key);
            } else if (value.isJsonObject() && defaultValue.isJsonObject()) {
                pruneDefaults(value.getAsJsonObject(), defaultValue.getAsJsonObject(), false);
            }
        }
    }

    private static void overlay(JsonObject base, JsonObject over) {
        for (Map.Entry<String, JsonElement> entry : over.entrySet()) {
            JsonElement existing = base.get(entry.getKey());
            JsonElement value = entry.getValue();
            if (existing != null && existing.isJsonObject() && value.isJsonObject()) {
                overlay(existing.getAsJsonObject(), value.getAsJsonObject());
            } else {
                base.add(entry.getKey(), value);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!PeakElement.class.isAssignableFrom(type.getRawType())) {
            return null;
        }

        return (TypeAdapter<T>) new TypeAdapter<PeakElement<?>>() {
            @Override
            public void write(JsonWriter out, PeakElement<?> value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                Class<?> actualClass = value.getClass();
                String typeName = BY_CLASS.get(actualClass);
                if (typeName == null) {
                    throw new JsonParseException("Unknown Element Type: " + actualClass);
                }

                TypeAdapter<PeakElement<?>> delegate =
                        (TypeAdapter<PeakElement<?>>) gson.getDelegateAdapter(
                                PeakElementTypeAdapterFactory.this, TypeToken.get(actualClass));

                JsonObject tree = delegate.toJsonTree(value).getAsJsonObject();
                if (prune) pruneDefaults(tree, prototypeOf(actualClass, delegate), true);
                JsonObject wrapped = new JsonObject();
                wrapped.addProperty("type", typeName);
                for (var entry : tree.entrySet()) {
                    wrapped.add(entry.getKey(), entry.getValue());
                }
                gson.getAdapter(JsonObject.class).write(out, wrapped);
            }

            @Override
            public PeakElement<?> read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                JsonObject tree = gson.getAdapter(JsonObject.class).read(in);
                String typeName = tree.get("type").getAsString();
                Class<? extends PeakElement<?>> clazz = BY_NAME.get(typeName);
                if (clazz == null) {
                    throw new JsonParseException("Unknown Element Type: " + typeName);
                }

                TypeAdapter<PeakElement<?>> delegate =
                        (TypeAdapter<PeakElement<?>>) gson.getDelegateAdapter(
                                PeakElementTypeAdapterFactory.this, TypeToken.get(clazz));

                JsonObject merged = prototypeOf(clazz, delegate).deepCopy();
                overlay(merged, tree);
                return delegate.fromJsonTree(merged);
            }
        };
    }
}
