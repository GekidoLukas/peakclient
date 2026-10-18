package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInteraction;

import static com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RETURN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_SPACE;

final class PeakActivationController<E extends PeakElement<?>> implements PeakInputController<E> {
    private final PeakMenuContext context;
    private final PeakInputControllers controllers;
    private final boolean focusOnPress;

    PeakActivationController(PeakMenuContext context, PeakInputControllers controllers, boolean focusOnPress) {
        this.context = context;
        this.controllers = controllers;
        this.focusOnPress = focusOnPress;
    }

    @Override
    public void pressed(E element, PeakLayoutEntry hit, double mouseX, double mouseY) {
        PeakMenuInputState input = context.input();
        PeakInteraction interaction = element.getInteraction();
        if (focusOnPress) input.focus(element.getId());
        else if (interaction == null || !interaction.isKeepFocus()) input.blur();
        context.doubleClickPressed(context.registerClick(element, mouseX, mouseY) >= 2);
        context.pressed(element);
    }

    @Override
    public boolean key(E element, int keyCode) {
        if (keyCode != KEY_SPACE && keyCode != KEY_RETURN && keyCode != KEY_NUMPADENTER) return false;
        context.playClick(element.getInteraction());
        controllers.activate(element);
        return true;
    }
}
