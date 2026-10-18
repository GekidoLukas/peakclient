package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakTargets;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPanelPatch;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.template.PeakRichTextToolbar;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class PeakTestCommon {

    private static void registerReadyListener() {
        PeakClient.onClientReady(event -> {
            var replay = event.replay();
            event.player().sendSystemMessage(Component.literal("§a[PeakTest] Client ready, protocol " + event.protocolVersion()
                    + ", replayed: " + replay.customValues() + " custom values, " + replay.waypoints() + " waypoints, "
                    + replay.vanillaHudOverrides() + " HUD overrides, world border: " + (replay.worldBorder() ? "yes" : "no")));
        });
    }

    private static void registerDocumentActionListener() {
        PeakClient.onDocumentAction(event -> {
            if (!event.actionId().startsWith("events-")) return;
            event.player().sendSystemMessage(Component.literal("§a[server] §fgot §e" + event.actionId() + "§f from §e"
                    + event.elementId() + "§f in §e" + event.documentId() + "§f " + event.args()));
        });
    }

    public static void registerQuickTests(){
        registerReadyListener();
        registerPercentQuickTest();
        registerDocumentActionListener();
        PeakMenuTestSuite.init();

        PeakClient.menu("peaktest:quick-menu/toolbar").on("toolbar-print", event ->
                event.player().sendSystemMessage(Component.literal("§d[toolbar] §f"
                        + event.arg("peaktest:quick/toolbar/notes"))));

        PeakQuickTestRegistry.registerMenu("toolbar", (player, menu) -> {
            String box = "peaktest:quick/toolbar/notes";
            PeakRichTextBox notes = PeakRichTextBox.create(box, "§lBold§r, §oitalic§r, §nunderlined§r and §cred§r text\nSecond line")
                    .placeholder("Write something")
                    .size(240, 80);

            PeakPanel customRow = PeakPanel.create("peaktest:quick/toolbar/custom").row().gap(3).padding(0).background(0);
            customRow.add(
                    PeakButton.text("peaktest:quick/toolbar/red", "Red").keepFocus(true)
                            .onClick(PeakAction.formatColor(box, 0xFF5555)),
                    PeakButton.text("peaktest:quick/toolbar/green", "Green").keepFocus(true)
                            .onClick(PeakAction.formatColor(box, 0x55FF55)),
                    PeakButton.text("peaktest:quick/toolbar/blue", "Blue").keepFocus(true)
                            .onClick(PeakAction.formatColor(box, 0x5555FF)),
                    PeakButton.text("peaktest:quick/toolbar/clear", "Plain").keepFocus(true)
                            .onClick(PeakAction.format(box, PeakFormat.CLEAR)));

            PeakPanel panel = PeakPanel.create("peaktest:quick/toolbar/panel")
                    .absolute(PeakAnchor.CENTER, 0, 0)
                    .column().gap(6).padding(10).width(262).borderRadius(5)
                    .background(0xFF202030).border(1, 0xFF888888);
            panel.add(
                    PeakLabel.create("peaktest:quick/toolbar/title", "Rich text toolbar").color(0xFFD700),
                    PeakLabel.create("peaktest:quick/toolbar/l1", "Default toolbar").color(0x888888),
                    PeakRichTextToolbar.create("peaktest:quick/toolbar/default", box).colorPicker().build(),
                    PeakLabel.create("peaktest:quick/toolbar/l2", "Styled, only bold and italic").color(0x888888),
                    PeakRichTextToolbar.create("peaktest:quick/toolbar/styled", box)
                            .buttons(PeakFormat.BOLD, PeakFormat.ITALIC)
                            .gap(4).activeBackground(0xFF2E9E4F)
                            .buttonStyle(button -> button.borderRadius(8).padding(4, 10)).build(),
                    notes,
                    PeakLabel.create("peaktest:quick/toolbar/l3", "Own buttons with fixed colors").color(0x888888),
                    customRow,
                    PeakButton.text("peaktest:quick/toolbar/print", "Print value in chat")
                            .onClick(PeakAction.send("toolbar-print").collect(box)),
                    PeakButton.text("peaktest:quick/toolbar/close", "Close").onClick(PeakAction.close()));
            menu.add(panel);
            return menu;
        });

        PeakClient.menu("peaktest:quick-menu/colorpicker").on("colorpicker-print", event ->
                event.player().sendSystemMessage(Component.literal("§d[colorpicker] §f"
                        + event.arg("peaktest:quick/colorpicker/picker") + " §7/ §f"
                        + event.arg("peaktest:quick/colorpicker/notes"))));

        PeakQuickTestRegistry.registerMenu("colorpicker", (player, menu) -> {
            String prefix = "peaktest:quick/colorpicker/";
            String picker = prefix + "picker";
            String notes = prefix + "notes";

            PeakPanel swatches = PeakPanel.create(prefix + "swatches").row().gap(3).padding(0).background(0);
            int[] colors = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF, 0xFFFFFF};
            for (int i = 0; i < colors.length; i++) {
                swatches.add(PeakPanel.create(prefix + "swatch-" + i)
                        .size(16, 16).background(0xFF000000 | colors[i]).border(1, 0xFF000000)
                        .onClick(PeakAction.setColor(picker, colors[i]))
                        .tooltip(String.format("#%06X", colors[i])));
            }

            PeakPanel panel = PeakPanel.create(prefix + "panel")
                    .absolute(PeakAnchor.CENTER, 0, 0)
                    .column().gap(6).padding(10).width(220).borderRadius(5)
                    .background(0xFF202030).border(1, 0xFF888888);
            panel.add(
                    PeakLabel.create(prefix + "title", "Color picker").color(0xFFD700),
                    PeakRichTextToolbar.create(prefix + "toolbar", notes).build(),
                    PeakRichTextBox.create(notes, "§lSelect§r me and pick a §ocolor§r\nSecond line").size(200, 48),
                    PeakColorPicker.create(picker, 0xFF8800).size(200, 100).target(notes).hexField(prefix + "hex")
                            .onChange(PeakAction.send("colorpicker-print").collect(picker, notes)),
                    PeakTextBox.create(prefix + "hex", "#FF8800").maxLength(9).width(200),
                    swatches,
                    PeakButton.text(prefix + "close", "Close").onClick(PeakAction.close()));
            menu.add(panel);
            return menu;
        });

        PeakQuickTestRegistry.registerMenu("menu_tooltip_element", (player, menu) -> {
            PeakPanel tooltip = PeakPanel.create("peaktest:quick/menu_tooltip_element/tip")
                    .column().gap(2).padding(6).borderRadius(4)
                    .background(0xF0182030).border(1, 0xFFFFD700);
            tooltip.add(PeakLabel.create("peaktest:quick/menu_tooltip_element/title", "Diamond sword").color(0x55FFFF),
                    PeakLabel.create("peaktest:quick/menu_tooltip_element/body", "Attack damage: 7").color(0xAAAAAA),
                    PeakLabel.create("peaktest:quick/menu_tooltip_element/hint", "A whole element as a tooltip").color(0x55FF55));
            menu.add(PeakButton.text("peaktest:quick/menu_tooltip_element/button", "Hover me")
                    .absolute(PeakAnchor.CENTER, 0, 0)
                    .tooltip(tooltip));
            return menu;
        });

        PeakQuickTestRegistry.registerMenu("menu_tooltip", (player, menu) -> {
            menu.add(PeakButton.text("peaktest:quick/menu_tooltip/button", "Hover me").borderRadius(4)
                    .absolute(PeakAnchor.CENTER, 0, 0)
                    .tooltip("A tooltip on a button\nwith a second line"));
            return menu;
        });

        PeakQuickTestRegistry.registerMenu("char_menu", (player, menu) -> {
            menu.add(
                    PeakPanel
                            .create()
                            .borderRadius(4)
                            .size(PeakSize.percent(50,50))
                            .absolute(PeakAnchor.CENTER, 0, 0)
                            .add(PeakTableLayoutPanel
                                    .create().size(PeakSize.percent(100,100))
                                    .decor(s -> s.getLineMode())//lineMode(PeakTableStyle.LineMode.BOTH).lines(1,PeakPaint.RED)
                                    .columns(PeakTrack.percent(30),PeakTrack.percent(5),PeakTrack.percent(65))
                                    .rows(
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10),
                                            PeakTrack.percent(10)
                                    )
                                    .cellAlign(PeakAlign.START,PeakAlign.CENTER)
                                    .cellPadding(4)
                                            .row(PeakChibiPlayer.clientPlayer().rowSpan(10).size(PeakSize.percent(100,100)))
                                            .row(PeakSpacer.create(1,1), PeakLabel.create("GekidoLukas"))
                                            .row(PeakSpacer.create(1,1), PeakIconBar.create().verticalAlign(PeakAlign.END)
                                                            .fullIconTexture("minecraft:textures/gui/sprites/hud/heart/hardcore_full.png")
                                                            .emptyIconTexture("minecraft:textures/gui/sprites/hud/heart/container.png")
                                                            .halfIconTexture("minecraft:textures/gui/sprites/hud/heart/hardcore_half.png")
                                                            .emptyBehindFullAndHalf(true)
                                                            .maxValue(20)
                                                            .value(13)

                                                    )
                                            .row(PeakSpacer.create(1,1),PeakRichTextBox.create().placeholder("Hier reinschreiben").rowSpan(3))
                                            .empty()
//                                    .rowSpacing(3)
//                                    .columns(2)
//
//                                    .alignChildren(PeakAlign.CENTER)
//                                    .add(PeakDiv.create().size(PeakSize.percent(30,0)))
//                                    .add(PeakDiv.create().size(PeakSize.percent(70,0)))
//                                    .add(PeakChibiPlayer.clientPlayer().scale(2))
//                                    .add(PeakLabel.create("TestPlayer"))
                            )
            );
            return menu;
        });

        PeakQuickTestRegistry.registerHud("a_quick_test", (player, doc) -> {
            doc.deleteIfEmpty(true);
//            doc.add(
//                    PeakPopUpContainer.create("nexo:logo")
//                            .animateEnter(true)
//                            .animateExit(true)
//                            .enterAnimation(PeakPopupAnimation.SCALE)
//                            .exitAnimation(PeakPopupAnimation.FADE)
//                            .exitDurationTicks(30)
//                            .enterDurationTicks(30)
//                            .timeToLiveTicks(200)
//                            .autoRemoveOnExpire(true)
//                            .add(
//                                    PeakImage.create("minecraft:textures/gui/title/minecraft.png", 1024, 256)
//                                            .size(PeakSize.of(PeakSize.PeakDimension.pixels(512), PeakSize.PeakDimension.pixels(128))).objectFit(PeakObjectFit.COVER)
//                            )
//                            .size(PeakSize.of(PeakSize.PeakDimension.pixels(512), PeakSize.PeakDimension.pixels(128)))
//                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
//
//            );
            PeakLabel label = PeakLabel.create("§6Test: §f12.5§7x §f12.5§7x §f12.5§7x").countUp(30);
            doc.add(label);

            PeakTestTimeline.create(player)
                    .atSeconds(1, () -> PeakClient.to(player).updateElement(doc.getId(), label.getId(),
                            PeakPatch.label().text("§6Test: §f99.9§7x §f99.9§7x §f99.9§7x")))
                    .atSeconds(4, () -> PeakClient.to(player).updateElement(doc.getId(), label.getId(),
                            PeakPatch.label().text("§6Test: §f12.5§7x §f12.5§7x §f12.5§7x")))
                    .start();

//            doc.hideVanilla(PeakVanillaElement.SCOREBOARD);
            return doc;
        });

        PeakQuickTestRegistry.registerHud("charttest", (player, doc) -> {
            PeakBarChart votes = PeakBarChart.create()
                    .addEntry("Map A", 12, PeakPaint.rgb(85, 170, 255))
                    .addEntry("Map B", 8, PeakPaint.rgb(255, 170, 85))
                    .addEntry("Map C", 15, PeakPaint.rgb(85, 255, 120))
                    .valueLabelPosition(PeakValueLabelPosition.INSIDE)
                    .categoryLabelColor(PeakPaint.rgb(255, 255, 255))
                    .background(PeakPaint.rgba(0,0,0,100))
//                    .valueLabelColor(PeakPaint.rgb(20, 20, 20))
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0));

            PeakPieChart teamShare = PeakPieChart.create()
                    .donutThickness(10)
                    .legendPosition(PeakLegendPosition.BOTTOM)
                    .showLabels(true)
                    .addEntry("Red", 60, PeakPaint.rgb(220, 60, 60))
                    .addEntry("Blue", 40, PeakPaint.rgb(60, 100, 220))
                    .absolute(PeakPosition.of(PeakAnchor.TOP_RIGHT, -20, 20));
            doc.add(votes);
            doc.add(teamShare);
            return doc;
        });
        PeakQuickTestRegistry.registerHud("chibi_test", (player, doc) -> {
            doc.add(PeakChibiPlayer.clientPlayer()
                    .scale(2f)
                    .absolute(PeakPosition.of(PeakAnchor.CENTER_LEFT, 0, 0))
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(48), PeakSize.PeakDimension.pixels(64))));
            doc.add(PeakChibiPlayer.of(UUID.fromString("d7d0818c-4e7d-4ba0-b7e8-60a862396ac1"))
                    .scale(2f)
                            .rotationY(PeakChibiRotation.THREE_QUARTER_MIRRORED)
                    .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(48), PeakSize.PeakDimension.pixels(64))));
            doc.add(PeakChibiPlayer.of(UUID.fromString("da8118e3-8329-43a6-b856-d3f66d81bb7c"))
                    .scale(2f)
                    .absolute(PeakPosition.of(PeakAnchor.CENTER_RIGHT, 0, 0))
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(48), PeakSize.PeakDimension.pixels(64))));
            return doc;
        });
        PeakQuickTestRegistry.registerHud("playericon", (player, doc) -> {
            doc.add(PeakPlayerIcon.clientPlayer().view(PeakPlayerIconView.FACE).absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 10, 10)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));
            doc.add(PeakPlayerIcon.clientPlayer().view(PeakPlayerIconView.BUST).absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 50, 10)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));
            doc.add(PeakPlayerIcon.clientPlayer().view(PeakPlayerIconView.UPPER_BODY).absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 100, 10)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));
            doc.add(PeakPlayerIcon.clientPlayer().view(PeakPlayerIconView.FULL_BODY).absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 150, 10)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));

            doc.add(PeakPlayerIcon.of(UUID.fromString("d14a63cd-c293-4813-acda-45c5f5cbfb41")).view(PeakPlayerIconView.FACE).absolute(PeakPosition.of(PeakAnchor.CENTER, -75, 0)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));
            doc.add(PeakPlayerIcon.of(UUID.fromString("d14a63cd-c293-4813-acda-45c5f5cbfb41")).view(PeakPlayerIconView.BUST).absolute(PeakPosition.of(PeakAnchor.CENTER, -25, 0)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));
            doc.add(PeakPlayerIcon.of(UUID.fromString("d14a63cd-c293-4813-acda-45c5f5cbfb41")).view(PeakPlayerIconView.UPPER_BODY).absolute(PeakPosition.of(PeakAnchor.CENTER, 25, 0)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));
            doc.add(PeakPlayerIcon.of(UUID.fromString("d14a63cd-c293-4813-acda-45c5f5cbfb41")).view(PeakPlayerIconView.FULL_BODY).absolute(PeakPosition.of(PeakAnchor.CENTER, 75, 0)).size(PeakSize.of(PeakSize.PeakDimension.pixels(40), PeakSize.PeakDimension.pixels(40))));

            return doc;
        });

        PeakQuickTestRegistry.registerHud("minimap", (player, doc) -> {

            doc.add(
                    PeakMinimap.create("minecraft:textures/gui/presets/isles.png")
                    .imageSize(256, 256)
                    .worldBounds(-128, -128, 128, 128)
                    .viewRadiusBlocks(32)
                    .absolute(PeakPosition.of(PeakAnchor.TOP_LEFT, 10, 10))
                    .playerIconMode(PeakMinimapPlayerIconMode.TEXTURE)
                    .playerIconTexture("minecraft:textures/map/decorations/player.png")
                    .size(PeakSize.of(PeakSize.PeakDimension.pixels(128), PeakSize.PeakDimension.pixels(128)))
                            .worldBorderSource(PeakWorldBorderSource.CUSTOM).worldBorderShape(PeakBorderShape.SQUARE)
            );

            return doc;
        });

        PeakQuickTestRegistry.registerHud("jade", (player, doc) -> {
            doc.add(
                    PeakConditional.create().addCondition(PeakCondition.targetingEntity())
                            .add(
                                    PeakPanel.create()
                                            .style(
                                                    PeakStyle.create()
                                                            .background(0xF0100010)
                                                            .borderColor(0x505000FF)
                                                            .borderWidth(1)
                                                            .padding(2)
                                            )
                                            .maxSize(160,36)
                                            .minSize(160,36)

                                            .add(
                                                    PeakTableLayoutPanel.create().columns(2)
                                                            .add(
                                                                    PeakEntityElement.targetedEntity().yaw(210).copyMode(PeakEntityCopyMode.TYPE_AND_SKIN_EQUIPMENT)
                                                                            .size(PeakSize.of(PeakSize.PeakDimension.pixels(32), PeakSize.PeakDimension.pixels(32)))
                                                            )
                                                            .add(
                                                                    PeakTableLayoutPanel.create().columns(1).rowSpacing(2)
                                                                            .emptyRow(4)
                                                                            .add(PeakLabel.create("{mc:target_entity}"))
                                                                            .add(PeakLabel.create("{mc:target_entity_health}/{mc:target_entity_max_health}").color(0xFFFF4444))
                                                            )
                                            )
                            )
                            .maxSize(160,36)
                            .minSize(160,36)
                            .absolute(PeakPosition.of(PeakAnchor.TOP_CENTER, 0, 10))
            );
            return doc;
        });

        PeakQuickTestRegistry.registerHud("alphapanels", (player, doc) -> {
            List<BackgroundCase> backgrounds = List.of(
                    new BackgroundCase("A", PeakPaint.solid(0xFF0000), "0xFF0000: plain RGB, opaque red"),
                    new BackgroundCase("B", PeakPaint.solid(0xFFFF0000), "0xFFFF0000: explicit alpha FF, opaque red"),
                    new BackgroundCase("C", PeakPaint.solid(0x80FF0000), "0x80FF0000: half transparent red"),
                    new BackgroundCase("D", PeakPaint.solid(0x00FF0000), "0x00FF0000: alpha 0 with RGB counts as opaque red"),
                    new BackgroundCase("E", PeakPaint.solid(0), "0: transparent, the gradient is fully visible"),
                    new BackgroundCase("F", PeakPaint.hex("#00AA00"), "PeakPaint.hex(\"#00AA00\"): opaque green"),
                    new BackgroundCase("G", PeakPaint.BLUE.withAlpha(0.5f), "PeakPaint.BLUE.withAlpha(0.5f): half transparent blue"),
                    new BackgroundCase("H", PeakPaint.BLACK, "PeakPaint.BLACK: opaque black"),
                    new BackgroundCase("I", PeakPaint.solid(0x000000), "0x000000 equals 0: transparent")
            );
            PeakPanel row = PeakPanel.create("alphapanels-backgrounds").absolute(PeakAnchor.TOP_LEFT, 10, 10)
                    .row().gap(8).padding(8).background(PeakPaint.NONE);
            PeakPanel legend = PeakPanel.create("alphapanels-legend").absolute(PeakAnchor.TOP_LEFT, 10, 130)
                    .gap(2).padding(4).background(PeakPaint.BLACK.withAlpha(0.6f));
            for (BackgroundCase entry : backgrounds) {
                row.add(alphaCell("alphapanels-bg-" + entry.letter(), entry.letter(), entry.color(), 1, PeakPaint.WHITE));
                legend.add(PeakLabel.create(entry.letter() + " = " + entry.description()));
            }

            PeakPaint darkBackground = PeakPaint.hex("#303030");
            List<BorderCase> borders = List.of(
                    new BorderCase("J", PeakPaint.solid(0xFF0000), PeakPaint.NONE, "border 0xFF0000 on a transparent panel: plain RGB, opaque red"),
                    new BorderCase("K", PeakPaint.solid(0x80FF0000), PeakPaint.NONE, "border 0x80FF0000 on a transparent panel: half transparent red, the world shines through"),
                    new BorderCase("L", PeakPaint.solid(0), darkBackground, "border 0 on a dark panel: no border"),
                    new BorderCase("M", PeakPaint.solid(0x80FF0000), darkBackground, "border 0x80FF0000 on a dark panel: the background lies under the border, so it looks darker (like CSS border-box)")
            );
            PeakPanel borderRow = PeakPanel.create("alphapanels-borders").absolute(PeakAnchor.TOP_LEFT, 10, 70)
                    .row().gap(8).padding(8).background(PeakPaint.NONE);
            for (BorderCase entry : borders) {
                borderRow.add(alphaCell("alphapanels-border-" + entry.letter(), entry.letter(), entry.backgroundColor(), 4, entry.borderColor()));
                legend.add(PeakLabel.create(entry.letter() + " = " + entry.description()));
            }
            doc.add(row, borderRow, legend);
            player.sendSystemMessage(Component.literal("§e[alphapanels] §fExpected: A, B, D, F, H opaque; C and G half transparent; E and I see-through; "
                    + "J opaque red border; K half transparent red border with the world shining through; L dark panel without border; "
                    + "M dark red border (the dark background lies under the border)"));
            return doc;
        });

        PeakQuickTestRegistry.registerHud("alphapanelpatch", (player, doc) -> {
            String documentId = doc.getId();
            PeakPanel backdrop = PeakPanel.create("apatch-backdrop").absolute(PeakAnchor.TOP_LEFT, 10, 10)
                    .size(320, 140).padding(10).background(PeakPaint.NONE);
            backdrop.add(PeakLabel.create("apatch-header", "Alpha panel patch test").color(PeakPaint.YELLOW));
            backdrop.add(PeakPanel.create("apatch-panel").size(80, 40).padding(6).border(2, PeakPaint.WHITE)
                    .borderRadius(4).background(0xFF2255AA)
                    .add(PeakLabel.create("apatch-label", "Panel")));
            doc.add(backdrop);

            PeakTestTimeline.create(player)
                    .atSeconds(1, () -> alphaStep(player, documentId, 1, PeakPatch.panel().background(0x00FF00),
                            "background(0x00FF00)", "opaque green; border, padding and rounded corners stay"))
                    .atSeconds(3, () -> alphaStep(player, documentId, 2, PeakPatch.panel().background(0x80FF0000),
                            "background(0x80FF0000)", "half transparent red, the gradient shines through"))
                    .atSeconds(5, () -> alphaStep(player, documentId, 3, PeakPatch.panel().background(0),
                            "background(0)", "transparent; only border and label remain"))
                    .atSeconds(7, () -> alphaStep(player, documentId, 4, PeakPatch.panel().border(4, 0x80FFFF00),
                            "border(4, 0x80FFFF00)", "thicker half transparent yellow border; background stays transparent"))
                    .atSeconds(9, () -> alphaStep(player, documentId, 5, PeakPatch.panel().padding(16),
                            "padding(16)", "the label moves inward; border and background stay"))
                    .atSeconds(11, () -> alphaStep(player, documentId, 6, PeakPatch.panel().width(140),
                            "width(140)", "only the width grows, the height stays 40"))
                    .atSeconds(13, () -> alphaStep(player, documentId, 7, PeakPatch.panel().boxShadow(0xA0000000, 4, 4, 0),
                            "boxShadow(0xA0000000, 4, 4, 0)", "a hard half transparent shadow appears at the bottom right"))
                    .atSeconds(15, () -> alphaStep(player, documentId, 8, PeakPatch.panel().borderRadius(12),
                            "borderRadius(12)", "stronger rounded corners; everything else stays"))
                    .atSeconds(17, () -> alphaStep(player, documentId, 9, PeakPatch.panel().background(0xFF2255AA),
                            "background(0xFF2255AA)", "opaque blue again; no patched field reset another one"))
                    .start();
            return doc;
        });
    }

    private static PeakPanel percentBox(String text, int color) {
        return PeakPanel.create().background(color).add(PeakLabel.create(text).color(PeakPaint.WHITE));
    }

    private static void registerPercentQuickTest() {
        PeakQuickTestRegistry.registerHud("percentsizes", (player, doc) -> {
            doc.add(PeakPanel.create().size(PeakSize.percent(50,75)).absolute(PeakAnchor.BOTTOM_CENTER,0,0));
            PeakClient.to(player).sendUi(doc);
//            PeakPanel frame = PeakPanel.create("pct-frame").absolute(PeakAnchor.TOP_LEFT, 10, 10)
//                    .size(220, 150).padding(10).background(0xFF202030).border(1, PeakPaint.GRAY);
//            frame.add(percentBox("A: width 50%, height 16", 0xFFAA3333).widthPercent(50).height(16));
//            frame.add(percentBox("B: width 100% of the content box (200)", 0xFF33AA33).widthPercent(100).height(16));
//            PeakPanel row = PeakPanel.create().row().gap(0).widthPercent(100).height(18);
//            row.add(percentBox("25", 0xFF3333AA).widthPercent(25).heightPercent(100),
//                    percentBox("25", 0xFFAA33AA).widthPercent(25).heightPercent(100),
//                    percentBox("50", 0xFF33AAAA).widthPercent(50).heightPercent(100));
//            frame.add(row);
//            PeakPanel outer = percentBox("C: 50% wide, 40 high", 0xFF555555).widthPercent(50).height(40);
//            outer.add(percentBox("50% of C", 0xFFAA7733).widthPercent(50).height(12));
//            frame.add(outer);
//            doc.add(frame);
//
//            PeakPanel box = PeakPanel.create("pct-box").absolute(PeakAnchor.TOP_LEFT, 250, 10)
//                    .size(140, 90).padding(10).background(0xFF202030).border(1, PeakPaint.GRAY);
//            box.add(PeakPanel.create().absolute(PeakAnchor.TOP_LEFT, 0, 0).widthPercent(100).heightPercent(100)
//                    .background(PeakPaint.YELLOW.withAlpha(0.3f)));
//            box.add(percentBox("50%", 0xFFAA3333).absolute(PeakAnchor.BOTTOM_RIGHT, 0, 0).widthPercent(50).heightPercent(50));
//            doc.add(box);
//
//            PeakPanel auto = PeakPanel.create("pct-auto").absolute(PeakAnchor.TOP_LEFT, 250, 120)
//                    .padding(6).background(0xFF202030).border(1, PeakPaint.GRAY);
//            auto.add(PeakLabel.create("Auto-sized container"));
//            auto.add(PeakLabel.create("child with 50% width counts as auto").widthPercent(50));
//            doc.add(auto);
//
//            player.sendSystemMessage(Component.literal("§e[percentsizes] §fLeft frame (220 wide, padding 10, content 200): A is 100 px wide, B fills 200 px, "
//                    + "the three row cells are 50, 50 and 100 px wide and as high as the row (18), C is 100 px wide and its child 50 px. "
//                    + "Right box (140x90, padding 10): the yellow layer covers the whole box including the padding, the red layer is a quarter of the box in the bottom right corner. "
//                    + "Lower box: a percent child of an auto-sized container is as wide as its content."));
            return doc;
        });
    }

    private static PeakPanel alphaCell(String id, String text, PeakPaint background, int borderWidth, PeakPaint borderColor) {
        return PeakPanel.create(id).size(36, 36).background(background).border(borderWidth, borderColor)
                .add(PeakLabel.create(id + "-text", text).color(PeakPaint.WHITE));
    }

    private static void alphaStep(ServerPlayer player, String documentId, int step, PeakPanelPatch patch, String what, String expected) {
        PeakTargets self = PeakClient.to(player);
        self.updateElement(documentId, "apatch-panel", patch);
        self.updateElement(documentId, "apatch-header", PeakPatch.label().text("Step " + step + ": " + what));
        player.sendSystemMessage(Component.literal("§e[alphapanelpatch] §fStep " + step + ": " + what + " §7-> expected: " + expected));
    }

    private record BackgroundCase(String letter, PeakPaint color, String description) {}

    private record BorderCase(String letter, PeakPaint borderColor, PeakPaint backgroundColor, String description) {}
}
