package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class CompassElementTest implements PeakElementTest {
    @Override public String name() { return "compass"; }
    @Override public String description() { return "PeakCompass: labels, ticks, waypoint stacking, name grouping"; }

    private PeakCompass base() {
        return PeakCompass.create().fieldOfViewDegrees(90)
                .stackDirection(PeakCompassStackDirection.DOWN)
                .waypointLabelYOffset(12)
                .absolute(PeakPosition.of(PeakAnchor.TOP_CENTER, 0, 10));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("cardinal labels only", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base());
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("intercardinal labels + tick marks (top)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base().showIntercardinalLabels(true).showTickMarks(true).tickPosition(PeakCompassTickPosition.TOP));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("stacking waypoints (several close together)", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base();
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);

                    double playerX = player.getX(), playerZ = player.getZ();
                    for (int i = 0; i < 4; i++) {
                        PeakClient.to(player).upsertWaypoint(
                                PeakWaypoint.of(playerX + 40, player.getY(), playerZ + i * 3, "Point " + i)
                                        .color(0xFFAA00).icon("minecraft:textures/map/decorations/target_x.png").visibleIn(compass.getId()));
                    }
                }),
                PeakElementTestStep.of("same-name waypoint grouping (Loot Drop x3)", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base();
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);

                    double playerX = player.getX(), playerZ = player.getZ();
                    for (int i = 0; i < 3; i++) {
                        PeakClient.to(player).upsertWaypoint(
                                PeakWaypoint.of(playerX + 30, player.getY(), playerZ + i * 2, "Loot Drop")
                                        .color(0x55FF55).icon("minecraft:textures/map/decorations/target_x.png").visibleIn(compass.getId()));
                    }
                }),
                PeakElementTestStep.of("styled and translated labels: a gold bold name, an alt font name, a translated key and a tracked entity name keep their style", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base();
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);

                    double playerX = player.getX(), playerZ = player.getZ();
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 40, player.getY(), playerZ - 12,
                            Component.literal("Gold Bold").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                            .color(0xFFFFFF).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 40, player.getY(), playerZ - 4,
                            Component.literal("Alt font").withStyle(Style.EMPTY.withFont(ResourceLocation.parse("minecraft:alt"))))
                            .color(0xFFFFFF).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 40, player.getY(), playerZ + 4,
                            Component.translatable("gui.done")).color(0xFFFFFF).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 40, player.getY(), playerZ + 12,
                            Component.literal("Twin").withStyle(ChatFormatting.GREEN)).color(0xFFFFFF).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 40, player.getY(), playerZ + 14,
                            Component.literal("Twin").withStyle(ChatFormatting.GREEN)).color(0xFFFFFF).visibleIn(compass.getId()));
                }),
                PeakElementTestStep.document("FIXED scaling document: the compass sits centered at the top", 80, doc -> {
                    doc.scaling(PeakScaling.FIXED);
                    doc.add(PeakCompass.create().fieldOfViewDegrees(90).absolute(PeakAnchor.TOP_CENTER, 0, 10));
                }),
                PeakElementTestStep.document("tickPosition BOTTOM, tickIntervalDegrees 15, custom tickColor and backgroundColor", 80, doc ->
                        doc.add(base().showTickMarks(true).tickPosition(PeakCompassTickPosition.BOTTOM).tickIntervalDegrees(15)
                                .tickColor(0xFFFFAA00).backgroundColor(0xCC112233))),
                PeakElementTestStep.of("maxDistance 20: the waypoint at 10 blocks is shown, the one at 40 blocks is hidden", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base().maxDistance(20);
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);
                    double playerX = player.getX(), playerZ = player.getZ();
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 10, player.getY(), playerZ, "Near 10").color(0x55FF55).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX, player.getY(), playerZ + 40, "Far 40").color(0xFF5555).visibleIn(compass.getId()));
                }),
                PeakElementTestStep.of("overlapMode NONE: close waypoints overlap instead of stacking", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base().overlapMode(PeakCompassOverlapMode.NONE);
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);
                    double playerX = player.getX(), playerZ = player.getZ();
                    for (int i = 0; i < 3; i++) {
                        PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 40, player.getY(), playerZ + i * 2, "Point " + i)
                                .color(0xFFAA00).visibleIn(compass.getId()));
                    }
                }),
                PeakElementTestStep.of("stackDirection UP with maxStackedWaypoints 2 and 4 close waypoints", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base().stackDirection(PeakCompassStackDirection.UP).maxStackedWaypoints(2).stackSpacing(2);
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);
                    double playerX = player.getX(), playerZ = player.getZ();
                    for (int i = 0; i < 4; i++) {
                        PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 40, player.getY(), playerZ + i * 3, "Point " + i)
                                .color(0xFFAA00).visibleIn(compass.getId()));
                    }
                }),
                PeakElementTestStep.of("icon scale by distance: 5, 20 and 40 blocks away grow from 0.6 to 1.6", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base().iconScaleRange(0.6f, 1.6f).iconScaleDistanceRange(5, 40).waypointIconSize(12);
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);
                    double playerX = player.getX(), playerZ = player.getZ();
                    String icon = "minecraft:textures/map/decorations/target_x.png";
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 5, player.getY(), playerZ - 5, "5").icon(icon).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 20, player.getY(), playerZ, "20").icon(icon).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(playerX + 30, player.getY(), playerZ + 30, "40").icon(icon).visibleIn(compass.getId()));
                }),
                PeakElementTestStep.of("waypointIconSize 8 (top compass) vs. 16 (lower compass)", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass small = base().waypointIconSize(8).absolute(PeakAnchor.TOP_CENTER, 0, 10);
                    PeakCompass large = PeakCompass.create().fieldOfViewDegrees(90).waypointIconSize(16)
                            .waypointLabelYOffset(18).absolute(PeakAnchor.TOP_CENTER, 0, 70);
                    doc.add(small, large);
                    PeakClient.to(player).sendUi(doc);
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(player.getX() + 30, player.getY(), player.getZ(), "Marker")
                            .icon("minecraft:textures/map/decorations/target_x.png").visibleIn(small.getId(), large.getId()));
                }),
                PeakElementTestStep.of("waypoint expireAfterTicks 40: the waypoint disappears after 2 seconds", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base();
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(player.getX() + 20, player.getY(), player.getZ(), "Expires")
                            .color(0xFFAA00).expireAfterTicks(40).visibleIn(compass.getId()));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(player.getX(), player.getY(), player.getZ() + 20, "Stays")
                            .color(0x55FF55).visibleIn(compass.getId()));
                }),
                PeakElementTestStep.of("waypoint scoped to a compass is removed with it after 60 ticks, the global one stays", 140, (player, docId) -> {
                    PeakDocument first = PeakDocument.create(docId).documentTimeToLiveTicks(60);
                    first.add(PeakCompass.create("evict-a").fieldOfViewDegrees(90).absolute(PeakAnchor.TOP_CENTER, 0, 10));
                    PeakDocument second = PeakDocument.create(docId + "-b").documentTimeToLiveTicks(150).deleteIfEmpty(true);
                    second.add(PeakCompass.create("evict-b").fieldOfViewDegrees(90).absolute(PeakAnchor.TOP_CENTER, 0, 50));
                    PeakClient.to(player).sendUi(first);
                    PeakClient.to(player).sendUi(second);
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(player.getX() + 20, player.getY(), player.getZ(), "Scoped to A")
                            .color(0xFF5555).visibleIn("evict-a"));
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.of(player.getX(), player.getY(), player.getZ() + 20, "Global")
                            .color(0x55FF55));
                }),
                PeakElementTestStep.of("entity tracking LIVING_NAME: the marker follows the nearest mob and shows its name", 160, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakCompass compass = base();
                    doc.add(compass);
                    PeakClient.to(player).sendUi(doc);
                    LivingEntity target = nearestMob(player);
                    if (target == null) {
                        player.sendSystemMessage(Component.literal(
                                "No mob within 48 blocks: summon one (for example /summon minecraft:cow) and run the step again"));
                        return;
                    }
                    PeakClient.to(player).upsertWaypoint(PeakWaypoint.trackingEntity(target.getUUID().toString(),
                            PeakEntityTrackingMode.LIVING_NAME, target.getX(), target.getY(), target.getZ())
                            .color(0x55FFFF).visibleIn(compass.getId()));
                }),
                PeakElementTestStep.of("entity tracking PLAYER_NAME: the marker follows another player and shows the name", 160, (player, docId) -> {
                    trackPlayer(player, docId, PeakEntityTrackingMode.PLAYER_NAME);
                }),
                PeakElementTestStep.of("entity tracking PLAYER_FACE: the marker shows the face of another player", 160, (player, docId) -> {
                    trackPlayer(player, docId, PeakEntityTrackingMode.PLAYER_FACE);
                })
        );
    }

    private static LivingEntity nearestMob(ServerPlayer player) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(48), candidate -> candidate != player && !(candidate instanceof Player))) {
            double distance = entity.distanceToSqr(player);
            if (distance < bestDistance) {
                best = entity;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static void trackPlayer(ServerPlayer player, String docId, PeakEntityTrackingMode mode) {
        ServerPlayer target = player;
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other != player) {
                target = other;
                break;
            }
        }
        if (target == player) {
            player.sendSystemMessage(Component.literal(
                    "Only one player online: the marker follows you, a second player gives a meaningful direction"));
        }
        PeakDocument doc = PeakDocument.create(docId);
        PeakCompass compass = PeakCompass.create().fieldOfViewDegrees(90)
                .waypointLabelYOffset(12)
                .absolute(PeakPosition.of(PeakAnchor.TOP_CENTER, 0, 10));
        doc.add(compass);
        PeakClient.to(player).sendUi(doc);
        PeakClient.to(player).upsertWaypoint(PeakWaypoint.trackingEntity(target.getUUID().toString(), mode,
                target.getX(), target.getY(), target.getZ()).color(0xFFAA00).visibleIn(compass.getId()));
    }
}