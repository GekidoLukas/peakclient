package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakRadioGroupRenderer;
import net.gekidolukas.peakclient.ui.PeakOption;
import net.gekidolukas.peakclient.ui.PeakRadioGroup;
import net.gekidolukas.peakclient.ui.PeakRadioLayout;
import net.gekidolukas.peakclient.ui.PeakEvent;

import static com.mojang.blaze3d.platform.InputConstants.KEY_DOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_END;
import static com.mojang.blaze3d.platform.InputConstants.KEY_HOME;
import static com.mojang.blaze3d.platform.InputConstants.KEY_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RETURN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_SPACE;
import static com.mojang.blaze3d.platform.InputConstants.KEY_UP;

final class PeakRadioGroupController implements PeakInputController<PeakRadioGroup> {
    private final PeakMenuContext context;

    PeakRadioGroupController(PeakMenuContext context) {
        this.context = context;
    }

    @Override
    public void pressed(PeakRadioGroup radio, PeakLayoutEntry hit, double mouseX, double mouseY) {
        context.input().focus(radio.getId());
        PeakRadioLayout layout = PeakRadioGroupRenderer.layout(context.minecraft().font, radio);
        int index = layout.optionAt(Math.round(PeakMenuContext.localX(hit, mouseX)),
                Math.round(PeakMenuContext.localY(hit, mouseY)));
        if (index >= 0) select(radio, index);
    }

    @Override
    public boolean key(PeakRadioGroup radio, int keyCode) {
        int current = radio.selectedIndex();
        int target;
        if (keyCode == KEY_UP || keyCode == KEY_LEFT) {
            target = current < 0 ? radio.lastEnabledIndex() : radio.nextEnabledIndex(current, -1);
        } else if (keyCode == KEY_DOWN || keyCode == KEY_RIGHT) {
            target = current < 0 ? radio.firstEnabledIndex() : radio.nextEnabledIndex(current, 1);
        } else if (keyCode == KEY_HOME) {
            target = radio.firstEnabledIndex();
        } else if (keyCode == KEY_END) {
            target = radio.lastEnabledIndex();
        } else if (keyCode == KEY_SPACE || keyCode == KEY_RETURN || keyCode == KEY_NUMPADENTER) {
            target = current < 0 ? radio.firstEnabledIndex() : current;
        } else {
            return false;
        }
        if (target >= 0 && target != current) select(radio, target);
        return true;
    }

    private void select(PeakRadioGroup radio, int index) {
        if (index < 0 || index >= radio.getOptions().size()) return;
        PeakOption option = radio.getOptions().get(index);
        if (!option.isEnabled() || option.getValue().equals(radio.getSelected())) return;
        radio.selected(option.getValue());
        context.playClick(radio.getInteraction());
        context.fireChange(radio, PeakEvent.CHANGE, radio.inputValue());
    }
}
