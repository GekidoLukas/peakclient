package net.gekidolukas.peakclient.ui.client;

import net.minecraft.client.gui.GuiGraphics;

interface PeakMenuDrag {
    void dragged(double mouseX, double mouseY);

    void released(double mouseX, double mouseY);

    default void cancel() {}

    default String scrollbarOwner() { return null; }

    default boolean keepsScrollbarHover() { return false; }

    default boolean silencesHoverSound() { return false; }

    default boolean hidesTooltip() { return false; }

    default boolean cancelledByEscape() { return false; }

    default void beforeRender() {}

    default void afterRender(GuiGraphics graphics, int mouseX, int mouseY, int screenWidth, int screenHeight) {}
}
