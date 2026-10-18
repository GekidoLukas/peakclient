package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** Live player-state conditions require the player to actually trigger them - each
 *  step displays instructions and a live conditional element for the step's
 *  duration, so you can perform the action while it's shown. */
public final class ConditionalElementTest implements PeakElementTest {
    @Override public String name() { return "conditional"; }
    @Override public String description() { return "PeakConditional: live conditions, requireAll AND/OR"; }

    private PeakConditional conditionalWith(PeakCondition condition, String label, int color) {
        return conditionalAt(condition, label, color, 20);
    }

    private static PeakConditional conditionalAt(PeakCondition condition, String label, int color, int yOffset) {
        PeakConditional conditional = PeakConditional.create()
                .addCondition(condition)
                .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, yOffset));
        conditional.add(PeakLabel.create(label).color(color).absolute(PeakPosition.of(PeakAnchor.CENTER)));
        return conditional;
    }

    private static void sendLive(ServerPlayer player, String docId, String instruction,
                                 PeakCondition condition, String shown, int color) {
        PeakDocument doc = PeakDocument.create(docId);
        doc.add(PeakLabel.create(instruction).color(0xAAAAAA).absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
        doc.add(conditionalAt(condition, shown, color, 20));
        PeakClient.to(player).sendUi(doc);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("SNEAKING (try sneaking now)", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Sneak to reveal the message below").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
                    doc.add(conditionalWith(PeakCondition.sneaking(), "You are sneaking!", 0x55FF55));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("SPRINTING (try sprinting now)", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Sprint to reveal the message below").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
                    doc.add(conditionalWith(PeakCondition.sprinting(), "You are sprinting!", 0xFFAA00));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("UNDERWATER (submerge your eyes)", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Go underwater to reveal the message below").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
                    doc.add(conditionalWith(PeakCondition.underwater(), "You are underwater!", 0x55AAFF));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("requireAll(true): sneaking AND targeting a block", 120, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Sneak WHILE looking at a block to reveal the message below").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0)));
                    PeakConditional conditional = PeakConditional.create()
                            .addCondition(PeakCondition.sneaking())
                            .addCondition(PeakCondition.targetingBlock())
                            .requireAll(true)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 20));
                    conditional.add(PeakLabel.create("Both conditions met!").color(0xFF55FF).absolute(PeakPosition.of(PeakAnchor.CENTER)));
                    doc.add(conditional);
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("SWIMMING (swim in water with sprint held)", 100, (player, docId) ->
                        sendLive(player, docId, "Swim to reveal the message below",
                                PeakCondition.swimming(), "You are swimming!", 0x55FFFF)),
                PeakElementTestStep.of("FLYING (creative mode: double-tap jump)", 100, (player, docId) ->
                        sendLive(player, docId, "Fly to reveal the message below",
                                PeakCondition.flying(), "You are flying!", 0xFFFF55)),
                PeakElementTestStep.of("RIDING (mount a boat, a horse or a minecart)", 100, (player, docId) ->
                        sendLive(player, docId, "Ride something to reveal the message below",
                                PeakCondition.riding(), "You are riding!", 0xFFAA55)),
                PeakElementTestStep.document("HEALTH_BELOW / HEALTH_ABOVE: the two green lines show, the red line stays hidden", 100, doc -> {
                    doc.add(conditionalAt(PeakCondition.healthBelow(21), "healthBelow(21): shown", 0x55FF55, -10));
                    doc.add(conditionalAt(PeakCondition.healthAbove(0), "healthAbove(0): shown", 0x55FF55, 10));
                    doc.add(conditionalAt(PeakCondition.healthBelow(0), "ERROR: healthBelow(0) must stay hidden", 0xFF5555, 30));
                }),
                PeakElementTestStep.document("IS_DAY / IS_NIGHT: exactly one of the two lines shows (change it with /time set day or night)", 160, doc -> {
                    doc.add(conditionalAt(PeakCondition.isDay(), "isDay: it is day", 0xFFFF55, -10));
                    doc.add(conditionalAt(PeakCondition.isNight(), "isNight: it is night", 0x5555FF, 10));
                }),
                PeakElementTestStep.of("GAMEMODE and DIMENSION: the two green lines show, the red lines stay hidden", 100, (player, docId) -> {
                    String mode = player.gameMode.getGameModeForPlayer().getName();
                    String dimension = player.level().dimension().location().toString();
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(conditionalAt(PeakCondition.gamemode(mode), "gamemode(" + mode + "): shown", 0x55FF55, -20));
                    doc.add(conditionalAt(PeakCondition.dimension(dimension), "dimension(" + dimension + "): shown", 0x55FF55, 0));
                    doc.add(conditionalAt(PeakCondition.gamemode("no_such_mode"), "ERROR: gamemode(no_such_mode) must stay hidden", 0xFF5555, 20));
                    doc.add(conditionalAt(PeakCondition.dimension("minecraft:no_such_dimension"), "ERROR: dimension(no_such_dimension) must stay hidden", 0xFF5555, 40));
                    PeakClient.to(player).sendUi(doc);
                })
        );
    }
}