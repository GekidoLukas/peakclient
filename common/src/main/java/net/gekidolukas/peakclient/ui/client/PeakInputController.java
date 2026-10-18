package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakElement;

interface PeakInputController<E extends PeakElement<?>> {
    void pressed(E element, PeakLayoutEntry hit, double mouseX, double mouseY);

    default boolean isTyping() { return false; }

    default boolean key(E element, int keyCode) { return false; }

    default boolean typed(E element, char codePoint) { return false; }

    default boolean wheel(E element, double scrollY) { return false; }

    default boolean escape(E element) { return false; }

    default void focusedByTab(E element) {}
}
