package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class LabelElementTest implements PeakElementTest {
    @Override public String name() { return "label"; }
    @Override public String description() { return "PeakLabel: color, shadow, translation, gradients, formatting codes, placeholders, wrapping"; }

    private static void lines(PeakDocument doc, String... texts) {
        for (int i = 0; i < texts.length; i++) {
            doc.add(PeakLabel.create(texts[i]).absolute(PeakAnchor.TOP_LEFT, 20, 20 + i * 12));
        }
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.document("color: plain colored text", 60, doc ->
                        doc.add(PeakLabel.create("Plain colored text").color(0x55FF55).absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("shadow: on (top) vs. off (bottom)", 60, doc -> {
                    doc.add(PeakLabel.create("Shadow on").absolute(PeakAnchor.TOP_CENTER, 0, 10));
                    doc.add(PeakLabel.create("Shadow off").shadow(false).absolute(PeakAnchor.TOP_CENTER, 0, 26));
                }),
                PeakElementTestStep.document("translation: key without arguments", 60, doc ->
                        doc.add(PeakLabel.translatable("gui.done").absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("translation: key with an argument (Seed: 1234)", 60, doc ->
                        doc.add(PeakLabel.translatable("commands.seed.success", "1234").absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("components: red, bold green and plain parts in one label; Seed with a yellow Component argument; the jump key in aqua", 80, doc -> {
                    doc.add(PeakLabel.create(Component.literal("Red ").withStyle(ChatFormatting.RED)
                            .append(Component.literal("bold green").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .append(Component.literal(" and plain"))).absolute(PeakAnchor.TOP_CENTER, 0, 10));
                    doc.add(PeakLabel.translatable("commands.seed.success", Component.literal("1234").withStyle(ChatFormatting.YELLOW))
                            .absolute(PeakAnchor.TOP_CENTER, 0, 26));
                    doc.add(PeakLabel.create(Component.keybind("key.jump").withStyle(ChatFormatting.AQUA)).absolute(PeakAnchor.TOP_CENTER, 0, 42));
                }),
                PeakElementTestStep.document("fonts: default, minecraft:alt (enchanting glyphs), minecraft:illageralt, minecraft:uniform", 100, doc -> {
                    doc.add(PeakLabel.create(Component.literal("Default font")).absolute(PeakAnchor.TOP_CENTER, 0, 10));
                    doc.add(PeakLabel.create(Component.literal("Alt font").withStyle(Style.EMPTY.withFont(ResourceLocation.parse("minecraft:alt"))))
                            .absolute(PeakAnchor.TOP_CENTER, 0, 26));
                    doc.add(PeakLabel.create(Component.literal("Illager font").withStyle(Style.EMPTY.withFont(ResourceLocation.parse("minecraft:illageralt"))))
                            .absolute(PeakAnchor.TOP_CENTER, 0, 42));
                    doc.add(PeakLabel.create(Component.literal("Uniform font ").withStyle(Style.EMPTY.withFont(ResourceLocation.parse("minecraft:uniform")))
                            .append(Component.literal("then default again")))
                            .absolute(PeakAnchor.TOP_CENTER, 0, 58));
                }),
                PeakElementTestStep.document("style inheritance: the bold parent keeps 'bold', the child with an explicit bold=false is thin, the last part is bold again", 100, doc ->
                        doc.add(PeakLabel.create(Component.literal("Bold ").withStyle(ChatFormatting.BOLD)
                                .append(Component.literal("thin ").withStyle(Style.EMPTY.withBold(false)))
                                .append(Component.literal("bold again"))).absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("click, hover and insertion are removed: the link text is shown plain and nothing happens when clicked in a menu", 100, doc ->
                        doc.add(PeakLabel.create(Component.literal("Looks like a link").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://example.org"))
                                .withInsertion("inserted").withColor(ChatFormatting.AQUA))).absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("underline, strikethrough and obfuscated with shadow, plus an animated gradient over a bold underlined line", 120, doc -> {
                    doc.add(PeakLabel.create(Component.literal("Underlined ").withStyle(ChatFormatting.UNDERLINE)
                            .append(Component.literal("struck ").withStyle(ChatFormatting.STRIKETHROUGH))
                            .append(Component.literal("obfuscated").withStyle(ChatFormatting.OBFUSCATED))).absolute(PeakAnchor.TOP_CENTER, 0, 10));
                    doc.add(PeakLabel.create(Component.literal("Gradient bold underlined").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                            .color(PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55, 0xFF55FF55).animated(40))
                            .absolute(PeakAnchor.TOP_CENTER, 0, 26));
                }),
                PeakElementTestStep.document("placeholder styles: your display name keeps its team color (join a team first), the gold text around it stays gold", 160, doc ->
                        doc.add(PeakLabel.create("§6Hello {mc:player_display_name}§6, welcome!").absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("gradient: static, the end must be clearly blue", 60, doc ->
                        doc.add(PeakLabel.create("Static rainbow gradient")
                                .color(PeakPaint.gradient(0xFFFF0000, 0xFF00FF00, 0xFF0000FF))
                                .absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("gradient: animated, cycles smoothly without a jump", 120, doc ->
                        doc.add(PeakLabel.create("Animated pulsing gradient")
                                .color(PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55, 0xFF55FF55).animated(40))
                                .absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("formatting codes: the shadow follows each color segment, hex color below", 80, doc -> {
                    doc.add(PeakLabel.create("§eYellow §cRed §aGreen §9Blue §rNormal").absolute(PeakAnchor.TOP_CENTER, 0, 10));
                    doc.add(PeakLabel.create(PeakTextFormatting.colored("Hex color RGB", 0xFF8800)).absolute(PeakAnchor.TOP_CENTER, 0, 26));
                }),
                PeakElementTestStep.document("placeholders: built-in values", 80, doc ->
                        doc.add(PeakLabel.create("FPS: {mc:fps} | Pos: {mc:x=1} {mc:y=1} {mc:z=1}").absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("placeholders: identity (name, display name, uuid, world, ping, players online)", 160, doc ->
                        lines(doc,
                                "Name: {mc:player_name}",
                                "Display name: {mc:player_display_name}",
                                "UUID: {mc:player_uuid}",
                                "World (singleplayer only): {mc:world_name#none}",
                                "Ping: {mc:ping} ms | Players online: {mc:players_online}")),
                PeakElementTestStep.document("placeholders: world and time (block pos, chunk, game clock, day, weather, moon, light, real time)", 160, doc ->
                        lines(doc,
                                "Block: {mc:block_x} {mc:block_y} {mc:block_z} | Chunk: {mc:chunk_x} {mc:chunk_z}",
                                "Game time: {mc:time} | Day {mc:day} | Weather: {mc:weather} | Moon phase: {mc:moon_phase}",
                                "Light: {mc:light} (block {mc:light=block}, sky {mc:light=sky})",
                                "Real time: {mc:time_real} | {mc:time_real=HH:mm:ss} | {mc:time_real=dd.MM.yyyy}",
                                "Version: {mc:minecraft_version} | Biome: {mc:biome} | Dimension: {mc:dimension}")),
                PeakElementTestStep.document("placeholders: player status (move, sneak, hold a tool, switch the hotbar slot)", 240, doc ->
                        lines(doc,
                                "Gamemode: {mc:gamemode} | Slot: {mc:player_selected_slot}",
                                "Hunger: {mc:player_hunger=0} | Saturation: {mc:player_saturation=1} | Absorption: {mc:player_absorption=0}",
                                "XP: level {mc:player_experience_level} | total {mc:player_xp_total}",
                                "Held: {mc:player_selected_item_name#nothing} x{mc:held_item_count} | durability {mc:held_item_durability#-}/{mc:held_item_max_durability#-}",
                                "Offhand: {mc:player_offhand_item_name#nothing}",
                                "Speed: {mc:speed=2} blocks/s | Yaw: {mc:yaw=0} | Pitch: {mc:pitch=0} | Effects: {mc:effect_count}")),
                PeakElementTestStep.document("placeholders: targets (look at a block, a mob and another player)", 240, doc ->
                        lines(doc,
                                "Block: {mc:target_block#none} ({mc:target_block_id#-})",
                                "Entity: {mc:target_entity#none} ({mc:target_entity_type#-})",
                                "Player: {mc:target_player_name#none}",
                                "Health: {mc:target_entity_health#-}/{mc:target_entity_max_health#-} | Armor: {mc:target_entity_armor#-}")),
                PeakElementTestStep.of("placeholders: team and score (join a team and display an objective, commands are in the chat)", 300,
                        (player, docId) -> {
                            player.sendSystemMessage(Component.literal("§e[label] §f/team add red, /team modify red color red, /team join red @s, "
                                    + "/scoreboard objectives add kills dummy, /scoreboard objectives setdisplay sidebar kills, "
                                    + "/scoreboard players set @s kills 7"));
                            PeakDocument doc = PeakDocument.create(docId);
                            lines(doc,
                                    "Team: {mc:team_name#no team} | Color: {mc:team_color#-}",
                                    "Kills: {mc:score=kills#-}");
                            doc.add(PeakLabel.create("Colored by the team color").bind("color", "{mc:team_color#FFFFFF}")
                                    .absolute(PeakAnchor.TOP_LEFT, 20, 70));
                            PeakClient.to(player).sendUi(doc);
                        }),
                PeakElementTestStep.document("placeholders as bindings: the red box disappears while sneaking, the labels print the flags", 240, doc -> {
                    lines(doc,
                            "Sneaking: {mc:is_sneaking} | Sprinting: {mc:is_sprinting} | Swimming: {mc:is_swimming}",
                            "Flying: {mc:is_flying} | On ground: {mc:is_on_ground} | In water: {mc:is_in_water} | Riding: {mc:is_riding}",
                            "Raining: {mc:is_raining} | Day: {mc:is_day} | Target block: {mc:has_target_block} | Target entity: {mc:has_target_entity}");
                    doc.add(PeakPanel.create("peaktest:label/sneak-box").size(60, 20).background(0xFFFF5555)
                            .bind("hidden", "{mc:is_sneaking}").absolute(PeakAnchor.TOP_LEFT, 20, 70));
                }),
                PeakElementTestStep.document("wrapWidth: word wrap keeps the color across lines", 70, doc ->
                        doc.add(PeakLabel.create("§eThis is a long yellow sentence that should wrap onto multiple lines while staying yellow throughout")
                                .wrapWidth(140).absolute(PeakAnchor.TOP_CENTER, 0, 10))),
                PeakElementTestStep.document("lineSpacing: 0 (left) vs. 4 (right) with the same wrapWidth", 70, doc -> {
                    String text = "A sentence that wraps over several lines";
                    doc.add(PeakLabel.create(text).wrapWidth(90).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(PeakLabel.create(text).wrapWidth(90).lineSpacing(4).absolute(PeakAnchor.TOP_LEFT, 140, 20));
                }),
                PeakElementTestStep.of("updateElementsByClass: after 2 s both 'group' labels turn green, the third stays gray", 100, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Before: group A").addClass("peaktest-group").color(0xAAAAAA)
                            .absolute(PeakAnchor.TOP_CENTER, 0, 10));
                    doc.add(PeakLabel.create("Before: group B").addClass("peaktest-group").color(0xAAAAAA)
                            .absolute(PeakAnchor.TOP_CENTER, 0, 26));
                    doc.add(PeakLabel.create("Untouched: no class").color(0xAAAAAA)
                            .absolute(PeakAnchor.TOP_CENTER, 0, 42));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(2, () -> PeakClient.to(player).updateElementsByClass(docId, "peaktest-group",
                                    PeakPatch.label().text("After: updated by class").color(0x55FF55)))
                            .start();
                }),
                PeakElementTestStep.of("count-up: the score counts up in 1 s steps (0, 120, 450, -30), the box does not jump (auto size, prefix and suffix stay)", 260, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakLabel score = PeakLabel.create("Score: 0 pts").countUp(20);
                    doc.add(PeakPanel.create().padding(4).add(score).absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline timeline = PeakTestTimeline.create(player);
                    int[] values = {120, 450, -30, 1000};
                    for (int i = 0; i < values.length; i++) {
                        int value = values[i];
                        timeline.atSeconds(1 + i * 2, () -> PeakClient.to(player).updateElement(docId, score.getId(),
                                PeakPatch.label().text("Score: " + value + " pts")));
                    }
                    timeline.start();
                }),
                PeakElementTestStep.of("count-up with decimals and colors: 12.5 grows to 99.9 and back; the color codes and both decimals stay", 220, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakLabel ratio = PeakLabel.create("§6Ratio: §f12.5§7x").countUp(30);
                    doc.add(ratio.absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(1, () -> PeakClient.to(player).updateElement(docId, ratio.getId(),
                                    PeakPatch.label().text("§6Ratio: §f99.9§7x")))
                            .atSeconds(4, () -> PeakClient.to(player).updateElement(docId, ratio.getId(),
                                    PeakPatch.label().text("§6Ratio: §f12.5§7x")))
                            .start();
                }),
                PeakElementTestStep.of("count-up from a placeholder: {peaktest:score} changes every 1.5 s and the label counts, a gradient color stays smooth", 260, (player, docId) -> {
                    PeakClient.to(player).setValue("peaktest:score", 0);
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Team score: {peaktest:score}").countUp(25)
                            .color(PeakPaint.gradient(0xFFFFFF55, 0xFFFF5555))
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline timeline = PeakTestTimeline.create(player);
                    int[] values = {40, 85, 300, 120};
                    for (int i = 0; i < values.length; i++) {
                        int value = values[i];
                        timeline.atSeconds(1 + i * 1.5, () -> PeakClient.to(player).setValue("peaktest:score", value));
                    }
                    timeline.start();
                }),
                PeakElementTestStep.of("component values: a PeakValueKey.ofComponent keeps its style, a translatable value follows the game language, the text around stays gold; after 3 s both change", 200, (player, docId) -> {
                    PeakValueKey<Component> who = PeakValueKey.ofComponent("peaktest:who");
                    PeakValueKey<Component> seed = PeakValueKey.ofComponent("peaktest:seed");
                    PeakClient.to(player).setValue(who, Component.literal(player.getGameProfile().getName()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    PeakClient.to(player).setValue(seed, Component.translatable("commands.seed.success", Component.literal("1234").withStyle(ChatFormatting.YELLOW)));
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("§6Hello " + who.placeholder() + "§6, welcome!").absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(PeakLabel.create(seed.placeholder()).absolute(PeakAnchor.TOP_LEFT, 20, 34));
                    doc.add(PeakLabel.create("Missing value: {peaktest:nothing#fallback text}").absolute(PeakAnchor.TOP_LEFT, 20, 48));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline timeline = PeakTestTimeline.create(player);
                    timeline.atSeconds(3, () -> {
                        PeakClient.to(player).setValue(who, Component.literal("Someone Else").withStyle(Style.EMPTY.withFont(ResourceLocation.parse("minecraft:alt"))));
                        PeakClient.to(player).setValue(seed, Component.translatable("gui.done").withStyle(ChatFormatting.GREEN));
                    });
                    timeline.start();
                }),
                PeakElementTestStep.document("typewriter: 20 chars per second (top), 60 with a wrap width and a gradient that must not squeeze (middle), colored (bottom)", 260, doc -> {
                    doc.add(PeakLabel.create("The quick brown fox jumps over the lazy dog").typewriter(20f)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    doc.add(PeakLabel.create("A longer sentence with a rainbow gradient that wraps over several lines of text")
                            .typewriter(60f).wrapWidth(140).color(PeakPaint.gradient(0xFFFF5555, 0xFF55FF55, 0xFF5555FF))
                            .absolute(PeakAnchor.TOP_LEFT, 20, 50));
                    doc.add(PeakLabel.create("§6Gold §bblue §aand green §ftext").typewriter(15f)
                            .absolute(PeakAnchor.TOP_LEFT, 20, 110));
                }),
                PeakElementTestStep.of("typewriter control: text is appended after 3 s (continues), replaced after 6 s (restarts), replayed after 9 s", 300, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakLabel label = PeakLabel.create("Wave 1 begins.").typewriter(15f);
                    doc.add(label.absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(3, () -> PeakClient.to(player).updateElement(docId, label.getId(),
                                    PeakPatch.label().text("Wave 1 begins. Hold the line!")))
                            .atSeconds(6, () -> PeakClient.to(player).updateElement(docId, label.getId(),
                                    PeakPatch.label().text("Wave 2 is coming.")))
                            .atSeconds(9, () -> PeakClient.to(player).updateElement(docId, label.getId(), PeakPatch.label().replay()))
                            .start();
                }),
                PeakElementTestStep.of("typewriter and count-up together: the sentence is typed once, later score changes only count the number", 300, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    PeakLabel label = PeakLabel.create("Wave 3 cleared: 100 points").typewriter(20f).countUp(20);
                    doc.add(label.absolute(PeakAnchor.TOP_LEFT, 20, 20));
                    PeakClient.to(player).sendUi(doc);
                    PeakTestTimeline.create(player)
                            .atSeconds(4, () -> PeakClient.to(player).updateElement(docId, label.getId(),
                                    PeakPatch.label().text("Wave 3 cleared: 250 points")))
                            .atSeconds(6, () -> PeakClient.to(player).updateElement(docId, label.getId(),
                                    PeakPatch.label().text("Wave 3 cleared: 90 points")))
                            .start();
                })
        );
    }
}
