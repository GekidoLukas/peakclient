package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.gekidolukas.peakclienttest.common.PeakTestTimeline;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class ExampleMenuElementTest implements PeakElementTest {
    private static final String SHOP = "peaktest:example/shop";
    private static boolean handlerRegistered;

    @Override public String name() { return "examplemenu"; }

    @Override
    public String description() {
        return "Docs example \"A notification and a menu\": notify, a shop menu with a server handler for the buy button";
    }

    private static synchronized void registerHandler() {
        if (handlerRegistered) return;
        handlerRegistered = true;
        PeakClient.menu(SHOP).on("buy", event -> {
            event.player().sendSystemMessage(Component.literal("§e[examplemenu] §fYou bought " + event.arg("item")));
            PeakClient.to(event.player()).menus().back();
        });
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("notification and shop menu from the examples page: click Buy sword, the server answers in chat and closes the menu", 600,
                        (player, docId) -> {
                            registerHandler();
                            List<ServerPlayer> players = List.of(player);

                            PeakClient.to(players).notify("Round starts in 10 seconds");

                            PeakDocument shop = PeakMenu.create(SHOP, settings -> settings.title("Shop"));
                            shop.add(PeakPanel.create().absolute(PeakAnchor.CENTER, 0, 0).column().gap(6).padding(10).width(200)
                                    .add(PeakLabel.create("peaktest:example/shop/title", "Shop"),
                                         PeakButton.text("peaktest:example/shop/buy", "Buy sword")
                                                 .onClick(PeakAction.send("buy").arg("item", "sword")),
                                         PeakButton.text("peaktest:example/shop/close", "Close").onClick(PeakAction.close())));

                            PeakClient.menu(SHOP).on("buy", event -> {
                                if(event.player() instanceof ServerPlayer serverPlayer) {
                                    serverPlayer.addItem(Items.DIAMOND_SWORD.getDefaultInstance());
                                }
                                PeakClient.to(event.player()).menus().back();
                            });

                            PeakClient.to(player).menus().sendAndOpen(shop);

                            PeakTestTimeline timeline = PeakTestTimeline.create(player);
                            timeline.at(580, () -> {});
                            timeline.onCancel(() -> PeakClient.to(player).menus().remove(SHOP));
                            timeline.start();
                        })
        );
    }
}
