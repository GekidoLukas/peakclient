package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class EntityElementTest implements PeakElementTest {
    @Override public String name() { return "entity"; }
    @Override public String description() { return "PeakEntityElement: FIXED, PLAYER copy modes, TARGETED_ENTITY, textures, yaw, pitch, zoom, name tag"; }

    private PeakEntityElement withSize(PeakEntityElement entity) {
        return entity.absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                .size(PeakSize.of(PeakSize.PeakDimension.pixels(48), PeakSize.PeakDimension.pixels(64)));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("FIXED zombie", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.fixed("minecraft:zombie").yaw(180)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("PLAYER, FULL copy (live animation)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.player().copyMode(PeakEntityCopyMode.FULL).yaw(180)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("PLAYER, TYPE_AND_SKIN (neutral pose, no gear)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).yaw(180)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("PLAYER, TYPE_AND_SKIN_EQUIPMENT", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN_EQUIPMENT).yaw(180)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("TARGETED_ENTITY (look at any mob)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Look at a living entity").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, -40)));
                    doc.add(withSize(PeakEntityElement.targetedEntity().yaw(180)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("name tag off by default (TYPE_AND_SKIN)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).yaw(180)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("name tag enabled, yaw 60 (TYPE_AND_SKIN, turn your view)", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).showNameTag(true).yaw(60)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("TEXTURE wide/steve (left) and slim/alex (right)", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.playerTexture("minecraft:textures/entity/player/wide/steve.png").yaw(20)).absolute(PeakPosition.of(PeakAnchor.CENTER, -40, 0)));
                    doc.add(withSize(PeakEntityElement.playerTexture("minecraft:textures/entity/player/slim/alex.png", true).yaw(-20)).absolute(PeakPosition.of(PeakAnchor.CENTER, 40, 0)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("sneak test: FULL copy, hold sneak - size must stay constant", 200, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakEntityElement.player().copyMode(PeakEntityCopyMode.FULL).yaw(180)));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.document("yaw 0, 90 and 180 side by side (TYPE_AND_SKIN)", 100, doc -> {
                    for (int i = 0; i < 3; i++) {
                        doc.add(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).yaw(i * 90f)
                                .size(48, 64).absolute(PeakAnchor.CENTER, (i - 1) * 70, 0));
                    }
                }),
                PeakElementTestStep.document("pitch -30, 0 and 30 side by side (TYPE_AND_SKIN)", 100, doc -> {
                    for (int i = 0; i < 3; i++) {
                        doc.add(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).pitch((i - 1) * 30f)
                                .size(48, 64).absolute(PeakAnchor.CENTER, (i - 1) * 70, 0));
                    }
                }),
                PeakElementTestStep.document("zoom 0.6, 1.0 and 1.4 side by side (TYPE_AND_SKIN)", 100, doc -> {
                    float[] zooms = { 0.6f, 1.0f, 1.4f };
                    for (int i = 0; i < 3; i++) {
                        doc.add(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).zoom(zooms[i])
                                .size(48, 64).absolute(PeakAnchor.CENTER, (i - 1) * 70, 0));
                    }
                }),
                PeakElementTestStep.document("showNameTag with the local player: FULL (left) never shows it, TYPE_AND_SKIN copy (right) does", 120, doc -> {
                    doc.add(PeakEntityElement.player().copyMode(PeakEntityCopyMode.FULL).showNameTag(true).yaw(0)
                            .size(64, 96).absolute(PeakAnchor.CENTER, -50, 0));
                    doc.add(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).showNameTag(true).yaw(0)
                            .size(64, 96).absolute(PeakAnchor.CENTER, 50, 0));
                }),
                PeakElementTestStep.document("name tag with opacity 1.0 (left) vs. 0.5 (right): the name fades with the entity", 120, doc -> {
                    doc.add(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).showNameTag(true).yaw(0)
                            .size(64, 96).absolute(PeakAnchor.CENTER, -50, 0));
                    doc.add(PeakEntityElement.player().copyMode(PeakEntityCopyMode.TYPE_AND_SKIN).showNameTag(true).yaw(0)
                            .size(64, 96).opacity(0.5f).absolute(PeakAnchor.CENTER, 50, 0));
                })
        );
    }
}
