package net.gekidolukas.peakclient.ui.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.util.List;

public final class PeakUiSerializer {
    public static final Gson GSON = build(false);
    public static final Gson WIRE = build(true);

    private PeakUiSerializer() {}

    private static Gson build(boolean pruneDefaults) {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new PeakElementTypeAdapterFactory(pruneDefaults))
                .registerTypeAdapter(PeakUiOperation.class, new PeakUiOperationAdapter())
                .registerTypeAdapter(ItemStack.class, new PeakItemStackAdapter())
                .registerTypeHierarchyAdapter(PeakPaint.class, new PeakPaintAdapter())
                .registerTypeHierarchyAdapter(Component.class, new PeakComponentAdapter())
                .create();
    }

    public static String toJson(PeakDocument document) {
        return WIRE.toJson(document, PeakElement.class);
    }

    public static PeakDocument documentFromJson(String json) {
        return (PeakDocument) GSON.fromJson(json, PeakElement.class);
    }

    public static String operationsToJson(List<PeakUiOperation> operations) {
        Type listType = new TypeToken<List<PeakUiOperation>>() {}.getType();
        return WIRE.toJson(operations, listType);
    }

    public static List<PeakUiOperation> operationsFromJson(String json) {
        Type listType = new TypeToken<List<PeakUiOperation>>() {}.getType();
        return GSON.fromJson(json, listType);
    }
}
