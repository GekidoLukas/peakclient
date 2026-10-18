package net.gekidolukas.peakclient.ui.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.gekidolukas.peakclient.ui.PeakUiOperation;

import java.lang.reflect.Type;

public final class PeakUiOperationAdapter implements JsonSerializer<PeakUiOperation>, JsonDeserializer<PeakUiOperation> {

    @Override
    public JsonElement serialize(PeakUiOperation src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json;
        String operationType;
        if (src instanceof PeakUiOperation.Update update) {
            json = (JsonObject) context.serialize(update, PeakUiOperation.Update.class);
            operationType = "update";
        } else if (src instanceof PeakUiOperation.UpdateByClass updateByClass) {
            json = (JsonObject) context.serialize(updateByClass, PeakUiOperation.UpdateByClass.class);
            operationType = "updateByClass";
        }  else if (src instanceof PeakUiOperation.Delete delete) {
            json = (JsonObject) context.serialize(delete, PeakUiOperation.Delete.class);
            operationType = "delete";
        } else if (src instanceof PeakUiOperation.Add add) {
            json = (JsonObject) context.serialize(add, PeakUiOperation.Add.class);
            operationType = "add";
        } else {
            throw new JsonParseException("Unknown operation: " + src);
        }
        json.addProperty("op", operationType);
        return json;
    }

    @Override
    public PeakUiOperation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        String operationType = json.getAsJsonObject().get("op").getAsString();
        return switch (operationType) {
            case "update" -> context.deserialize(json, PeakUiOperation.Update.class);
            case "updateByClass" -> context.deserialize(json, PeakUiOperation.UpdateByClass.class);
            case "delete" -> context.deserialize(json, PeakUiOperation.Delete.class);
            case "add" -> context.deserialize(json, PeakUiOperation.Add.class);
            default -> throw new JsonParseException("Unknown operation: " + operationType);
        };
    }
}
