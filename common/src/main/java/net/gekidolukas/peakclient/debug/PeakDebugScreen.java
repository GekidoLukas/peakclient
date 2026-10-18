package net.gekidolukas.peakclient.debug;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PeakDebugScreen extends Screen {
    public PeakDebugScreen() {
        super(Component.literal("PeakClient Debug"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        PeakDebugOverlayRenderer.setMousePosition(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            PeakLayoutEntry selected = PeakDebugState.resolveSelected((int) mouseX, (int) mouseY);
            if (selected != null) {
                copyToClipboard(selected.elementId());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        PeakDebugState.scroll((int) mouseX, (int) mouseY, scrollY);
        return true;
    }

    private void copyToClipboard(String text) {
        minecraft.keyboardHandler.setClipboard(text);
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal("§7[PeakClient Debug] Copied element ID: §f" + text), false);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        PeakDebugState.setEnabled(false);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
