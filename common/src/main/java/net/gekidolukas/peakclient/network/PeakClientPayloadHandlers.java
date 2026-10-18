package net.gekidolukas.peakclient.network;

import com.google.gson.reflect.TypeToken;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.PeakVanillaHudEntry;
import net.gekidolukas.peakclient.ui.PeakWaypoint;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomWorldBorderState;
import net.gekidolukas.peakclient.ui.client.PeakClientMenus;
import net.gekidolukas.peakclient.ui.client.PeakClientUiState;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import net.gekidolukas.peakclient.ui.client.PeakClientWaypointRegistry;
import net.gekidolukas.peakclient.ui.serialize.PeakGzip;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public final class PeakClientPayloadHandlers {
    private static final Type VANILLA_HUD_TYPE =
            new TypeToken<Map<PeakVanillaElement, PeakVanillaHudEntry>>() {}.getType();

    private PeakClientPayloadHandlers() {}

    private static void safe(Runnable action, String what) {
        try {
            action.run();
        } catch (Exception e) {
            PeakLog.error("Failed to process " + what, e);
        }
    }

    public static void onDocument(PeakUiDocumentPayload payload) {
        safe(() -> {
            String json = PeakGzip.decompress(payload.gzippedJson());
            PeakDocument document = PeakUiSerializer.documentFromJson(json);
            PeakClientUiState.applyDocument(document);
        }, "a received UI document");
    }

    public static void onUpdate(PeakUiUpdatePayload payload) {
        safe(() -> {
            String json = PeakGzip.decompress(payload.gzippedJson());
            List<PeakUiOperation> operations = PeakUiSerializer.operationsFromJson(json);
            PeakClientUiState.applyOperations(payload.documentId(), operations);
        }, "a received UI update");
    }

    public static void onVanillaHud(PeakVanillaHudPayload payload) {
        safe(() -> {
            String json = PeakGzip.decompress(payload.gzippedJson());
            Map<PeakVanillaElement, PeakVanillaHudEntry> updates = PeakUiSerializer.GSON.fromJson(json, VANILLA_HUD_TYPE);
            PeakClientVanillaHudState.apply(updates);
        }, "a vanilla HUD update");
    }

    public static void onCustomValueSet(PeakCustomValueSetPayload payload) {
        safe(() -> PeakClientCustomValues.set(payload.namespace(), payload.key(), payload.value()), "a custom value update");
    }

    public static void onCustomValueClear(PeakCustomValueClearPayload payload) {
        safe(() -> {
            if (payload.key().isEmpty()) {
                PeakClientCustomValues.clearNamespace(payload.namespace());
            } else {
                PeakClientCustomValues.clear(payload.namespace(), payload.key());
            }
        }, "a custom value clear");
    }

    public static void onWorldBorderCurrent(PeakCustomWorldBorderCurrentPayload payload) {
        safe(() -> PeakClientCustomWorldBorderState.setCurrent(payload.minX(), payload.minZ(), payload.maxX(), payload.maxZ()),
                "a custom world border current update");
    }

    public static void onWorldBorderTarget(PeakCustomWorldBorderTargetPayload payload) {
        safe(() -> PeakClientCustomWorldBorderState.setTarget(payload.minX(), payload.minZ(), payload.maxX(), payload.maxZ(),
                payload.transitionTicks()), "a custom world border target update");
    }

    public static void onWorldBorderClear(PeakCustomWorldBorderClearPayload payload) {
        safe(PeakClientCustomWorldBorderState::clear, "clearing the custom world border");
    }

    public static void onMenu(PeakMenuPayload payload) {
        safe(() -> PeakClientMenus.receive(payload), "a menu packet");
    }

    public static void onWaypointUpsert(PeakWaypointUpsertPayload payload) {
        safe(() -> {
            String json = PeakGzip.decompress(payload.gzippedJson());
            PeakClientWaypointRegistry.upsert(PeakUiSerializer.GSON.fromJson(json, PeakWaypoint.class));
        }, "a waypoint upsert");
    }

    public static void onWaypointRemove(PeakWaypointRemovePayload payload) {
        safe(() -> PeakClientWaypointRegistry.remove(payload.waypointId()), "a waypoint remove");
    }

    public static void onWaypointClear(PeakWaypointClearPayload payload) {
        safe(PeakClientWaypointRegistry::clearAll, "a waypoint clear");
    }

    public static void onWaypointPosition(PeakWaypointPositionSyncPayload payload) {
        safe(() -> PeakClientWaypointRegistry.syncPosition(payload.waypointId(), payload.x(), payload.y(), payload.z()),
                "a waypoint position sync");
    }
}
