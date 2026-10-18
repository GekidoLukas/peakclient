package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakColorPickerLayout;
import net.minecraft.client.gui.screens.Screen;

import static com.mojang.blaze3d.platform.InputConstants.KEY_DOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_END;
import static com.mojang.blaze3d.platform.InputConstants.KEY_HOME;
import static com.mojang.blaze3d.platform.InputConstants.KEY_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEDOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEUP;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_UP;

final class PeakColorPickerController implements PeakInputController<PeakColorPicker> {
    private final PeakMenuContext context;

    PeakColorPickerController(PeakMenuContext context) {
        this.context = context;
    }

    @Override
    public void pressed(PeakColorPicker picker, PeakLayoutEntry hit, double mouseX, double mouseY) {
        PeakMenuInputState input = context.input();
        String target = picker.getTarget();
        if (target.isEmpty() || !input.isFocused(target)) input.focus(picker.getId());
        PeakColorPickerLayout layout = PeakColorPickerLayout.of(hit.localWidth(), hit.localHeight(), picker.isAlpha());
        PeakColorPickerLayout.Part part = layout.partAt(Math.round(PeakMenuContext.localX(hit, mouseX)),
                Math.round(PeakMenuContext.localY(hit, mouseY)));
        if (part != PeakColorPickerLayout.Part.NONE) {
            context.beginDrag(new PickerDrag(picker, part));
            context.pressed(picker);
            applyPosition(picker, hit, part, mouseX, mouseY);
        }
    }

    @Override
    public boolean key(PeakColorPicker picker, int keyCode) {
        boolean shift = Screen.hasShiftDown();
        PeakColorPickerState state = context.input().picker(picker);
        float step = shift ? 0.1f : 0.01f;
        float hueStep = shift ? 30f : 5f;
        if (keyCode == KEY_LEFT) {
            state.setSaturationBrightness(state.saturation() - step, state.brightness());
        } else if (keyCode == KEY_RIGHT) {
            state.setSaturationBrightness(state.saturation() + step, state.brightness());
        } else if (keyCode == KEY_UP) {
            state.setSaturationBrightness(state.saturation(), state.brightness() + step);
        } else if (keyCode == KEY_DOWN) {
            state.setSaturationBrightness(state.saturation(), state.brightness() - step);
        } else if (keyCode == KEY_PAGEUP) {
            state.setHue(PeakPanoramas.wrap(state.hue() - hueStep));
        } else if (keyCode == KEY_PAGEDOWN) {
            state.setHue(PeakPanoramas.wrap(state.hue() + hueStep));
        } else if (picker.isAlpha() && keyCode == KEY_HOME) {
            state.setOpacity(0f);
        } else if (picker.isAlpha() && keyCode == KEY_END) {
            state.setOpacity(1f);
        } else {
            return false;
        }
        PeakColorPickerLinks.commitState(context.menuId(), picker, state, true);
        PeakColorPickerLinks.fireChange(context.menuId(), picker);
        return true;
    }

    private void applyPosition(PeakColorPicker picker, PeakLayoutEntry entry, PeakColorPickerLayout.Part part,
                               double mouseX, double mouseY) {
        PeakColorPickerLayout layout = PeakColorPickerLayout.of(entry.localWidth(), entry.localHeight(), picker.isAlpha());
        int x = Math.round(PeakMenuContext.localX(entry, mouseX));
        int y = Math.round(PeakMenuContext.localY(entry, mouseY));
        PeakColorPickerState state = context.input().picker(picker);
        switch (part) {
            case FIELD -> state.setSaturationBrightness(layout.saturationAt(x), layout.valueAt(y));
            case HUE -> state.setHue(layout.hueAt(y));
            case ALPHA -> state.setOpacity(layout.alphaAt(y));
            default -> {
                return;
            }
        }
        PeakColorPickerLinks.commitState(context.menuId(), picker, state, true);
    }

    private final class PickerDrag implements PeakMenuDrag {
        private final PeakColorPicker picker;
        private final PeakColorPickerLayout.Part part;

        PickerDrag(PeakColorPicker picker, PeakColorPickerLayout.Part part) {
            this.picker = picker;
            this.part = part;
        }

        @Override
        public void dragged(double mouseX, double mouseY) {
            PeakLayoutEntry entry = context.entryOf(picker);
            if (entry != null) applyPosition(picker, entry, part, mouseX, mouseY);
        }

        @Override
        public void released(double mouseX, double mouseY) {
            PeakColorPickerLinks.fireChange(context.menuId(), picker);
        }
    }
}
