package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakConfirmDialog;
import net.gekidolukas.peakclient.server.PeakMenuEvents;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakRef;
import net.gekidolukas.peakclient.ui.template.PeakChoiceList;
import net.gekidolukas.peakclient.ui.template.PeakPagedList;
import net.gekidolukas.peakclient.ui.template.PeakTabs;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakIdConventionTest {
    @Test
    void oneColonForTheNamespaceAndSlashesAfterwardsAreValid() {
        assertTrue(PeakId.isValid("mygame:shop/title"));
        assertTrue(PeakId.isValid("mygame:menu/shop/items/0"));
        assertTrue(PeakId.isValid("plain"));
        assertTrue(PeakId.isValid("with/slash"));
        assertTrue(PeakId.isValid(""));
        assertTrue(PeakId.isValid(PeakId.random()));
    }

    @Test
    void moreThanOneColonIsRejected() {
        assertFalse(PeakId.isValid("mygame:shop:title"));
        assertFalse(PeakId.isValid("mygame:shop/title:label"));
        assertFalse(PeakId.isValid("::"));
        assertFalse(PeakId.isValid(null));
    }

    @Test
    void elementsRejectInvalidIds() {
        assertThrows(IllegalArgumentException.class, () -> PeakLabel.create("mygame:shop:title", "x"));
        assertThrows(IllegalArgumentException.class, () -> PeakPanel.create("a:b:c"));
        assertThrows(IllegalArgumentException.class, () -> PeakDocument.create("mygame:hud:main"));
        assertDoesNotThrow(() -> PeakLabel.create("mygame:shop/title", "x"));
    }

    @Test
    void anInvalidIdMessageExplainsTheConvention() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> PeakPanel.create("a:b:c"));
        assertTrue(error.getMessage().contains("'/'"));
        assertTrue(error.getMessage().contains("a:b:c"));
    }

    @Test
    void actionsRejectInvalidTargets() {
        assertThrows(IllegalArgumentException.class, () -> PeakAction.send("mygame:shop:buy"));
        assertThrows(IllegalArgumentException.class, () -> PeakAction.open("mygame:shop:more"));
        assertThrows(IllegalArgumentException.class, () -> PeakAction.replaceWith("a:b:c"));
        assertDoesNotThrow(() -> PeakAction.send("mygame:buy"));
        assertDoesNotThrow(() -> PeakAction.copyText("a:b:c"));
    }

    @Test
    void referencesHandlersAndValueKeysAreChecked() {
        assertThrows(IllegalArgumentException.class, () -> PeakRef.label("a:b:c"));
        assertThrows(IllegalArgumentException.class, () -> PeakMenuEvents.on("a:b:c", "buy", event -> {}));
        assertThrows(IllegalArgumentException.class, () -> PeakMenuEvents.on("mygame:shop", "a:b:c", event -> {}));
        assertThrows(IllegalArgumentException.class, () -> PeakValueKey.ofInt("mygame:stats:kills"));
        assertDoesNotThrow(() -> PeakValueKey.ofInt("mygame:stats/kills"));
    }

    @Test
    void templatesRejectInvalidIdsAndGenerateValidChildIds() {
        assertThrows(IllegalArgumentException.class, () -> PeakTabs.create("a:b:c"));
        assertThrows(IllegalArgumentException.class, () -> PeakChoiceList.create("a:b:c", "pick"));
        assertThrows(IllegalArgumentException.class, () -> PeakPagedList.create("a:b:c", 3));
        assertThrows(IllegalArgumentException.class, () -> PeakConfirmDialog.create("a:b:c"));

        List<String> ids = new ArrayList<>();
        PeakPagedList list = PeakPagedList.create("mygame:list", 2);
        for (int i = 0; i < 5; i++) list.add(PeakButton.text("mygame:list/item-" + i, "Item " + i));
        collect(list.build(), ids);
        collect(PeakTabs.create("mygame:tabs").tab("One", PeakLabel.create("mygame:tabs/a", "A"))
                .tab("Two", PeakLabel.create("mygame:tabs/b", "B")).build(), ids);
        collect(PeakChoiceList.create("mygame:choice", "pick").option("a", "A").option("b", "B").build(), ids);
        collect(PeakConfirmDialog.create("mygame:confirm").message("Sure?").build(), ids);

        assertFalse(ids.isEmpty());
        for (String id : ids) assertTrue(PeakId.isValid(id), id);
    }

    private static void collect(PeakElement<?> element, List<String> ids) {
        ids.add(element.getId());
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) collect(child, ids);
        }
    }
}
