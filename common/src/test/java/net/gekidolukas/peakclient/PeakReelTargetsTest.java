package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.api.PeakReelSpins;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakReel;
import net.gekidolukas.peakclient.ui.PeakReelEasing;
import net.gekidolukas.peakclient.ui.PeakReelMotion;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakReelTargetsTest {
    private static PeakReel reelWith(String... ids) {
        PeakReel reel = PeakReel.create("reel");
        for (String id : ids) reel.add(PeakLabel.create(id, id));
        return reel;
    }

    @Test
    void anInsertIndexPlacesTheChildAndSurvivesTheWire() {
        PeakDocument document = PeakDocument.create("doc");
        document.add(PeakPanel.create("panel").add(PeakLabel.create("a", "a")).add(PeakLabel.create("b", "b")));
        PeakDocumentModel model = new PeakDocumentModel(document);

        assertTrue(model.applyOperation(new PeakUiOperation.Add("panel", PeakLabel.create("x", "x"), 1)));
        assertTrue(model.applyOperation(new PeakUiOperation.Add("panel", PeakLabel.create("y", "y"), 99)));
        assertTrue(model.applyOperation(new PeakUiOperation.Add("panel", PeakLabel.create("z", "z"))));

        PeakPanel panel = (PeakPanel) model.find("panel");
        assertEquals(List.of("a", "x", "b", "y", "z"), panel.getChildren().stream().map(child -> child.getId()).toList());

        String json = PeakUiSerializer.operationsToJson(List.of(
                new PeakUiOperation.Add("panel", PeakLabel.create("p", "p"), 2),
                new PeakUiOperation.Add("panel", PeakLabel.create("q", "q"))));
        List<PeakUiOperation> back = PeakUiSerializer.operationsFromJson(json);
        assertEquals(2, assertInstanceOf(PeakUiOperation.Add.class, back.get(0)).insertIndex());
        assertNull(assertInstanceOf(PeakUiOperation.Add.class, back.get(1)).insertIndex());
    }

    @Test
    void aSpinByIdPatchCarriesTheIdThroughTheWire() {
        PeakDocument document = PeakDocument.create("doc");
        document.add(reelWith("a", "b", "c"));
        PeakReel back = assertInstanceOf(PeakReel.class,
                PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document)).findById("reel"));

        back.applyPatch(PeakPatch.reel().spinTo("c", 30, 1).build());
        PeakReel.SpinRequest request = back.takeSpinRequest();
        assertEquals("c", request.targetId());
        assertEquals(30, request.ticks());
        assertEquals(1, request.loops());

        back.applyPatch(PeakPatch.reel().spinTo(PeakLabel.create("b", "b")).build());
        assertEquals("b", back.takeSpinRequest().targetId());

        back.applyPatch(PeakPatch.reel().spinTo(2).build());
        assertNull(back.takeSpinRequest().targetId());
    }

    @Test
    void spinningToANewWinnerAddsItOnceAndThenSpins() {
        PeakReel reel = reelWith("a", "b", "c");
        PeakLabel winner = PeakLabel.create("win", "win");

        List<PeakUiOperation> operations = PeakReelSpins.operations("reel", winner, reel, new Random(3));

        assertEquals(2, operations.size());
        PeakUiOperation.Add add = assertInstanceOf(PeakUiOperation.Add.class, operations.get(0));
        assertTrue(add.insertIndex() >= 0 && add.insertIndex() <= 3);
        PeakUiOperation.Update update = assertInstanceOf(PeakUiOperation.Update.class, operations.get(1));
        assertEquals("reel", update.elementId());
        assertEquals("win", update.patch().getAsJsonObject("spin").get("targetId").getAsString());
    }

    @Test
    void spinningToAnExistingChildOnlyUpdatesAndAnUnknownReelAppends() {
        PeakReel reel = reelWith("a", "b");
        List<PeakUiOperation> known = PeakReelSpins.operations("reel", PeakLabel.create("b", "b"), reel, new Random(1));
        assertEquals(1, known.size());
        assertInstanceOf(PeakUiOperation.Update.class, known.get(0));

        List<PeakUiOperation> unknown = PeakReelSpins.operations("reel", PeakLabel.create("n", "n"), null, new Random(1));
        assertEquals(2, unknown.size());
        assertNull(assertInstanceOf(PeakUiOperation.Add.class, unknown.get(0)).insertIndex());
    }

    @Test
    void theBuilderInsertsAnElementOnceAndTargetsItsId() {
        PeakLabel winner = PeakLabel.create("win", "win");
        PeakReel reel = reelWith("a", "b", "c").spinOnStart(winner, 5);

        assertEquals(4, reel.getChildren().size());
        assertTrue(reel.getChildren().contains(winner));
        assertEquals("win", reel.getAutoSpinTargetId());
        assertEquals(5, reel.getAutoSpinDelayTicks());
        assertTrue(reel.hasAutoSpin());

        reel.spinOnStart(winner);
        assertEquals(4, reel.getChildren().size());

        PeakReel byId = reelWith("a", "b").spinOnStart("b");
        assertEquals("b", byId.getAutoSpinTargetId());
        assertEquals(2, byId.getChildren().size());
        assertEquals(-1, byId.getAutoSpinTarget());

        byId.spinOnStart(1);
        assertTrue(byId.hasAutoSpin());
        assertEquals(1, byId.getAutoSpinTarget());
    }

    @Test
    void anAutoSpinByIdSurvivesTheWire() {
        PeakDocument document = PeakDocument.create("doc");
        document.add(reelWith("a", "b").spinOnStart("b", 7));
        PeakReel back = assertInstanceOf(PeakReel.class,
                PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document)).findById("reel"));
        assertEquals("b", back.getAutoSpinTargetId());
        assertEquals(7, back.getAutoSpinDelayTicks());
        assertTrue(back.hasAutoSpin());
    }

    @Test
    void remapKeepsTheDisplayedChildWhenOthersAreInsertedBefore() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(2);

        motion.remap(5, 6, slot -> slot >= 1 ? slot + 1 : slot, 0.0);

        assertEquals(3.0, motion.position(0.0), 1e-9);
        assertFalse(motion.isSpinning());
    }

    @Test
    void remapFallsBackWhenTheDisplayedChildIsGone() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(4);

        motion.remap(5, 3, slot -> -1, 0.0);

        assertEquals(2.0, motion.position(0.0), 1e-9);
    }

    @Test
    void remapDuringASpinStillLandsOnTheSameChild() {
        PeakReelMotion motion = new PeakReelMotion();
        motion.ensureInitialized(0);
        motion.spin(5, 3, 0.0, 100.0, 1, PeakReelEasing.DECELERATE);
        motion.position(40.0);

        motion.remap(5, 6, slot -> slot >= 2 ? slot + 1 : slot, 40.0);

        assertTrue(motion.isSpinning());
        assertEquals(4.0, PeakReelMotion.mod(motion.position(1000.0), 6), 1e-9);
    }
}
