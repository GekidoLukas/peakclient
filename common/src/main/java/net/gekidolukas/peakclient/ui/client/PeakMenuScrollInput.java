package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakContainer;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import static com.mojang.blaze3d.platform.InputConstants.KEY_DOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_END;
import static com.mojang.blaze3d.platform.InputConstants.KEY_HOME;
import static com.mojang.blaze3d.platform.InputConstants.KEY_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEDOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEUP;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_UP;

final class PeakMenuScrollInput {
    private final PeakMenuContext context;

    PeakMenuScrollInput(PeakMenuContext context) {
        this.context = context;
    }

    boolean key(int keyCode) {
        if (Screen.hasControlDown() || Screen.hasAltDown()) return false;
        PeakMenuScrollKeys.Command command;
        PeakMenuScrollKeys.Axis axis;
        if (keyCode == KEY_UP) {
            command = PeakMenuScrollKeys.Command.LINE_BACK;
            axis = PeakMenuScrollKeys.Axis.VERTICAL;
        } else if (keyCode == KEY_DOWN) {
            command = PeakMenuScrollKeys.Command.LINE_FORWARD;
            axis = PeakMenuScrollKeys.Axis.VERTICAL;
        } else if (keyCode == KEY_LEFT) {
            command = PeakMenuScrollKeys.Command.LINE_BACK;
            axis = PeakMenuScrollKeys.Axis.HORIZONTAL;
        } else if (keyCode == KEY_RIGHT) {
            command = PeakMenuScrollKeys.Command.LINE_FORWARD;
            axis = PeakMenuScrollKeys.Axis.HORIZONTAL;
        } else if (keyCode == KEY_PAGEUP) {
            command = PeakMenuScrollKeys.Command.PAGE_BACK;
            axis = PeakMenuScrollKeys.Axis.VERTICAL;
        } else if (keyCode == KEY_PAGEDOWN) {
            command = PeakMenuScrollKeys.Command.PAGE_FORWARD;
            axis = PeakMenuScrollKeys.Axis.VERTICAL;
        } else if (keyCode == KEY_HOME) {
            command = PeakMenuScrollKeys.Command.START;
            axis = PeakMenuScrollKeys.Axis.ANY;
        } else if (keyCode == KEY_END) {
            command = PeakMenuScrollKeys.Command.END;
            axis = PeakMenuScrollKeys.Axis.ANY;
        } else {
            return false;
        }

        PeakDocument menu = PeakClientMenus.currentMenu();
        if (menu == null) return false;
        PeakElement<?> focused = context.focusedElement();
        PeakLayoutEntry focusEntry = focused == null ? null : context.entryOf(focused);
        Minecraft minecraft = context.minecraft();
        var window = minecraft.getWindow();
        int mouseX = (int) (minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / Math.max(1, window.getScreenWidth()));
        int mouseY = (int) (minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / Math.max(1, window.getScreenHeight()));
        PeakMenuScrollState scroll = PeakClientMenus.scrollState(menu.getId());
        PeakLayoutProbe probe = context.probe();
        PeakMenuScrollKeys.Step step = PeakMenuScrollKeys.plan(probe, focusEntry, probe.findAt(mouseX, mouseY),
                scroll, command, axis);
        return step != null && scroll.scrollBy(step.containerId(), step.delta());
    }

    void wheel(PeakDocument menu, double mouseX, double mouseY, double scrollY) {
        PeakMenuScrollState scroll = PeakClientMenus.scrollState(menu.getId());
        int step = (int) Math.round(-scrollY * PeakMenuScrollState.WHEEL_STEP);
        if (step == 0) return;

        PeakLayoutProbe probe = context.probe();
        for (PeakLayoutEntry entry = probe.findAt((int) mouseX, (int) mouseY); entry != null; entry = probe.parentOf(entry)) {
            if (entry.element() instanceof PeakRichTextBox richBox) {
                if (scroll.scrollBy(richBox.getId(), step)) break;
                continue;
            }
            if (!(entry.element() instanceof PeakContainer<?> container) || container.getOverflow() != PeakOverflow.SCROLL) continue;
            if (scroll.scrollBy(container.getId(), step)) break;
        }
    }
}
