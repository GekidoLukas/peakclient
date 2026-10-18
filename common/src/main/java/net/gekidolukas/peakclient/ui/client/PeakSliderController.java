package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakSliderRenderer;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakEvent;

import static com.mojang.blaze3d.platform.InputConstants.KEY_END;
import static com.mojang.blaze3d.platform.InputConstants.KEY_HOME;
import static com.mojang.blaze3d.platform.InputConstants.KEY_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT;

final class PeakSliderController implements PeakInputController<PeakSlider> {
    private final PeakMenuContext context;

    PeakSliderController(PeakMenuContext context) {
        this.context = context;
    }

    @Override
    public void pressed(PeakSlider slider, PeakLayoutEntry hit, double mouseX, double mouseY) {
        context.input().focus(slider.getId());
        context.beginDrag(new SliderDrag(slider));
        context.pressed(slider);
        applyPosition(slider, hit, mouseX);
    }

    @Override
    public boolean key(PeakSlider slider, int keyCode) {
        double target;
        switch (keyCode) {
            case KEY_LEFT -> target = slider.getValue() - slider.keyStep();
            case KEY_RIGHT -> target = slider.getValue() + slider.keyStep();
            case KEY_HOME -> target = slider.getMin();
            case KEY_END -> target = slider.getMax();
            default -> {
                return false;
            }
        }
        double before = slider.getValue();
        slider.value(slider.valueForFraction(slider.getMax() <= slider.getMin() ? 0
                : (target - slider.getMin()) / (slider.getMax() - slider.getMin())));
        if (slider.getValue() != before) context.fireChange(slider, PeakEvent.CHANGE, slider.inputValue());
        return true;
    }

    private static void applyPosition(PeakSlider slider, PeakLayoutEntry entry, double mouseX) {
        float span = Math.max(1, entry.localWidth() - PeakSliderRenderer.HANDLE_WIDTH);
        float fraction = (PeakMenuContext.localX(entry, mouseX) - PeakSliderRenderer.HANDLE_WIDTH / 2f) / span;
        slider.value(slider.valueForFraction(fraction));
    }

    private final class SliderDrag implements PeakMenuDrag {
        private final PeakSlider slider;

        SliderDrag(PeakSlider slider) {
            this.slider = slider;
        }

        @Override
        public void dragged(double mouseX, double mouseY) {
            PeakLayoutEntry entry = context.entryOf(slider);
            if (entry != null) applyPosition(slider, entry, mouseX);
        }

        @Override
        public void released(double mouseX, double mouseY) {
            context.fireChange(slider, PeakEvent.CHANGE, slider.inputValue());
        }
    }
}
