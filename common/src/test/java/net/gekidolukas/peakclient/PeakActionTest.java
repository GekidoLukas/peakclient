package net.gekidolukas.peakclient;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PeakActionTest {
    @Test
    void factoriesSetTypeAndTarget() {
        assertEquals(PeakActionType.CLOSE_MENU, PeakAction.close().type());
        assertEquals(PeakActionType.BACK, PeakAction.back().type());
        assertEquals("shop", PeakAction.open("shop").target());
        assertFalse(PeakAction.open("shop").replace());
        assertTrue(PeakAction.replaceWith("shop").replace());
        assertEquals("buy", PeakAction.send("buy").target());
    }

    @Test
    void argumentsKeepTheirOrder() {
        PeakAction action = PeakAction.send("buy").arg("item", "sword").arg("count", "3");

        assertEquals(Map.of("item", "sword", "count", "3"), action.args());
        assertEquals("item", action.args().keySet().iterator().next());
    }

    @Test
    void patchActionsCopyThePatch() {
        var patch = PeakPatch.label().text("first");
        PeakAction action = PeakAction.patch("title", patch);
        patch.text("second");

        assertEquals("first", action.patch().get("text").getAsString());
    }

    @Test
    void actionsSurviveASerializationRoundTrip() {
        PeakPanel panel = PeakPanel.create("test:buy")
                .onClick(PeakAction.send("buy").arg("item", "sword"), PeakAction.patch("title", PeakPatch.label().text("Bought")), PeakAction.close())
                .tooltip("Buy it");

        String json = PeakUiSerializer.GSON.toJson(panel);
        PeakPanel back = PeakUiSerializer.GSON.fromJson(json, PeakPanel.class);

        assertEquals(3, back.actionsFor(PeakEvent.CLICK).size());
        assertEquals(PeakActionType.SEND, back.actionsFor(PeakEvent.CLICK).get(0).type());
        assertEquals("sword", back.actionsFor(PeakEvent.CLICK).get(0).args().get("item"));
        JsonObject patch = back.actionsFor(PeakEvent.CLICK).get(1).patch();
        assertEquals("Bought", patch.get("text").getAsString());
        assertEquals(PeakActionType.CLOSE_MENU, back.actionsFor(PeakEvent.CLICK).get(2).type());
        assertEquals("Buy it", back.getInteraction().getTooltip());
        assertTrue(back.getInteraction().isEnabled());
    }
}
