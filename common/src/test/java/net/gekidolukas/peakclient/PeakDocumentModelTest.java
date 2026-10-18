package net.gekidolukas.peakclient;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeakDocumentModelTest {

    private static PeakDocument document(String firstText, boolean withSecond, boolean withThird) {
        PeakDocument doc = PeakDocument.create("doc");
        PeakPanel panel = PeakPanel.create("panel");
        panel.add(PeakLabel.create("first", firstText));
        if (withSecond) panel.add(PeakLabel.create("second", "second"));
        if (withThird) panel.add(PeakLabel.create("third", "third"));
        doc.add(panel);
        return doc;
    }

    private static JsonObject patch(String key, String value) {
        JsonObject patch = new JsonObject();
        patch.addProperty(key, value);
        return patch;
    }

    @Test
    void modelIndexesAllElements() {
        PeakDocumentModel model = new PeakDocumentModel(document("a", true, false));
        assertTrue(model.contains("doc"));
        assertTrue(model.contains("panel"));
        assertTrue(model.contains("second"));
        assertEquals("panel", model.parentOf("first"));
    }

    @Test
    void modelAppliesUpdateAddAndDelete() {
        PeakDocumentModel model = new PeakDocumentModel(document("a", false, false));
        assertTrue(model.applyOperation(new PeakUiOperation.Update("first", patch("text", "b"))));
        assertEquals("b", ((PeakLabel) model.find("first")).getText());

        assertTrue(model.applyOperation(new PeakUiOperation.Add("panel", PeakLabel.create("added", "x"))));
        assertTrue(model.contains("added"));
        assertFalse(model.applyOperation(new PeakUiOperation.Add("panel", PeakLabel.create("added", "again"))));

        assertTrue(model.applyOperation(new PeakUiOperation.Delete("added")));
        assertFalse(model.contains("added"));
        assertFalse(model.applyOperation(new PeakUiOperation.Delete("added")));
    }

    @Test
    void removingAContainerRemovesItsChildrenFromTheIndex() {
        PeakDocumentModel model = new PeakDocumentModel(document("a", true, true));
        List<String> removed = new ArrayList<>();
        PeakDocumentModel observed = new PeakDocumentModel(document("a", true, true), new PeakDocumentModel.Listener() {
            @Override
            public void onRemoved(PeakElement<?> element) {
                removed.add(element.getId());
            }
        });
        assertTrue(observed.remove("panel"));
        assertTrue(removed.containsAll(List.of("panel", "first", "second", "third")));
        assertFalse(observed.contains("first"));
        assertTrue(model.contains("first"));
    }

    @Test
    void diffProducesDeleteUpdateAndAdd() {
        PeakDocument before = document("a", true, true);
        PeakDocument after = document("changed", true, false);
        ((PeakPanel) after.getChildren().get(0)).add(PeakLabel.create("fourth", "new"));

        List<PeakUiOperation> operations = PeakDocumentDiff.diff(before, after);

        assertEquals(3, operations.size());
        assertInstanceOf(PeakUiOperation.Delete.class, operations.get(0));
        assertEquals("third", ((PeakUiOperation.Delete) operations.get(0)).elementId());
        PeakUiOperation.Update update = assertInstanceOf(PeakUiOperation.Update.class, operations.get(1));
        assertEquals("first", update.elementId());
        assertEquals("changed", update.patch().get("text").getAsString());
        PeakUiOperation.Add add = assertInstanceOf(PeakUiOperation.Add.class, operations.get(2));
        assertEquals("panel", add.parentId());
        assertEquals("fourth", add.element().getId());
    }

    @Test
    void diffOfIdenticalDocumentsIsEmpty() {
        assertTrue(PeakDocumentDiff.diff(document("a", true, true), document("a", true, true)).isEmpty());
    }

    @Test
    void diffReplacesElementsWhoseTypeChanged() {
        PeakDocument before = PeakDocument.create("doc");
        before.add(PeakLabel.create("x", "label"));
        PeakDocument after = PeakDocument.create("doc");
        after.add(PeakPanel.create("x"));

        List<PeakUiOperation> operations = PeakDocumentDiff.diff(before, after);

        assertEquals(2, operations.size());
        assertInstanceOf(PeakUiOperation.Delete.class, operations.get(0));
        assertInstanceOf(PeakUiOperation.Add.class, operations.get(1));
    }

    @Test
    void appliedDiffTurnsTheOldModelIntoTheNewOne() {
        PeakDocument before = document("a", true, true);
        PeakDocument after = document("changed", true, false);
        PeakDocumentModel model = new PeakDocumentModel(before);
        for (PeakUiOperation operation : PeakDocumentDiff.diff(before, after)) model.applyOperation(operation);

        assertEquals("changed", ((PeakLabel) model.find("first")).getText());
        assertFalse(model.contains("third"));
        assertTrue(PeakDocumentDiff.diff(before, after).isEmpty());
    }

    @Test
    void valueKeysBuildPlaceholders() {
        PeakValueKey<Integer> key = PeakValueKey.ofInt("mymod:score");
        assertEquals("mymod", key.namespace());
        assertEquals("score", key.key());
        assertEquals("{mymod:score}", key.placeholder());
        assertEquals("{mymod:score#0}", key.placeholder("0"));
        assertEquals("42", key.format(42));
        assertEquals("1.50", PeakValueKey.ofDouble("mymod:ratio", "%.2f").format(1.5));
    }

    @Test
    void valueKeysRejectMalformedIds() {
        assertThrows(IllegalArgumentException.class, () -> PeakValueKey.ofString("nocolon"));
        assertThrows(IllegalArgumentException.class, () -> PeakValueKey.ofString(":key"));
        assertThrows(IllegalArgumentException.class, () -> PeakValueKey.ofString("ns:"));
    }
}
