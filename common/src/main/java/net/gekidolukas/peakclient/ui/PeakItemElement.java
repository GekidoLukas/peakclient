package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class PeakItemElement extends PeakElement<PeakItemElement> {
    private PeakItemSource source;

    public PeakItemElement(PeakItemSource source) { this(PeakId.random(), source); }

    public PeakItemElement(String id, PeakItemSource source) {
        super(id);
        this.source = source;
        size(PeakSize.of(PeakSize.PeakDimension.pixels(16), PeakSize.PeakDimension.pixels(16)));
    }

    public static PeakItemElement create(PeakItemSource source) { return new PeakItemElement(source); }
    public static PeakItemElement create(String id, PeakItemSource source) { return new PeakItemElement(id, source); }

    public static PeakItemElement fixed(ItemStack stack) { return new PeakItemElement(PeakItemSource.fixed(stack)); }
    public static PeakItemElement equipment(EquipmentSlot equipmentSlot) { return new PeakItemElement(PeakItemSource.equipment(equipmentSlot)); }
    public static PeakItemElement inventorySlot(int slotIndex) { return new PeakItemElement(PeakItemSource.inventorySlot(slotIndex)); }

    public PeakItemSource getSource() { return source; }
    public PeakItemElement source(PeakItemSource source) { this.source = source; return this; }

    @Override
    public String elementType() { return "item"; }
}
