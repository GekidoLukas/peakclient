package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerIconElementTest implements PeakElementTest {
    @Override public String name() { return "playericon"; }
    @Override public String description() { return "PeakPlayerIcon: face/bust/upper body/full body incl. outer layers, aspect fit"; }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();
        for (PeakPlayerIconView view : PeakPlayerIconView.values()) {
            steps.add(PeakElementTestStep.of(view.name() + " (64x64 box)", 50, (player, docId) -> {
                PeakDocument doc = PeakDocument.create(docId);
                doc.add(PeakPlayerIcon.clientPlayer().view(view)
                        .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                        .size(PeakSize.of(PeakSize.PeakDimension.pixels(64), PeakSize.PeakDimension.pixels(64))));
                PeakClient.to(player).sendUi(doc);
            }));
        }
        steps.add(PeakElementTestStep.of("FULL_BODY in odd-sized box (37x61, gap check)", 50, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakPlayerIcon.clientPlayer().view(PeakPlayerIconView.FULL_BODY)
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(37), PeakSize.PeakDimension.pixels(61))));
            PeakClient.to(player).sendUi(doc);
        }));
        for (boolean slim : new boolean[]{ false, true }) {
            steps.add(PeakElementTestStep.of("TEXTURE " + (slim ? "slim/alex" : "wide/steve") + " (UPPER_BODY)", 50, (player, docId) -> {
                PeakDocument doc = PeakDocument.create(docId);
                doc.add(PeakPlayerIcon.ofTexture(slim ? "minecraft:textures/entity/player/slim/alex.png" : "minecraft:textures/entity/player/wide/steve.png", slim)
                        .view(PeakPlayerIconView.UPPER_BODY)
                        .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                        .size(PeakSize.of(PeakSize.PeakDimension.pixels(64), PeakSize.PeakDimension.pixels(64))));
                PeakClient.to(player).sendUi(doc);
            }));
        }
        steps.add(PeakElementTestStep.of("UUID not on the server (Mojang lookup, nothing first, then the skin)", 200, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakPlayerIcon.of(UUID.fromString("d9cca0a7-893d-4777-a92b-cefd5bcb3110")).view(PeakPlayerIconView.BUST)
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(64), PeakSize.PeakDimension.pixels(64))));
            PeakClient.to(player).sendUi(doc);
        }));
        steps.add(PeakElementTestStep.of("UUID without a profile (all zero, nothing shown, at most one warning per minute)", 200, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            doc.add(PeakPlayerIcon.of(new UUID(0L, 0L)).view(PeakPlayerIconView.BUST)
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(64), PeakSize.PeakDimension.pixels(64))));
            PeakClient.to(player).sendUi(doc);
        }));
        return steps;
    }
}
