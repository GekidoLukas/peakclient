package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class WorldAnchorElementTest implements PeakElementTest {
    @Override public String name() { return "worldanchor"; }

    @Override
    public String description() {
        return "PeakWorldAnchor: fixed points, entity targets, distance scale and fade, occlusion by blocks and entities, edge clamping, patches";
    }

    private static Vec3 ahead(ServerPlayer player, double distance) {
        Vec3 look = player.getLookAngle();
        double length = Math.sqrt(look.x * look.x + look.z * look.z);
        double dx = length < 1e-3 ? 0.0 : look.x / length;
        double dz = length < 1e-3 ? 1.0 : look.z / length;
        return new Vec3(player.getX() + dx * distance, player.getY(), player.getZ() + dz * distance);
    }

    private static PeakPanel tag(String text, int color) {
        return PeakPanel.create().padding(3, 6, 3, 6).borderRadius(4).background(color)
                .add(PeakLabel.create(text).color(PeakPaint.WHITE));
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§e[worldanchor] §f" + text));
    }

    private static void send(ServerPlayer player, String docId, PeakWorldAnchor... anchors) {
        PeakDocument doc = PeakDocument.create(docId);
        for (PeakWorldAnchor anchor : anchors) doc.add(anchor);
        PeakClient.to(player).sendUi(doc);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("fixed point 8 blocks ahead and 1.5 up: the label sits above the point; turn away and it leaves the screen", 400,
                        (player, docId) -> {
                            Vec3 point = ahead(player, 8);
                            send(player, docId, PeakWorldAnchor.at("peaktest:anchor/fixed", point.x, player.getY() + 1.5, point.z)
                                    .noOcclusion().add(tag("Fixed point", 0xFF3366CC)));
                            say(player, "turn around: the label disappears at the screen edge (edge mode HIDE)");
                        }),
                PeakElementTestStep.of("entity target: a label above your own head (switch to third person with F5), with and without a 0.5 block offset", 400,
                        (player, docId) -> {
                            send(player, docId,
                                    PeakWorldAnchor.entity("peaktest:anchor/entity", player.getUUID()).noOcclusion()
                                            .add(tag("Above head", 0xFF33AA55)),
                                    PeakWorldAnchor.entity("peaktest:anchor/entity-center", player.getUUID()).noOcclusion()
                                            .target(target -> target.point(PeakAnchorTarget.EntityPoint.CENTER))
                                            .pivot(PeakAnchor.CENTER).add(tag("Center", 0xFFCC7722)));
                            say(player, "press F5: the labels follow you and stay glued to the model while you walk");
                        }),
                PeakElementTestStep.of("distance: four markers 5, 15, 30 and 60 blocks ahead; they shrink with the distance (scale 0.5 to 1.5) and fade out between 40 and 50 blocks", 500,
                        (player, docId) -> {
                            double[] distances = {5, 15, 30, 60};
                            PeakWorldAnchor[] anchors = new PeakWorldAnchor[distances.length];
                            for (int i = 0; i < distances.length; i++) {
                                Vec3 point = ahead(player, distances[i]);
                                anchors[i] = PeakWorldAnchor.at("peaktest:anchor/distance-" + i, point.x, player.getY() + 2.0, point.z)
                                        .noOcclusion().scaleWithDistance(10f, 0.5f, 1.5f).maxDistance(50f).fadeDistance(10f)
                                        .add(tag((int) distances[i] + " blocks", 0xFF8844BB));
                            }
                            send(player, docId, anchors);
                            say(player, "walk towards the markers and back: the size follows the distance, the far one fades");
                        }),
                PeakElementTestStep.of("occlusion by blocks: three labels 3 blocks below the ground; look down: DIM fades to 35 percent, HIDE disappears, NONE shows through", 500,
                        (player, docId) -> {
                            Vec3 point = ahead(player, 5);
                            double y = player.getY() - 3.0;
                            send(player, docId,
                                    PeakWorldAnchor.at("peaktest:anchor/dim", point.x - 2, y, point.z)
                                            .occlusion(PeakOcclusion.blocks()).add(tag("Dim", 0xFF3366CC)),
                                    PeakWorldAnchor.at("peaktest:anchor/hide", point.x, y, point.z)
                                            .occlusion(PeakOcclusion.blocks().hide()).add(tag("Hide", 0xFFAA3333)),
                                    PeakWorldAnchor.at("peaktest:anchor/none", point.x + 2, y, point.z).noOcclusion()
                                            .add(tag("None", 0xFF33AA55)));
                            say(player, "stand on solid ground and look down at the three labels, then dig down to see them appear");
                        }),
                PeakElementTestStep.of("partial occlusion by entities: the label is 3 blocks above the ground 6 blocks ahead; place a mob or a second player so that it covers only a part of the label from your view; only the covered part disappears", 600,
                        (player, docId) -> {
                            Vec3 point = ahead(player, 6);
                            send(player, docId,
                                    PeakWorldAnchor.at("peaktest:anchor/partial-wide", point.x, player.getY() + 1.0, point.z)
                                            .pivot(PeakAnchor.CENTER).add(tag("Partially covered by anything in front of it", 0xFF3366CC)));
                            say(player, "walk around so that a mob, a player or a block corner overlaps one end of the label; the rest stays visible");
                        }),
                PeakElementTestStep.of("partial occlusion by blocks: two labels 4 blocks ahead at head height; build or find a corner so a block edge cuts through them, the left one hides the covered part, the right one dims it", 600,
                        (player, docId) -> {
                            Vec3 point = ahead(player, 4);
                            send(player, docId,
                                    PeakWorldAnchor.at("peaktest:anchor/partial-hide", point.x - 1.5, player.getY() + 1.6, point.z)
                                            .pivot(PeakAnchor.CENTER).add(tag("Hidden where covered", 0xFFAA3333)),
                                    PeakWorldAnchor.at("peaktest:anchor/partial-dim", point.x + 1.5, player.getY() + 1.6, point.z)
                                            .pivot(PeakAnchor.CENTER).occlusion(PeakOcclusion.partial().dim().dimTo(0.3f))
                                            .add(tag("Dimmed where covered", 0xFF33AA55)));
                            say(player, "stand so that a pillar or wall edge crosses a label: the border follows the edge pixel by pixel");
                        }),
                PeakElementTestStep.of("partial occlusion on a target entity: labels on a nearby mob or player with default bias and with 0 bias (the model should never cut the label with the default)", 600,
                        (player, docId) -> {
                            send(player, docId,
                                    PeakWorldAnchor.entity("peaktest:anchor/partial-entity", player.getUUID())
                                            .add(tag("Default bias", 0xFF3366CC)),
                                    PeakWorldAnchor.entity("peaktest:anchor/partial-entity-zero", player.getUUID())
                                            .occlusion(PeakOcclusion.partial().depthBias(0f))
                                            .target(target -> target.point(PeakAnchorTarget.EntityPoint.CENTER))
                                            .pivot(PeakAnchor.CENTER).add(tag("Bias 0 at the center", 0xFFCC7722)));
                            say(player, "press F5 and look at yourself: the first label sits cleanly above your head, the second may flicker where it touches the model");
                        }),
                PeakElementTestStep.of("occlusion by entities: the left label ignores entities, the right one is hidden by any mob or player standing in the line of sight (stand a mob in front)", 500,
                        (player, docId) -> {
                            Vec3 point = ahead(player, 10);
                            send(player, docId,
                                    PeakWorldAnchor.at("peaktest:anchor/blocks-only", point.x - 1.5, player.getY() + 2.0, point.z)
                                            .occlusion(PeakOcclusion.blocks().hide()).add(tag("Blocks only", 0xFF3366CC)),
                                    PeakWorldAnchor.at("peaktest:anchor/with-entities", point.x + 1.5, player.getY() + 2.0, point.z)
                                            .occlusion(PeakOcclusion.blocksAndEntities().hide()).add(tag("Blocks + entities", 0xFFAA3333)));
                            say(player, "let another player or a mob walk between you and the labels; only the right one reacts");
                        }),
                PeakElementTestStep.of("edge clamp: markers 20 blocks behind you and 20 to your right; they stick to the screen border with an arrow, the third one only hides", 500,
                        (player, docId) -> {
                            Vec3 behind = ahead(player, -20);
                            Vec3 look = player.getLookAngle();
                            double length = Math.max(1e-3, Math.sqrt(look.x * look.x + look.z * look.z));
                            double rightX = -look.z / length, rightZ = look.x / length;
                            send(player, docId,
                                    PeakWorldAnchor.at("peaktest:anchor/behind", behind.x, player.getY() + 1.5, behind.z)
                                            .noOcclusion().clampToEdge(8).add(tag("Behind", 0xFFCC7722)),
                                    PeakWorldAnchor.at("peaktest:anchor/right", player.getX() + rightX * 20, player.getY() + 1.5,
                                            player.getZ() + rightZ * 20).noOcclusion().clampToEdge(8)
                                            .edgeArrow(0xFFFFD700, 9).add(tag("Right", 0xFF33AA55)),
                                    PeakWorldAnchor.at("peaktest:anchor/hidden-edge", behind.x + 3, player.getY() + 1.5, behind.z)
                                            .noOcclusion().add(tag("Hidden at the edge", 0xFFAA3333)));
                            say(player, "turn slowly: the markers slide along the border and enter the screen as labels");
                        }),
                PeakElementTestStep.of("patches: the label moves 1 block per half second along your look direction and flashes green at 3 s", 400,
                        (player, docId) -> {
                            Vec3 start = ahead(player, 4);
                            PeakWorldAnchor anchor = PeakWorldAnchor.at("peaktest:anchor/moving", start.x, player.getY() + 1.5, start.z)
                                    .noOcclusion();
                            PeakAnimated animated = PeakAnimated.create("peaktest:anchor/moving-box").borderRadius(4)
                                    .add(tag("Moving", 0xFF3366CC));
                            anchor.add(animated);
                            send(player, docId, anchor);
                            Vec3 look = player.getLookAngle();
                            double length = Math.max(1e-3, Math.sqrt(look.x * look.x + look.z * look.z));
                            double dx = look.x / length, dz = look.z / length;
                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            for (int i = 1; i <= 12; i++) {
                                int step = i;
                                timeline.atSeconds(0.5 * i, () -> PeakClient.to(player).updateElement(docId, anchor.getId(),
                                        PeakPatch.worldAnchor().moveTo(start.x + dx * step, player.getY() + 1.5, start.z + dz * step)));
                            }
                            timeline.atSeconds(3, () -> PeakClient.to(player).updateElement(docId, animated.getId(),
                                    PeakPatch.animated().flash(0xFF55FF55)));
                            timeline.start();
                        }),
                PeakElementTestStep.of("in a layout: an anchor inside a flow and a column does not change their size or the position of the other children", 400,
                        (player, docId) -> {
                            Vec3 point = ahead(player, 8);
                            PeakFlow flow = PeakFlow.create("peaktest:anchor/flow").width(160).background(0x60000000).padding(4).spacing(4)
                                    .absolute(PeakAnchor.TOP_LEFT, 20, 20);
                            flow.add(tag("One", 0xFF3366CC), tag("Two", 0xFF33AA55),
                                    PeakWorldAnchor.at("peaktest:anchor/in-flow", point.x, player.getY() + 1.5, point.z).noOcclusion()
                                            .add(tag("Anchored", 0xFFAA3333)),
                                    tag("Three", 0xFFCC7722), tag("Four", 0xFF8844BB));
                            PeakDocument doc = PeakDocument.create(docId);
                            doc.add(flow);
                            PeakClient.to(player).sendUi(doc);
                            say(player, "the flow shows One to Four without a gap; the red label hangs in the world");
                        }),
                PeakElementTestStep.of("highlights on an anchor: the label loops a gold glow around its shape (F3+0 shows the anchor box, screen position, distance and occlusion state)", 400,
                        (player, docId) -> {
                            Vec3 point = ahead(player, 7);
                            PeakWorldAnchor anchor = PeakWorldAnchor.at("peaktest:anchor/highlight", point.x, player.getY() + 1.5, point.z)
                                    .noOcclusion();
                            PeakAnimated pulse = PeakAnimated.of(PeakAnimationEffect.GLOW).periodTicks(40).borderRadius(4)
                                    .add(tag("Glowing", 0xFF336699));
                            anchor.add(pulse);
                            send(player, docId, anchor);
                            say(player, "F3+0 shows the anchor box, its screen position, distance and occlusion state in the detail panel");
                        })
        );
    }
}
