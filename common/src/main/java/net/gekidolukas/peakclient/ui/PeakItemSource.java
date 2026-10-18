package net.gekidolukas.peakclient.ui;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class PeakItemSource {
    public enum Kind { FIXED, EQUIPMENT_SLOT, INVENTORY_SLOT, TARGETED_BLOCK }

    private Kind kind;
    private ItemStack itemStack = ItemStack.EMPTY;
    private EquipmentSlot equipmentSlot;
    private int inventorySlot;

    private PeakItemSource() {}

    public static PeakItemSource fixed(ItemStack stack) {
        PeakItemSource source = new PeakItemSource();
        source.kind = Kind.FIXED;
        source.itemStack = stack;
        return source;
    }

    public static PeakItemSource equipment(EquipmentSlot slot) {
        PeakItemSource source = new PeakItemSource();
        source.kind = Kind.EQUIPMENT_SLOT;
        source.equipmentSlot = slot;
        return source;
    }

    public static PeakItemSource inventorySlot(int slotIndex) {
        PeakItemSource source = new PeakItemSource();
        source.kind = Kind.INVENTORY_SLOT;
        source.inventorySlot = slotIndex;
        return source;
    }

    public static PeakItemSource targetedBlock() {
        PeakItemSource source = new PeakItemSource();
        source.kind = Kind.TARGETED_BLOCK;
        return source;
    }

    public Kind kind() { return kind; }
    public ItemStack itemStack() { return itemStack; }
    public EquipmentSlot equipmentSlot() { return equipmentSlot; }
    public int inventorySlot() { return inventorySlot; }
}
