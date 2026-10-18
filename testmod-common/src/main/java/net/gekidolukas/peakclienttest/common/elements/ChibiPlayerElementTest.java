package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChibiPlayerElementTest implements PeakElementTest {
    @Override public String name() { return "chibiplayer"; }
    @Override public String description() { return "PeakChibiPlayer: rotation presets, custom degrees, sources (targeted, texture, UUID), flat lighting, size, auto size and fit"; }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();
        for (PeakChibiRotation rotation : PeakChibiRotation.values()) {
            steps.add(PeakElementTestStep.of(rotation.name() + " (" + rotation.degrees() + " deg)", 50, (player, docId) -> {
                PeakDocument doc = PeakDocument.create(docId);
                doc.add(PeakChibiPlayer.clientPlayer().rotationY(rotation)
                        .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
                PeakClient.to(player).sendUi(doc);
            }));
        }
        steps.add(PeakElementTestStep.of("custom 45 deg", 50, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakChibiPlayer.clientPlayer().rotationYDegrees(45f)
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
            PeakClient.to(player).sendUi(doc);
        }));
        steps.add(PeakElementTestStep.of("targeted player (look at another player)", 100, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakChibiPlayer.targetedPlayer()
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
            doc.add(PeakPlayerIcon.targetedPlayer().view(PeakPlayerIconView.BUST)
                    .absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 10, 10)));
            PeakClient.to(player).sendUi(doc);
        }));
        for (boolean slim : new boolean[]{ false, true }) {
            steps.add(PeakElementTestStep.of("TEXTURE " + (slim ? "slim/alex" : "wide/steve"), 50, (player, docId) -> {
                PeakDocument doc = PeakDocument.create(docId);
                doc.add(PeakChibiPlayer.ofTexture(slim ? "minecraft:textures/entity/player/slim/alex.png" : "minecraft:textures/entity/player/wide/steve.png", slim)
                        .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
                PeakClient.to(player).sendUi(doc);
            }));
        }
        steps.add(PeakElementTestStep.of("UUID not on the server (Mojang lookup, nothing first, then the skin)", 200, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakChibiPlayer.of(UUID.fromString("d9cca0a7-893d-4777-a92b-cefd5bcb3110"))
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
            PeakClient.to(player).sendUi(doc);
        }));
        steps.add(PeakElementTestStep.of("UUID without a profile (all zero, nothing shown, at most one warning per minute)", 200, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakChibiPlayer.of(new UUID(0L, 0L))
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
            PeakClient.to(player).sendUi(doc);
        }));
        steps.add(PeakElementTestStep.document("size and scale: 48x64 (left), 96x128 (middle) and scale 2 (right)", 80, doc -> {
            doc.add(PeakChibiPlayer.clientPlayer().size(48, 64).absolute(PeakAnchor.CENTER, -90, 0));
            doc.add(PeakChibiPlayer.clientPlayer().size(96, 128).absolute(PeakAnchor.CENTER, 0, 0));
            doc.add(PeakChibiPlayer.clientPlayer().size(48, 64).scale(2f).absolute(PeakAnchor.CENTER, 90, 0));
        }));
        steps.add(PeakElementTestStep.document("size auto(): natural size, the framed panel hugs the model (use F3+0 to see the box)", 100, doc -> {
            doc.add(PeakPanel.create().border(1, 0xFFFF5555).absolute(PeakAnchor.CENTER, 0, 0)
                    .add(PeakChibiPlayer.clientPlayer().size(PeakSize.auto())));
        }));
        steps.add(PeakElementTestStep.document("fit: 100% of 200x60 (height limits), 40x200 (width limits) and 120x120", 100, doc -> {
            doc.add(PeakPanel.create().size(200, 60).border(1, 0xFFFF5555).absolute(PeakAnchor.CENTER, 0, -110)
                    .add(PeakChibiPlayer.clientPlayer().size(PeakSize.percent(100, 100))));
            doc.add(PeakPanel.create().size(40, 200).border(1, 0xFF55FF55).absolute(PeakAnchor.CENTER, -90, 30)
                    .add(PeakChibiPlayer.clientPlayer().size(PeakSize.percent(100, 100))));
            doc.add(PeakPanel.create().size(120, 120).border(1, 0xFF5555FF).absolute(PeakAnchor.CENTER, 60, 30)
                    .add(PeakChibiPlayer.clientPlayer().size(PeakSize.percent(100, 100))));
        }));
        steps.add(PeakElementTestStep.document("fit follows the rotation: 45 deg in a 40x80 box never overflows", 100, doc -> {
            doc.add(PeakPanel.create().size(40, 80).border(1, 0xFFFFFF55).absolute(PeakAnchor.CENTER, -50, 0)
                    .add(PeakChibiPlayer.clientPlayer().rotationYDegrees(45f).size(PeakSize.percent(100, 100))));
            doc.add(PeakPanel.create().size(40, 80).border(1, 0xFFFFFF55).absolute(PeakAnchor.CENTER, 50, 0)
                    .add(PeakChibiPlayer.clientPlayer().rotationY(PeakChibiRotation.FRONT).size(PeakSize.percent(100, 100))));
        }));
        return steps;
    }
}
