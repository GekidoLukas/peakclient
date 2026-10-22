package net.gekidolukas.peakclient.ui;

import net.minecraft.network.chat.Component;

public final class PeakCheckbox extends PeakElement<PeakCheckbox> implements PeakInput {
    private boolean checked = false;
    private Component text = Component.empty();
    @PeakOpaqueColor private PeakPaint color = PeakPaint.WHITE;
    private PeakCheckboxLook look = PeakCheckboxLook.BOX;

    public PeakCheckbox() { this(PeakId.random()); }

    public PeakCheckbox(String id) {
        super(id);
        interaction = PeakInteraction.create();
    }

    public static PeakCheckbox create() { return new PeakCheckbox(); }
    public static PeakCheckbox create(String id) { return new PeakCheckbox(id); }
    public static PeakCheckbox create(String id, String text) { return new PeakCheckbox(id).text(text); }
    public static PeakCheckbox create(String id, Component text) { return new PeakCheckbox(id).text(text); }

    public boolean isChecked() { return checked; }
    public PeakCheckbox checked(boolean checked) { this.checked = checked; return this; }

    public PeakCheckboxLook getLook() { return look == null ? PeakCheckboxLook.BOX : look; }
    public PeakCheckbox look(PeakCheckboxLook look) { this.look = look; return this; }
    public PeakCheckbox asSwitch() { return look(PeakCheckboxLook.SWITCH); }

    public Component getText() { return text == null ? Component.empty() : text; }
    public PeakCheckbox text(String text) { this.text = Component.literal(text == null ? "" : text); return this; }
    public PeakCheckbox text(Component text) { this.text = text == null ? Component.empty() : text; return this; }

    public PeakPaint getColor() { return color; }
    public PeakCheckbox color(PeakPaint paint) { this.color = paint.opaque(); return this; }
    public PeakCheckbox color(int argb) { return color(PeakPaint.solid(argb)); }

    @Override
    public String inputValue() { return Boolean.toString(checked); }

    @Override
    public String elementType() { return "checkbox"; }
}
