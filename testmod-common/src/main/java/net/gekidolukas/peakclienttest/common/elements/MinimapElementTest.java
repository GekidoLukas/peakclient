package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;

import java.util.List;

public final class MinimapElementTest implements PeakElementTest {
    private static final String TEXTURE = "minecraft:textures/gui/presets/isles.png";

    @Override public String name() { return "minimap"; }
    @Override public String description() { return "PeakMinimap: border shapes, player icon modes, edge waypoints"; }

    private PeakMinimap base(double x, double z) {
        return PeakMinimap.create(TEXTURE)
                .imageSize(256, 256)
                .worldBounds(x - 500, z - 500, x + 500, z + 500)
                .viewRadiusBlocks(80)
                .absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 10, 10))
                .size(PeakSize.of(PeakSize.PeakDimension.pixels(128), PeakSize.PeakDimension.pixels(128)));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("plain, no border", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(player.getX(), player.getZ()));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("square custom world border", 60, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakClient.to(player).setCustomWorldBorder(x - 60, z - 60, x + 60, z + 60);
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(x, z).worldBorderSource(PeakWorldBorderSource.CUSTOM).worldBorderShape(PeakBorderShape.SQUARE));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("circle custom world border", 60, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakClient.to(player).setCustomWorldBorder(x - 60, z - 60, x + 60, z + 60);
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(x, z).worldBorderSource(PeakWorldBorderSource.CUSTOM).worldBorderShape(PeakBorderShape.CIRCLE));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("player head icon mode", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(player.getX(), player.getZ()).playerIconMode(PeakMinimapPlayerIconMode.PLAYER_HEAD));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("edge indicator for a far-away waypoint", 60, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakMinimap minimap = base(x, z).edgeIndicatorMaxDistance(400);
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(minimap);
                    PeakClient.to(player).sendUi(doc);
                    PeakClient.to(player).upsertWaypoint(
                            PeakWaypoint.of(x + 300, player.getY(), z, "Far Point").color(0xFF5555).visibleIn(minimap.getId()));
                }),
                PeakElementTestStep.of("snapping 1:1 (256px, 1 block per pixel): border, image and waypoint move together while walking", 200, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakClient.to(player).setCustomWorldBorder(x - 40.5, z - 40.5, x + 40.5, z + 40.5);
                    PeakMinimap minimap = PeakMinimap.create(TEXTURE)
                            .imageSize(256, 256)
                            .worldBounds(x - 128, z - 128, x + 128, z + 128)
                            .viewRadiusBlocks(128)
                            .worldBorderSource(PeakWorldBorderSource.CUSTOM)
                            .worldBorderShape(PeakBorderShape.SQUARE)
                            .playerIconMode(PeakMinimapPlayerIconMode.TEXTURE)
                            .playerIconTexture("minecraft:textures/map/decorations/player.png")
                            .absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 10, 10))
                            .size(PeakSize.of(PeakSize.PeakDimension.pixels(256), PeakSize.PeakDimension.pixels(256)));
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(minimap);
                    PeakClient.to(player).sendUi(doc);
                    PeakClient.to(player).upsertWaypoint(
                            PeakWaypoint.of(x + 20, player.getY(), z + 10, "Marker").color(0xFFAA00).visibleIn(minimap.getId()));
                }),
                PeakElementTestStep.of("snapping 2 screen pixels per block (256px, radius 64)", 200, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakClient.to(player).setCustomWorldBorder(x - 40, z - 40, x + 40, z + 40);
                    PeakMinimap minimap = PeakMinimap.create(TEXTURE)
                            .imageSize(256, 256)
                            .worldBounds(x - 128, z - 128, x + 128, z + 128)
                            .viewRadiusBlocks(64)
                            .worldBorderSource(PeakWorldBorderSource.CUSTOM)
                            .worldBorderShape(PeakBorderShape.SQUARE)
                            .playerIconMode(PeakMinimapPlayerIconMode.TEXTURE)
                            .playerIconTexture("minecraft:textures/map/decorations/player.png")
                            .absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 10, 10))
                            .size(PeakSize.of(PeakSize.PeakDimension.pixels(256), PeakSize.PeakDimension.pixels(256)));
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(minimap);
                    PeakClient.to(player).sendUi(doc);
                    PeakClient.to(player).upsertWaypoint(
                            PeakWaypoint.of(x + 20, player.getY(), z + 10, "Marker").color(0xFFAA00).visibleIn(minimap.getId()));
                }),
                PeakElementTestStep.of("player icon TEXTURE: face north and the icon points straight up, unrotated", 160, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(player.getX(), player.getZ()).viewRadiusBlocks(60)
                            .playerIconMode(PeakMinimapPlayerIconMode.TEXTURE)
                            .playerIconTexture("minecraft:textures/map/decorations/player.png"));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("waypoint label on the map does not hide the player icon", 120, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakMinimap minimap = base(x, z).viewRadiusBlocks(30).playerIconMode(PeakMinimapPlayerIconMode.PLAYER_HEAD);
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(minimap);
                    PeakClient.to(player).sendUi(doc);
                    PeakClient.to(player).upsertWaypoint(
                            PeakWaypoint.of(x + 2, player.getY(), z, "Loot Drop").labelOnMap(true).visibleIn(minimap.getId()));
                }),
                PeakElementTestStep.of("viewRadiusBlocks: 30 (left) vs. 120 (right)", 80, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(x, z).viewRadiusBlocks(30).absolute(PeakAnchor.TOP_LEFT, 10, 10));
                    doc.add(base(x, z).viewRadiusBlocks(120).absolute(PeakAnchor.TOP_LEFT, 160, 10));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("borderColor: an orange frame around the map", 80, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(player.getX(), player.getZ()).borderColor(0xFFFFAA00));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("outOfImageColor: the area outside a small image is blue", 80, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(x, z).worldBounds(x - 20, z - 20, x + 20, z + 20).viewRadiusBlocks(60).outOfImageColor(0xFF223355));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("worldBorderThickness: 1 (left) vs. 4 (right) with a square custom border", 120, (player, docId) -> {
                    double x = player.getX(), z = player.getZ();
                    PeakClient.to(player).setCustomWorldBorder(x - 40, z - 40, x + 40, z + 40);
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(base(x, z).worldBorderSource(PeakWorldBorderSource.CUSTOM).worldBorderShape(PeakBorderShape.SQUARE)
                            .worldBorderThickness(1).absolute(PeakAnchor.TOP_LEFT, 10, 10));
                    doc.add(base(x, z).worldBorderSource(PeakWorldBorderSource.CUSTOM).worldBorderShape(PeakBorderShape.SQUARE)
                            .worldBorderThickness(4).absolute(PeakAnchor.TOP_LEFT, 160, 10));
                    PeakClient.to(player).sendUi(doc);
                })
        );
    }
}