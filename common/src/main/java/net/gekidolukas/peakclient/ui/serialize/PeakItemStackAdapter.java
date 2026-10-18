package net.gekidolukas.peakclient.ui.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;
import net.gekidolukas.peakclient.PeakClientServer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Supplier;

public final class PeakItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    private static volatile Supplier<HolderLookup.Provider> clientProvider = () -> null;

    public static void useClientProvider(Supplier<HolderLookup.Provider> supplier) {
        clientProvider = supplier == null ? () -> null : supplier;
    }

    private static HolderLookup.Provider provider() {
        MinecraftServer server = PeakClientServer.server();
        if (server != null) return server.registryAccess();
        return clientProvider.get();
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty("item", BuiltInRegistries.ITEM.getKey(src.getItem()).toString());
        itemJson.addProperty("count", src.getCount());

        HolderLookup.Provider provider = provider();
        if (provider != null && !src.isEmpty()) {
            RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
            Optional<JsonElement> encoded = ItemStack.CODEC.encodeStart(ops, src).result();
            encoded.ifPresent(json -> itemJson.add("stack", json));
        }
        return itemJson;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject itemJson = json.getAsJsonObject();

        HolderLookup.Provider provider = provider();
        if (provider != null && itemJson.has("stack")) {
            RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
            Optional<ItemStack> decoded = ItemStack.CODEC.parse(ops, itemJson.get("stack")).result();
            if (decoded.isPresent()) return decoded.get();
        }

        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemJson.get("item").getAsString()));
        int count = itemJson.has("count") ? itemJson.get("count").getAsInt() : 1;
        return new ItemStack(item, count);
    }
}
