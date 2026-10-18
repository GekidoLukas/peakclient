package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakClientApiHolder;
import net.gekidolukas.peakclient.api.PeakTargets;
import net.gekidolukas.peakclient.server.PeakClientReadyEvent;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class PeakTargetsTest {
    private RecordingPeakApi api;
    private PeakClientApi previous;

    @BeforeEach
    void install() {
        previous = PeakClientApiHolder.INSTANCE;
        api = new RecordingPeakApi();
        PeakClientApiHolder.INSTANCE = api;
    }

    @AfterEach
    void restore() {
        PeakClientApiHolder.INSTANCE = previous;
    }

    @Test
    void sendUiReachesTheGivenPlayer() {
        UUID id = UUID.randomUUID();
        PeakDocument doc = PeakDocument.create("d");
        PeakClient.to(id).sendUi(doc);
        assertEquals(1, api.calls.size());
        assertEquals("sendUi", api.calls.get(0).name());
        assertSame(doc, api.calls.get(0).payload());
        assertEquals(List.of(id), api.calls.get(0).ids());
    }

    @Test
    void collectionTargetsAreDeduplicatedAndKeepTheirOrder() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        PeakClient.to(List.of(a, b, a)).clearWaypoints();
        assertEquals(List.of(a, b), api.calls.get(0).ids());
    }

    @Test
    void collectionTargetsRejectOtherTypes() {
        assertThrows(IllegalArgumentException.class, () -> PeakClient.to(List.of("not a player")));
        List<Object> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> PeakClient.to(withNull));
    }

    @Test
    void updateElementBuildsAnUpdateOperation() {
        UUID id = UUID.randomUUID();
        PeakClient.to(id).updateElement("doc", "panel", PeakPatch.panel().background(0xFF0000));
        RecordingPeakApi.Call call = api.calls.get(0);
        assertEquals("update:doc", call.name());
        @SuppressWarnings("unchecked")
        List<PeakUiOperation> operations = (List<PeakUiOperation>) call.payload();
        PeakUiOperation.Update update = assertInstanceOf(PeakUiOperation.Update.class, operations.get(0));
        assertEquals("panel", update.elementId());
        assertTrue(update.patch().has("style.background"));
    }

    @Test
    void removeUiSendsAnEmptyDeleteIfEmptyDocument() {
        PeakClient.to(UUID.randomUUID()).removeUi("doc");
        PeakDocument sent = (PeakDocument) api.calls.get(0).payload();
        assertEquals("doc", sent.getId());
        assertTrue(sent.isDeleteIfEmpty());
        assertTrue(sent.getChildren().isEmpty());
    }

    @Test
    void valuesUseTheKeyFormatAndRootLocale() {
        PeakClient.to(UUID.randomUUID()).setValue(PeakValueKey.ofInt("mygame:kills"), 5);
        assertEquals("value:mygame:kills", api.calls.get(0).name());
        assertEquals("5", api.calls.get(0).payload());

        PeakClient.to(UUID.randomUUID()).setValue("mygame:ratio", 1.5, "%.2f");
        assertEquals("1.50", api.calls.get(1).payload());

        PeakClient.to(UUID.randomUUID()).setValue("mygame:alive", true);
        assertEquals("true", api.calls.get(2).payload());

        PeakClient.to(UUID.randomUUID()).clearValue("mygame:alive");
        assertEquals("clearValue:mygame:alive", api.calls.get(3).name());

        PeakClient.to(UUID.randomUUID()).clearValues("mygame");
        assertEquals("clearNamespace:mygame", api.calls.get(4).name());
    }

    @Test
    void malformedValueKeysAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PeakClient.to(UUID.randomUUID()).setValue("nocolon", "x"));
    }

    @Test
    void hideVanillaHudDisablesTheGivenElements() {
        PeakClient.to(UUID.randomUUID()).vanillaHud().hide(PeakVanillaElement.HOTBAR);
        @SuppressWarnings("unchecked")
        Map<PeakVanillaElement, PeakVanillaHudEntry> entries = (Map<PeakVanillaElement, PeakVanillaHudEntry>) api.calls.get(0).payload();
        assertFalse(entries.get(PeakVanillaElement.HOTBAR).isEnabled());
    }

    @Test
    void timerHelpersPatchTheTimer() {
        PeakClient.to(UUID.randomUUID()).syncTimer("doc", "timer", 200L, true);
        @SuppressWarnings("unchecked")
        List<PeakUiOperation> operations = (List<PeakUiOperation>) api.calls.get(0).payload();
        PeakUiOperation.Update update = assertInstanceOf(PeakUiOperation.Update.class, operations.get(0));
        assertEquals("timer", update.elementId());
        assertTrue(update.patch().get("running").getAsBoolean());
    }

    @Test
    void elementOverloadsUseTheElementId() {
        PeakLabel label = PeakLabel.create("score", "0");
        PeakDocument doc = PeakDocument.create("hud");
        PeakClient.to(UUID.randomUUID()).updateElement(doc, label, PeakPatch.label().text("1"));
        PeakClient.to(UUID.randomUUID()).deleteElement("hud", label);
        @SuppressWarnings("unchecked")
        List<PeakUiOperation> update = (List<PeakUiOperation>) api.calls.get(0).payload();
        assertEquals("score", assertInstanceOf(PeakUiOperation.Update.class, update.get(0)).elementId());
        assertEquals("update:hud", api.calls.get(0).name());
        @SuppressWarnings("unchecked")
        List<PeakUiOperation> delete = (List<PeakUiOperation>) api.calls.get(1).payload();
        assertEquals("score", assertInstanceOf(PeakUiOperation.Delete.class, delete.get(0)).elementId());
    }

    @Test
    void typedReferencesBuildTheMatchingPatch() {
        PeakRef<PeakLabel, net.gekidolukas.peakclient.ui.patch.PeakLabelPatch> score = PeakRef.label("score");
        PeakClient.to(UUID.randomUUID()).updateElement("hud", score, patch -> patch.text("3 : 1").shadow(false));
        @SuppressWarnings("unchecked")
        List<PeakUiOperation> operations = (List<PeakUiOperation>) api.calls.get(0).payload();
        PeakUiOperation.Update update = assertInstanceOf(PeakUiOperation.Update.class, operations.get(0));
        assertEquals("score", update.elementId());
        assertEquals("3 : 1", update.patch().get("text").getAsString());
        assertFalse(update.patch().get("shadow").getAsBoolean());
    }

    @Test
    void referencesCreateElementsWithTheirId() {
        PeakRef<PeakLabel, net.gekidolukas.peakclient.ui.patch.PeakLabelPatch> score = PeakRef.label("score");
        PeakLabel label = score.create(id -> PeakLabel.create(id, "0"));
        assertEquals("score", label.getId());
        assertThrows(IllegalArgumentException.class, () -> score.create(id -> PeakLabel.create("other", "0")));
        assertThrows(IllegalArgumentException.class, () -> PeakRef.label(" "));
    }

    @Test
    void reachableIsEmptyWithoutAServerOrClient() {
        PeakTargets targets = PeakClient.to(List.of(UUID.randomUUID(), UUID.randomUUID()));
        assertEquals(2, targets.ids().size());
        assertTrue(targets.reachable().isEmpty());
        assertEquals(0, targets.count());
    }

    @Test
    void missingApiGivesAClearMessage() {
        PeakClientApiHolder.INSTANCE = null;
        assertFalse(PeakClient.isAvailable());
        IllegalStateException error = assertThrows(IllegalStateException.class, PeakClientApi::get);
        assertTrue(error.getMessage().contains("not initialized"));
    }
}
