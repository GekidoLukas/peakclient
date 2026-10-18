package net.gekidolukas.peakclient.ui.client;

import net.minecraft.client.Minecraft;

import static com.mojang.blaze3d.platform.InputConstants.KEY_A;
import static com.mojang.blaze3d.platform.InputConstants.KEY_BACKSPACE;
import static com.mojang.blaze3d.platform.InputConstants.KEY_C;
import static com.mojang.blaze3d.platform.InputConstants.KEY_DELETE;
import static com.mojang.blaze3d.platform.InputConstants.KEY_END;
import static com.mojang.blaze3d.platform.InputConstants.KEY_HOME;
import static com.mojang.blaze3d.platform.InputConstants.KEY_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_V;
import static com.mojang.blaze3d.platform.InputConstants.KEY_X;

final class PeakTextEditorKeys {
    private PeakTextEditorKeys() {}

    static boolean apply(PeakTextEditor editor, int keyCode, boolean ctrl, boolean shift, Minecraft minecraft) {
        switch (keyCode) {
            case KEY_BACKSPACE -> {
                return editor.backspace(ctrl);
            }
            case KEY_DELETE -> {
                return editor.delete(ctrl);
            }
            case KEY_LEFT -> editor.moveLeft(ctrl, shift);
            case KEY_RIGHT -> editor.moveRight(ctrl, shift);
            case KEY_HOME -> editor.home(shift);
            case KEY_END -> editor.end(shift);
            default -> {
                if (ctrl && keyCode == KEY_A) {
                    editor.selectAll();
                } else if (ctrl && keyCode == KEY_C) {
                    if (editor.hasSelection()) minecraft.keyboardHandler.setClipboard(editor.selectedText());
                } else if (ctrl && keyCode == KEY_X) {
                    if (editor.hasSelection()) {
                        minecraft.keyboardHandler.setClipboard(editor.selectedText());
                        return editor.deleteSelection();
                    }
                } else if (ctrl && keyCode == KEY_V) {
                    return editor.insert(minecraft.keyboardHandler.getClipboard());
                }
            }
        }
        return false;
    }
}
