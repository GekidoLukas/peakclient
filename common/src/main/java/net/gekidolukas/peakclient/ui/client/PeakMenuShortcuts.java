package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakShortcut;
import net.minecraft.client.gui.screens.Screen;

final class PeakMenuShortcuts {
    private int heldKey = -1;

    boolean handle(int keyCode) {
        PeakDocument menu = PeakClientMenus.currentMenu();
        if (menu == null || menu.getMenu() == null) return false;
        boolean ctrl = Screen.hasControlDown();
        boolean shift = Screen.hasShiftDown();
        boolean alt = Screen.hasAltDown();
        for (PeakShortcut shortcut : menu.getMenu().getShortcuts()) {
            if (!shortcut.matches(keyCode, ctrl, shift, alt)) continue;
            if (heldKey == keyCode) return true;
            heldKey = keyCode;
            if (!shortcut.getActions().isEmpty()) {
                PeakClientMenus.runActions(menu.getId(), "shortcut:" + shortcut.spec(), shortcut.getActions());
            }
            return true;
        }
        return false;
    }

    void released(int keyCode) {
        if (keyCode == heldKey) heldKey = -1;
    }
}
