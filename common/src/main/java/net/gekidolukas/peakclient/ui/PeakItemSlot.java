package net.gekidolukas.peakclient.ui;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class PeakItemSlot extends PeakElement<PeakItemSlot> {
    private PeakItemSource source;
    private boolean showCount = true;
    private boolean showDurability = true;
    private String countText = "";
    private boolean selected = false;
    private boolean itemTooltip = true;

    public PeakItemSlot(PeakItemSource source) { this(PeakId.random(), source); }

    public PeakItemSlot(String id, PeakItemSource source) {
        super(id);
        this.source = source;
        size = PeakSize.pixels(18, 18);
        interaction = PeakInteraction.create();
    }

    public static PeakItemSlot create(PeakItemSource source) { return new PeakItemSlot(source); }
    public static PeakItemSlot create(String id, PeakItemSource source) { return new PeakItemSlot(id, source); }
    public static PeakItemSlot fixed(ItemStack stack) { return new PeakItemSlot(PeakItemSource.fixed(stack)); }
    public static PeakItemSlot fixed(String id, ItemStack stack) { return new PeakItemSlot(id, PeakItemSource.fixed(stack)); }
    public static PeakItemSlot equipment(String id, EquipmentSlot slot) { return new PeakItemSlot(id, PeakItemSource.equipment(slot)); }
    public static PeakItemSlot inventorySlot(String id, int slotIndex) { return new PeakItemSlot(id, PeakItemSource.inventorySlot(slotIndex)); }

    public PeakItemSource getSource() { return source; }
    public PeakItemSlot source(PeakItemSource source) { this.source = source; return this; }

    public boolean isShowCount() { return showCount; }
    public PeakItemSlot showCount(boolean showCount) { this.showCount = showCount; return this; }

    public boolean isShowDurability() { return showDurability; }
    public PeakItemSlot showDurability(boolean showDurability) { this.showDurability = showDurability; return this; }

    public String getCountText() { return countText == null ? "" : countText; }
    public PeakItemSlot countText(String text) { this.countText = text == null ? "" : text; return this; }

    public boolean isSelected() { return selected; }
    public PeakItemSlot selected(boolean selected) { this.selected = selected; return this; }

    public boolean isItemTooltip() { return itemTooltip; }
    public PeakItemSlot itemTooltip(boolean itemTooltip) { this.itemTooltip = itemTooltip; return this; }

    @Override
    public String elementType() { return "item_slot"; }
}
