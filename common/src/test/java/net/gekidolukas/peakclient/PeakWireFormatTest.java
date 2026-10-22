package net.gekidolukas.peakclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.serialize.PeakGzip;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakWireFormatTest {

    private static JsonElement full(PeakElement<?> element) {
        return PeakUiSerializer.GSON.toJsonTree(element);
    }

    private static PeakElement<?> viaWire(PeakElement<?> element) {
        String json = PeakUiSerializer.WIRE.toJson(element, PeakElement.class);
        return PeakUiSerializer.GSON.fromJson(json, PeakElement.class);
    }

    private static PeakDocument sampleDocument(int cards) {
        PeakDocument doc = PeakDocument.create("wire");
        for (int i = 0; i < cards; i++) {
            PeakPanel card = PeakPanel.create("card-" + i)
                    .absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 10 + i, 20))
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(22), PeakSize.PeakDimension.pixels(9)));
            card.getStyle().borderWidth(1).borderColor(0xFFFFFFFF).borderRadius(2)
                    .background(PeakPaint.gradient(0xFF2255AA, 0xFFAA5522));
            card.add(PeakLabel.create("label-" + i, String.valueOf(i)));
            doc.add(card);
        }
        return doc;
    }

    @Test
    void labelWithDefaultsSurvivesTheWireFormat() {
        PeakLabel label = PeakLabel.create("l", "Hello");
        PeakLabel back = (PeakLabel) viaWire(label);
        assertEquals(full(label), full(back));
        assertEquals(PeakPaint.solid(0xFFFFFF), back.getColor());
        assertTrue(back.hasShadow());
    }

    @Test
    void labelWithChangedValuesSurvivesTheWireFormat() {
        PeakLabel label = PeakLabel.create("l", "Hello").color(0x123456).shadow(false).wrapWidth(40);
        label.opacity(0.5f).hidden(true).collapseWhenHidden(false);
        label.getStyle().padding(3).background(0x80FF0000);
        PeakLabel back = (PeakLabel) viaWire(label);
        assertEquals(full(label), full(back));
        assertFalse(back.hasShadow());
        assertEquals(0.5f, back.getOpacity());
    }

    @Test
    void valueGoingBackToTheDefaultIsStillCorrect() {
        PeakLabel label = PeakLabel.create("l", "x").color(0x123456).color(0xFFFFFF);
        assertEquals(full(label), full(viaWire(label)));
    }

    @Test
    void otherElementTypesSurviveTheWireFormat() {
        List<PeakElement<?>> elements = List.of(
                PeakProgressBar.create("bar").maxValue(20).fillColor(0xFF112233),
                PeakSpacer.create("spacer", 5, 7),
                PeakImage.create("image", "minecraft:textures/gui/presets/isles.png"),
                PeakPanel.create("panel").gap(4),
                PeakDiv.create("div"));
        for (PeakElement<?> element : elements) {
            assertEquals(full(element), full(viaWire(element)), element.getClass().getSimpleName());
        }
    }

    @Test
    void documentWithChildrenSurvivesTheWireFormat() {
        PeakDocument doc = sampleDocument(5).documentTimeToLiveTicks(100);
        PeakDocument back = (PeakDocument) viaWire(doc);
        assertEquals(full(doc), full(back));
        assertEquals(doc.getVersion(), back.getVersion());
        assertNotNull(back.findById("label-3"));
    }

    @Test
    void hiddenVanillaElementsSurviveTheWireFormatAndAreOmittedWhenEmpty() {
        PeakDocument plain = sampleDocument(1);
        JsonObject plainJson = PeakUiSerializer.WIRE.toJsonTree(plain, PeakElement.class).getAsJsonObject();
        assertFalse(plainJson.has("hiddenVanilla"));

        PeakDocument doc = sampleDocument(1).hideVanilla(PeakVanillaElement.HOTBAR, PeakVanillaElement.CHAT);
        PeakDocument back = (PeakDocument) viaWire(doc);
        assertEquals(java.util.Set.of(PeakVanillaElement.HOTBAR, PeakVanillaElement.CHAT), back.getHiddenVanilla());
    }

    @Test
    void menusWithButtonsAndActionsSurviveTheWireFormat() {
        PeakDocument menu = PeakMenu.create("shop", settings -> settings.title("Shop").escape(PeakMenuEscape.BACK).noBackdrop());
        menu.add(PeakButton.text("buy", "Buy")
                .onClick(PeakAction.send("buy").arg("item", "sword"), PeakAction.close())
                .tooltip("Buy the sword"));

        PeakDocument back = (PeakDocument) viaWire(menu);

        assertTrue(back.isMenu());
        assertEquals("Shop", back.getMenu().getTitle().getString());
        assertEquals(PeakMenuEscape.BACK, back.getMenu().getEscape());
        assertEquals(PeakPaint.solid(0), back.getMenu().getBackdrop());
        PeakButton button = (PeakButton) back.findById("buy");
        assertNotNull(button);
        assertEquals(2, button.actionsFor(PeakEvent.CLICK).size());
        assertEquals(PeakActionType.SEND, button.actionsFor(PeakEvent.CLICK).get(0).type());
        assertEquals("sword", button.actionsFor(PeakEvent.CLICK).get(0).args().get("item"));
        assertEquals("Buy the sword", button.getInteraction().getTooltip().getString());
        assertEquals(full(menu), full(back));
    }

    @Test
    void elementTooltipsSurviveTheWireFormatAndTextTooltipsReplaceThem() {
        PeakPanel card = PeakPanel.create("tip").column().padding(4).background(0xFF202020);
        card.add(PeakLabel.create("tip-title", "Sword"), PeakLabel.create("tip-body", "Sharp"));
        PeakDocument menu = PeakMenu.create("shop");
        menu.add(PeakButton.text("buy", "Buy").tooltip(card));

        PeakDocument back = (PeakDocument) viaWire(menu);
        PeakInteraction interaction = back.findById("buy").getInteraction();

        assertNotNull(interaction.getTooltipElement());
        assertTrue(interaction.hasTooltip());
        assertEquals("", interaction.getTooltip().getString());
        PeakPanel tooltip = (PeakPanel) interaction.getTooltipElement();
        assertEquals(2, tooltip.getChildren().size());
        assertEquals(PeakPaint.solid(0xFF202020), tooltip.getStyle().getBackground());
        assertEquals(full(menu), full(back));

        PeakButton button = (PeakButton) back.findById("buy");
        button.tooltip("plain");
        assertNull(button.getInteraction().getTooltipElement());
        assertEquals("plain", button.getInteraction().getTooltip().getString());
    }

    @Test
    void tooltipPatchesSwitchBetweenTextAndElement() {
        PeakButton button = PeakButton.text("b", "B").tooltip("text");
        PeakPanel card = PeakPanel.create("card");
        card.add(PeakLabel.create("card-label", "Rich"));

        button.applyPatch(net.gekidolukas.peakclient.ui.patch.PeakPatch.button().tooltip(card).build());
        assertNotNull(button.getInteraction().getTooltipElement());
        assertEquals("", button.getInteraction().getTooltip().getString());

        button.applyPatch(net.gekidolukas.peakclient.ui.patch.PeakPatch.button().tooltip("text again").build());
        assertNull(button.getInteraction().getTooltipElement());
        assertEquals("text again", button.getInteraction().getTooltip().getString());
    }

    @Test
    void hudDocumentsCarryNoMenuFields() {
        PeakDocument doc = PeakDocument.create("hud");
        doc.add(PeakLabel.create("l", "text"));

        JsonObject json = PeakUiSerializer.WIRE.toJsonTree(doc, PeakElement.class).getAsJsonObject();

        assertFalse(json.has("menu"));
        assertFalse(json.getAsJsonArray("children").get(0).getAsJsonObject().has("interaction"));
        assertFalse(((PeakDocument) viaWire(doc)).isMenu());
    }

    @Test
    void fullyWrittenJsonIsStillReadable() {
        PeakDocument doc = sampleDocument(3);
        String fullJson = PeakUiSerializer.GSON.toJson(doc, PeakElement.class);
        PeakDocument back = PeakUiSerializer.documentFromJson(fullJson);
        assertEquals(full(doc), full(back));
    }

    @Test
    void documentJsonDoesNotContainTheIndex() {
        JsonObject json = PeakUiSerializer.WIRE.toJsonTree(sampleDocument(2), PeakElement.class).getAsJsonObject();
        assertFalse(json.has("index"));
    }

    @Test
    void wireFormatIsMuchSmallerThanTheFullFormat() {
        PeakDocument doc = sampleDocument(100);
        String wire = PeakUiSerializer.toJson(doc);
        String full = PeakUiSerializer.GSON.toJson(doc, PeakElement.class);
        int wireCompressed = PeakGzip.compress(wire).length;
        int fullCompressed = PeakGzip.compress(full).length;
        System.out.println("Wire format: " + wire.length() + " chars (" + wireCompressed + " bytes gzip), full format: "
                + full.length() + " chars (" + fullCompressed + " bytes gzip)");
        assertTrue(wire.length() * 2 < full.length());
    }

    @Test
    void operationsWithAddedElementsSurviveTheWireFormat() {
        PeakLabel added = PeakLabel.create("added", "x").color(0x00FF00);
        List<PeakUiOperation> operations = List.of(new PeakUiOperation.Add("parent", added));
        String json = PeakUiSerializer.operationsToJson(operations);
        List<PeakUiOperation> back = PeakUiSerializer.operationsFromJson(json);
        PeakUiOperation.Add add = assertInstanceOf(PeakUiOperation.Add.class, back.get(0));
        assertEquals(full(added), full(add.element()));
    }

    @Test
    void validatorWarnsOnlyNearTheCompressedLimit() {
        PeakDocument doc = sampleDocument(1);
        assertTrue(PeakDocumentValidator.validate(doc, 5_000_000, 100_000).issues().isEmpty());
        assertFalse(PeakDocumentValidator.validate(doc, 5_000_000, 900_000).issues().isEmpty());
        assertFalse(PeakDocumentValidator.exceedsPayloadLimit(1_048_576));
        assertTrue(PeakDocumentValidator.exceedsPayloadLimit(1_048_577));
    }
}
