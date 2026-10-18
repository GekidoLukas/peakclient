package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakClientApi;
import net.gekidolukas.peakclient.api.PeakClientApiHolder;
import net.gekidolukas.peakclient.network.PeakMenuPayload;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PeakMenusTest {
    private RecordingPeakApi api;
    private PeakClientApi previous;
    private final UUID player = UUID.randomUUID();

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
    void sendingAMenuReachesTheApi() {
        PeakDocument menu = PeakMenu.create("shop");

        PeakClient.to(player).menus().send(menu);

        assertEquals(1, api.calls.size());
        assertEquals("sendMenu", api.calls.get(0).name());
        assertSame(menu, api.calls.get(0).payload());
        assertEquals(List.of(player), api.calls.get(0).ids());
    }

    @Test
    void aPlainDocumentIsNotAMenu() {
        assertThrows(IllegalArgumentException.class,
                () -> PeakClient.to(player).menus().send(PeakDocument.create("hud")));
    }

    @Test
    void updatesAreSentAsOperationsForTheMenu() {
        PeakClient.to(player).menus().update("shop", "title", PeakPatch.label().text("Sale"));

        assertEquals("menuUpdate:shop", api.calls.get(0).name());
        @SuppressWarnings("unchecked")
        List<PeakUiOperation> operations = (List<PeakUiOperation>) api.calls.get(0).payload();
        PeakUiOperation.Update update = assertInstanceOf(PeakUiOperation.Update.class, operations.get(0));
        assertEquals("title", update.elementId());
        assertEquals("Sale", update.patch().get("text").getAsString());
    }

    @Test
    void openingCarriesTheFlags() {
        PeakClient.to(player).menus().open("shop");
        PeakClient.to(player).menus().open("shop", true, true);

        assertEquals("menu:OPEN:shop", api.calls.get(0).name());
        assertEquals(0, api.calls.get(0).payload());
        assertEquals(PeakMenuPayload.FLAG_REPLACE | PeakMenuPayload.FLAG_FORCE, api.calls.get(1).payload());
    }

    @Test
    void sendAndOpenSendsTheMenuFirst() {
        PeakClient.to(player).menus().sendAndOpen(PeakMenu.create("shop"));

        assertEquals("sendMenu", api.calls.get(0).name());
        assertEquals("menu:OPEN:shop", api.calls.get(1).name());
    }

    @Test
    void navigationCommandsReachTheApi() {
        PeakClient.to(player).menus().back().close().remove("shop").clear();

        assertEquals("menu:BACK:", api.calls.get(0).name());
        assertEquals("menu:CLOSE:", api.calls.get(1).name());
        assertEquals("menu:REMOVE:shop", api.calls.get(2).name());
        assertEquals("menu:CLEAR:", api.calls.get(3).name());
    }
}
