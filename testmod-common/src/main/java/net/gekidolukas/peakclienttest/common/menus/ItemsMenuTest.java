package net.gekidolukas.peakclienttest.common.menus;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakItemSlot;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclienttest.common.PeakMenuChecks;
import net.gekidolukas.peakclienttest.common.PeakMenuTest;
import net.gekidolukas.peakclienttest.common.PeakMenuTestStep;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.button;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.frame;
import static net.gekidolukas.peakclienttest.common.PeakMenuTestKit.say;

public final class ItemsMenuTest implements PeakMenuTest {
    private static final String MENU = "peaktest:menu/items";
    private static final List<String> SLOTS = List.of("items:sword", "items:apples", "items:pickaxe", "items:special", "items:price",
            "items:custom", "items:plain", "items:faded", "items:disabled", "items:hotbar");

    @Override public String name() { return "items"; }
    @Override public String description() { return "Item slots: count, durability, item tooltip, selection, right and double click"; }

    @Override
    public void registerHandlers() {
        PeakClient.menu(MENU)
                .on("slot-click", event -> {
                    String clicked = event.elementId();
                    for (String id : SLOTS) {
                        PeakClient.to(event.player()).menus().update(MENU, id, PeakPatch.itemSlot().selected(id.equals(clicked)));
                    }
                })
                .on("slot-right", event -> say(event.player(), "right click on " + event.elementId()))
                .on("slot-double", event -> say(event.player(), "double click on " + event.elementId()));
    }

    private static PeakItemSlot slot(String id, ItemStack stack) {
        return PeakItemSlot.fixed(id, stack)
                .onClick(PeakAction.send("slot-click"))
                .onRightClick(PeakAction.send("slot-right"))
                .onDoubleClick(PeakAction.send("slot-double"));
    }

    private static void open(ServerPlayer player) {
        PeakDocument menu = PeakMenu.create(MENU, settings -> settings.title("Items"));

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack apples = new ItemStack(Items.APPLE, 16);
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(pickaxe.getMaxDamage() / 2);
        ItemStack special = new ItemStack(Items.GOLDEN_APPLE, 3);
        special.set(DataComponents.CUSTOM_NAME, Component.literal("Shop Special").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        ItemStack priced = new ItemStack(Items.EMERALD, 5);

        PeakPanel row = PeakPanel.create("items:row").row().gap(4).padding(0).background(0);
        row.add(slot("items:sword", sword),
                slot("items:apples", apples),
                slot("items:pickaxe", pickaxe).showDurability(false),
                slot("items:special", special),
                slot("items:price", priced).countText("§e5g"),
                slot("items:styled", new ItemStack(Items.GOLD_INGOT)).countText(Component.literal("x2").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)),
                slot("items:font", new ItemStack(Items.BOOK)).countText(Component.literal("ab").withStyle(Style.EMPTY.withFont(ResourceLocation.parse("minecraft:alt")))));

        PeakPanel second = PeakPanel.create("items:row2").row().gap(4).padding(0).background(0);
        second.add(slot("items:custom", new ItemStack(Items.NETHER_STAR)).tooltip("Custom tooltip text"),
                slot("items:plain", new ItemStack(Items.TNT)).itemTooltip(false),
                slot("items:faded", new ItemStack(Items.DIAMOND)).opacity(0.5f),
                slot("items:disabled", new ItemStack(Items.BARRIER)).enabled(false),
                PeakItemSlot.inventorySlot("items:hotbar", 0)
                        .onClick(PeakAction.send("slot-click")));

        menu.add(frame("Item slots",
                PeakLabel.create("items:l1", "Click, right click, double click").color(0xAAAAAA).wrapWidth(180), row,
                PeakLabel.create("items:l2", "Custom tooltip, no tooltip, faded, disabled, your first hotbar slot")
                        .color(0xAAAAAA).wrapWidth(180), second,
                button("items:close", "Close", PeakAction.close())));
        PeakClient.to(player).menus().sendAndOpen(menu);
    }

    @Override
    public List<PeakMenuTestStep> steps() {
        return List.of(
                PeakMenuTestStep.of("Slots show items, counts and durability")
                        .setup(ItemsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Five slots: a diamond sword, 16 apples (count at the bottom right), an iron pickaxe without a durability bar although it is damaged, 3 golden apples and emeralds with a yellow '5g' instead of the count"))
                        .check(PeakMenuChecks.manual("The second row: nether star, TNT, a half transparent diamond, a dimmed barrier and the item of your first hotbar slot")),

                PeakMenuTestStep.of("The item tooltip appears on hover")
                        .setup(ItemsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.manual("Hover the sword: its name (white, common rarity like in vanilla) and the attack damage lines as in the inventory; hover 'Shop Special': a gold bold name; F3+H adds the technical lines"))
                        .check(PeakMenuChecks.manual("The nether star shows 'Custom tooltip text' instead of the item tooltip, the TNT shows no tooltip at all")),

                PeakMenuTestStep.of("A click selects a slot through a server patch")
                        .setup(ItemsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the apples", "slot-click", event -> "items:apples".equals(event.elementId())))
                        .check(PeakMenuChecks.manual("Only the apples have a gold border now"))
                        .check(PeakMenuChecks.action("Click the sword", "slot-click", event -> "items:sword".equals(event.elementId())))
                        .check(PeakMenuChecks.manual("The border moved to the sword")),

                PeakMenuTestStep.of("Right click and double click")
                        .setup(ItemsMenuTest::open)
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Right-click the pickaxe", "slot-right", event -> "items:pickaxe".equals(event.elementId())))
                        .check(PeakMenuChecks.action("Double-click the golden apples", "slot-double", event -> "items:special".equals(event.elementId()))),

                PeakMenuTestStep.of("A disabled slot ignores clicks")
                        .setup(ItemsMenuTest::open)
                        .forbid(event -> event instanceof PeakMenuActionEvent action
                                && action.elementId().equals("items:disabled"))
                        .check(PeakMenuChecks.navigated("The menu opens", MENU))
                        .check(PeakMenuChecks.action("Click the barrier (nothing happens), then the TNT", "slot-click",
                                event -> "items:plain".equals(event.elementId())))
        );
    }
}
