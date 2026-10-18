package net.gekidolukas.peakclienttest.common.elements;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.*;
import net.gekidolukas.peakclienttest.common.PeakElementTest;
import net.gekidolukas.peakclienttest.common.PeakElementTestStep;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class ItemElementTest implements PeakElementTest {
    @Override public String name() { return "item"; }
    @Override public String description() { return "PeakItemElement: FIXED, EQUIPMENT_SLOT, INVENTORY_SLOT, TARGETED_BLOCK"; }

    private PeakItemElement withSize(PeakItemElement item) {
        return item.absolute(PeakPosition.of(PeakAnchor.CENTER, 0, 0))
                .size(PeakSize.of(PeakSize.PeakDimension.pixels(32), PeakSize.PeakDimension.pixels(32)));
    }

    @Override
    public List<PeakElementTestStep> steps() {
        return List.of(
                PeakElementTestStep.of("FIXED (diamond sword)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(withSize(PeakItemElement.create(PeakItemSource.fixed(new ItemStack(Items.DIAMOND_SWORD)))));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("EQUIPMENT_SLOT (mainhand, live)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Holding your mainhand item live").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, -24)));
                    doc.add(withSize(PeakItemElement.create(PeakItemSource.equipment(EquipmentSlot.MAINHAND))));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("INVENTORY_SLOT (slot 0, live)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Showing inventory slot 0 live").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, -24)));
                    doc.add(withSize(PeakItemElement.create(PeakItemSource.inventorySlot(0))));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.of("TARGETED_BLOCK (look at a block with an item form)", 60, (player, docId) -> {
                    PeakDocument doc = PeakDocument.create(docId);
                    doc.add(PeakLabel.create("Look at a block that has a BlockItem").color(0xAAAAAA)
                            .absolute(PeakPosition.of(PeakAnchor.CENTER, 0, -24)));
                    doc.add(withSize(PeakItemElement.create(PeakItemSource.targetedBlock())));
                    PeakClient.to(player).sendUi(doc);
                }),
                PeakElementTestStep.document("size: 16, 32 and 64 pixel boxes scale and center the item", 80, doc -> {
                    int[] sizes = { 16, 32, 64 };
                    for (int i = 0; i < 3; i++) {
                        doc.add(PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD)).size(sizes[i], sizes[i])
                                .background(0xFF303030).absolute(PeakAnchor.CENTER, (i - 1) * 90, 0));
                    }
                }),
                PeakElementTestStep.document("count and durability decorations: a stack of 12 apples and a damaged pickaxe", 80, doc -> {
                    ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
                    pickaxe.setDamageValue(100);
                    doc.add(PeakItemElement.fixed(new ItemStack(Items.APPLE, 12)).size(32, 32).absolute(PeakAnchor.CENTER, -40, 0));
                    doc.add(PeakItemElement.fixed(pickaxe).size(32, 32).absolute(PeakAnchor.CENTER, 40, 0));
                })
        );
    }
}