package net.gekidolukas.peakclient.ui;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum PeakVanillaElement {
    HEALTH_BAR(true),
    ARMOR_BAR(true),
    HUNGER_BAR(true),
    AIR_BAR(false),
    VEHICLE_HEALTH(true),
    BOSS_BAR(true),
    HOTBAR(true),
    OFFHAND(false),
    EXPERIENCE_BAR(true),
    JUMP_BAR(true),
    HELD_ITEM_NAME(true),
    CROSSHAIR(true),
    STATUS_EFFECTS(true),
    SCOREBOARD(true),
    ACTION_BAR(true),
    TITLE(true),
    SOUND_SUBTITLES(true),
    CHAT(false),
    TAB_LIST(false),
    ADVANCEMENT_TOAST(false),
    RECIPE_TOAST(false),
    SYSTEM_TOAST(false),
    TUTORIAL_TOAST(false);

    public static final Set<PeakVanillaElement> STATUS_BARS = Collections.unmodifiableSet(
            EnumSet.of(HEALTH_BAR, ARMOR_BAR, HUNGER_BAR, AIR_BAR, VEHICLE_HEALTH));
    public static final Set<PeakVanillaElement> TOASTS = Collections.unmodifiableSet(
            EnumSet.of(ADVANCEMENT_TOAST, RECIPE_TOAST, SYSTEM_TOAST, TUTORIAL_TOAST));
    public static final Set<PeakVanillaElement> ALL = Collections.unmodifiableSet(EnumSet.allOf(PeakVanillaElement.class));

    private final boolean offsettable;

    PeakVanillaElement(boolean offsettable) {
        this.offsettable = offsettable;
    }

    public boolean offsettable() {
        return offsettable;
    }
}
