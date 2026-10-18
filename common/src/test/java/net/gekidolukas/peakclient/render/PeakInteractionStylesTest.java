package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakInteractionStylesTest {
    @AfterEach
    void reset() {
        PeakMenuInteractionState.end();
    }

    @Test
    void outsideAMenuTheBaseStyleIsUsed() {
        PeakButton button = PeakButton.text("b", "x");

        assertSame(button.getStyle(), PeakInteractionStyles.resolve(button));
    }

    @Test
    void hoverPressedAndDisabledSelectTheirBackground() {
        PeakButton button = PeakButton.text("b", "x");

        PeakMenuInteractionState.begin(null, null);
        assertEquals(PeakPaint.solid(PeakButton.DEFAULT_BACKGROUND), PeakInteractionStyles.resolve(button).getBackground());

        PeakMenuInteractionState.begin(button, null);
        assertEquals(PeakPaint.solid(PeakButton.DEFAULT_HOVER_BACKGROUND), PeakInteractionStyles.resolve(button).getBackground());

        PeakMenuInteractionState.begin(button, button);
        assertEquals(PeakPaint.solid(PeakButton.DEFAULT_PRESSED_BACKGROUND), PeakInteractionStyles.resolve(button).getBackground());

        button.enabled(false);
        assertEquals(PeakPaint.solid(PeakButton.DEFAULT_DISABLED_BACKGROUND), PeakInteractionStyles.resolve(button).getBackground());
    }

    @Test
    void anOverrideKeepsTheRestOfTheBaseStyle() {
        PeakButton button = PeakButton.text("b", "x");
        button.borderRadius(4);
        PeakMenuInteractionState.begin(button, null);

        PeakStyle resolved = PeakInteractionStyles.resolve(button);

        assertEquals(4, resolved.getBorderRadius());
        assertEquals(1, resolved.getBorderWidth());
        assertEquals(button.getStyle().getPadding().getLeft(), resolved.getPadding().getLeft());
    }

    @Test
    void anOverrideBackgroundReplacesABaseGradient() {
        PeakStyle base = PeakStyle.create().background(PeakPaint.gradient(0xFFFF0000, 0xFF0000FF));
        PeakStyle override = PeakStyle.create().background(0xFF00FF00);

        PeakStyle merged = PeakInteractionStyles.merge(base, override);

        assertEquals(PeakPaint.solid(0xFF00FF00), merged.getBackground());
    }

    @Test
    void elementsWithoutInteractionNeverChange() {
        PeakPanel panel = PeakPanel.create("p");
        PeakMenuInteractionState.begin(panel, panel);

        assertSame(panel.getStyle(), PeakInteractionStyles.resolve(panel));
    }
}
