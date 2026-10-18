package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.api.PeakVanillaHud;
import net.gekidolukas.peakclient.server.PeakAudience;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class VanillaHudElementTest implements PeakElementTest {
    @Override public String name() { return "vanillahud"; }
    @Override public String description() { return "Vanilla HUD: hide, show, offset, groups, document bound hiding and audience bound hiding"; }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("§e[vanillahud] §f" + text));
    }

    private static void begin(ServerPlayer player, String text) {
        PeakClient.to(player).vanillaHud().reset();
        say(player, text);
    }

    private static PeakVanillaHud hud(ServerPlayer player) {
        return PeakClient.to(player).vanillaHud();
    }

    private static PeakTestTimeline timeline(ServerPlayer player) {
        return PeakTestTimeline.create(player);
    }

    @Override
    public List<PeakElementTestStep> steps() {
        List<PeakElementTestStep> steps = new ArrayList<>();

        steps.add(PeakElementTestStep.of("status bars one after the other: health, armor, hunger (2 seconds each)", 140, (player, docId) -> {
            begin(player, "Health bar disappears for 2 seconds, then the armor bar, then the hunger bar. Wear armor and take damage to see all three.");
            hud(player).hide(PeakVanillaElement.HEALTH_BAR);
            timeline(player)
                    .at(40, () -> hud(player).show(PeakVanillaElement.HEALTH_BAR).hide(PeakVanillaElement.ARMOR_BAR))
                    .at(80, () -> hud(player).show(PeakVanillaElement.ARMOR_BAR).hide(PeakVanillaElement.HUNGER_BAR))
                    .at(120, () -> hud(player).reset())
                    .start();
        }));

        steps.add(PeakElementTestStep.of("air bubbles hidden for 5 seconds (stay under water)", 140, (player, docId) -> {
            begin(player, "Dive now. The air bubbles must stay invisible for 5 seconds.");
            hud(player).hide(PeakVanillaElement.AIR_BAR);
            timeline(player).at(100, () -> hud(player).reset()).start();
        }));

        steps.add(PeakElementTestStep.of("hotbar, offhand and experience hidden together for 3 seconds", 100, (player, docId) -> {
            begin(player, "Hotbar, offhand slot and experience bar must disappear, then return. Hold an offhand item.");
            hud(player).hide(PeakVanillaElement.HOTBAR, PeakVanillaElement.OFFHAND, PeakVanillaElement.EXPERIENCE_BAR);
            timeline(player).at(60, () -> hud(player).reset()).start();
        }));

        steps.add(PeakElementTestStep.of("crosshair, held item name and status effects hidden for 3 seconds", 100, (player, docId) -> {
            begin(player, "Crosshair, the item name above the hotbar and the status effect icons must disappear. Hold an item and have an effect.");
            hud(player).hide(PeakVanillaElement.CROSSHAIR, PeakVanillaElement.HELD_ITEM_NAME, PeakVanillaElement.STATUS_EFFECTS);
            timeline(player).at(60, () -> hud(player).reset()).start();
        }));

        steps.add(PeakElementTestStep.of("scoreboard sidebar and tab list hidden for 4 seconds", 120, (player, docId) -> {
            begin(player, "Needs a scoreboard sidebar (/scoreboard objectives setdisplay sidebar <objective>). Hold Tab, it must show nothing for 4 seconds.");
            hud(player).hide(PeakVanillaElement.SCOREBOARD, PeakVanillaElement.TAB_LIST);
            timeline(player).at(80, () -> hud(player).reset()).start();
        }));

        steps.add(PeakElementTestStep.of("chat hidden for 3 seconds", 120, (player, docId) -> {
            begin(player, "The chat disappears in one second for 3 seconds. This message and the next must be visible again afterwards.");
            timeline(player)
                    .at(20, () -> hud(player).hide(PeakVanillaElement.CHAT))
                    .at(80, () -> {
                        hud(player).reset();
                        say(player, "Chat is back.");
                    })
                    .start();
        }));

        steps.add(PeakElementTestStep.of("action bar and title hidden while they are shown", 140, (player, docId) -> {
            begin(player, "An action bar text and a title are sent. Both must stay invisible, then a second pair appears and must be visible.");
            hud(player).hide(PeakVanillaElement.ACTION_BAR, PeakVanillaElement.TITLE);
            player.displayClientMessage(Component.literal("Hidden action bar"), true);
            player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 5));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Hidden title")));
            timeline(player)
                    .at(70, () -> {
                        hud(player).reset();
                        player.displayClientMessage(Component.literal("Visible action bar"), true);
                        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Visible title")));
                    })
                    .start();
        }));

        steps.add(PeakElementTestStep.of("offset: hotbar 20 up, health bar 30 right for 4 seconds", 120, (player, docId) -> {
            begin(player, "The hotbar moves 20 pixels up, the health bar 30 pixels right, then both return.");
            hud(player).offset(PeakVanillaElement.HOTBAR, 0, -20).offset(PeakVanillaElement.HEALTH_BAR, 30, 0);
            timeline(player).at(80, () -> hud(player).reset()).start();
        }));

        steps.add(PeakElementTestStep.of("offset on an element that cannot move is ignored with one warning", 80, (player, docId) -> {
            begin(player, "The chat must stay where it is, the server log shows one warning.");
            hud(player).offset(PeakVanillaElement.CHAT, 0, -40);
        }));

        steps.add(PeakElementTestStep.of("hide and offset stay independent: offset first, then hide, then show", 140, (player, docId) -> {
            begin(player, "Hotbar moves 20 up, disappears for 2 seconds, and comes back at the moved position.");
            hud(player).offset(PeakVanillaElement.HOTBAR, 0, -20);
            timeline(player)
                    .at(30, () -> hud(player).hide(PeakVanillaElement.HOTBAR))
                    .at(70, () -> hud(player).show(PeakVanillaElement.HOTBAR))
                    .at(120, () -> hud(player).reset())
                    .start();
        }));

        steps.add(PeakElementTestStep.of("document bound: the hotbar is hidden while the document is shown", 100, (player, docId) -> {
            begin(player, "A document with hideVanilla(HOTBAR) is shown for 4 seconds, the hotbar returns when it ends.");
            PeakDocument doc = PeakDocument.create(docId).hideVanilla(PeakVanillaElement.HOTBAR);
            doc.add(PeakLabel.create("vanillahud-doc", "The hotbar is replaced by this document").absolute(PeakAnchor.BOTTOM_CENTER, 0, -10));
            PeakClient.to(player).sendUi(doc);
        }));

        steps.add(PeakElementTestStep.of("audience bound: everything except the chat is hidden, close() restores it", 140, (player, docId) -> {
            begin(player, "Everything except the chat is hidden for 4 seconds, then audience.close() brings it back.");
            PeakAudience audience = PeakClient.audience(List.of(player));
            audience.vanillaHud().hideAllExcept(PeakVanillaElement.CHAT);
            audience.after(80, audience::close);
        }));

        return steps;
    }
}
