package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakColorMath;
import net.gekidolukas.peakclient.ui.PeakColorPicker;
import net.gekidolukas.peakclient.ui.PeakColorPickerLayout;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakRect;
import net.gekidolukas.peakclient.ui.client.PeakColorPickerState;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class PeakColorPickerRenderer implements PeakElementRenderer<PeakColorPicker> {
    private static final int[] HUES = {
            0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000
    };
    private static final int CHECKER_LIGHT = 0xFFFFFFFF;
    private static final int CHECKER_DARK = 0xFFC0C0C0;
    private static final int CHECKER_SIZE = 4;
    private static final int BORDER = 0xFF808080;

    @Override
    public PeakPixelSize measureContent(Font font, PeakColorPicker picker) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakColorPicker picker, int width, int height) {
        GuiGraphics graphics = ctx.graphics();
        Font font = ctx.font();
        boolean enabled = picker.getInteraction() == null || picker.getInteraction().isEnabled();
        boolean focused = enabled && PeakMenuInteractionState.isFocused(picker);
        int w = Math.max(width, 1);
        int h = Math.max(height, 1);

        PeakColorPickerLayout layout = PeakColorPickerLayout.of(w, h, picker.isAlpha());
        PeakColorPickerState state = PeakMenuInteractionState.pickerState(picker);
        int rgb = PeakColorMath.hsvToRgb(state.hue(), state.saturation(), state.brightness());

        PeakRect field = layout.field();
        PeakImmediateRender.fill(graphics, field.x(), field.y(), field.right(), field.bottom(), BORDER);
        int pureHue = 0xFF000000 | PeakColorMath.hsvToRgb(state.hue(), 1f, 1f);
        PeakImmediateRender.fillQuadGradient(graphics, field.x() + 1, field.y() + 1, field.right() - 1, field.bottom() - 1,
                0xFFFFFFFF, pureHue, 0xFF000000, 0xFF000000, 16);
        int markerX = layout.fieldX(state.saturation());
        int markerY = layout.fieldY(state.brightness());
        ring(graphics, markerX - 3, markerY - 3, markerX + 4, markerY + 4, 1, PeakPaint.WHITE);
        ring(graphics, markerX - 2, markerY - 2, markerX + 3, markerY + 3, 1, PeakPaint.BLACK);

        PeakRect hue = layout.hue();
        PeakImmediateRender.fill(graphics, hue.x(), hue.y(), hue.right(), hue.bottom(), BORDER);
        PeakImmediateRender.fillGradientStrips(graphics, hue.x() + 1, hue.y() + 1, hue.right() - 1, hue.bottom() - 1,
                false, HUES);
        int hueMarker = layout.hueY(state.hue());
        PeakImmediateRender.fill(graphics, hue.x(), hueMarker - 1, hue.right(), hueMarker + 2, 0xFFFFFFFF);
        PeakImmediateRender.fill(graphics, hue.x(), hueMarker, hue.right(), hueMarker + 1, 0xFF000000);

        if (layout.hasAlpha()) {
            PeakRect alpha = layout.alpha();
            PeakImmediateRender.fill(graphics, alpha.x(), alpha.y(), alpha.right(), alpha.bottom(), BORDER);
            checker(graphics, alpha.x() + 1, alpha.y() + 1, alpha.right() - 1, alpha.bottom() - 1);
            PeakImmediateRender.fillQuadGradient(graphics, alpha.x() + 1, alpha.y() + 1, alpha.right() - 1, alpha.bottom() - 1,
                    0xFF000000 | rgb, 0xFF000000 | rgb, 0x01000000 | rgb, 0x01000000 | rgb, 8);
            int alphaMarker = layout.alphaY(state.opacity());
            PeakImmediateRender.fill(graphics, alpha.x(), alphaMarker - 1, alpha.right(), alphaMarker + 2, 0xFFFFFFFF);
            PeakImmediateRender.fill(graphics, alpha.x(), alphaMarker, alpha.right(), alphaMarker + 1, 0xFF000000);
        }

        PeakRect preview = layout.preview();
        PeakImmediateRender.fill(graphics, preview.x(), preview.y(), preview.right(), preview.bottom(), BORDER);
        checker(graphics, preview.x() + 1, preview.y() + 1, preview.right() - 1, preview.bottom() - 1);
        PeakImmediateRender.fill(graphics, preview.x() + 1, preview.y() + 1, preview.right() - 1, preview.bottom() - 1,
                picker.getValue());

        if (picker.isShowHex()) {
            String text = picker.inputValue();
            int textY = layout.footerY() + (PeakColorPickerLayout.FOOTER_HEIGHT - font.lineHeight) / 2 + 1;
            PeakImmediateRender.text(graphics, font, text, layout.hexX(), textY, enabled ? 0xFFFFFF : 0x909090, true);
        }

        if (focused && PeakMenuInteractionState.focusVisible(picker)) {
            ring(graphics, 0, 0, w, h, Math.max(1, PeakMenuInteractionState.focusWidth(picker)),
                    PeakMenuInteractionState.focusColor(picker));
        }
        if (!enabled) PeakImmediateRender.fill(graphics, 0, 0, w, h, 0x80202020);
    }

    private static void checker(GuiGraphics graphics, int x1, int y1, int x2, int y2) {
        if (x2 <= x1 || y2 <= y1) return;
        PeakImmediateRender.fill(graphics, x1, y1, x2, y2, CHECKER_LIGHT);
        for (int row = 0, y = y1; y < y2; row++, y += CHECKER_SIZE) {
            for (int column = 0, x = x1; x < x2; column++, x += CHECKER_SIZE) {
                if (((row + column) & 1) == 0) continue;
                PeakImmediateRender.fill(graphics, x, y, Math.min(x + CHECKER_SIZE, x2), Math.min(y + CHECKER_SIZE, y2),
                        CHECKER_DARK);
            }
        }
    }

    private static void ring(GuiGraphics graphics, int x1, int y1, int x2, int y2, int thickness, PeakPaint paint) {
        int lineWidth = Math.max(1, thickness);
        PeakPaintRenderer.fill(graphics, paint, x1, y1, x2, y1 + lineWidth, x1, y1, x2, y2);
        PeakPaintRenderer.fill(graphics, paint, x1, y2 - lineWidth, x2, y2, x1, y1, x2, y2);
        PeakPaintRenderer.fill(graphics, paint, x1, y1 + lineWidth, x1 + lineWidth, y2 - lineWidth, x1, y1, x2, y2);
        PeakPaintRenderer.fill(graphics, paint, x2 - lineWidth, y1 + lineWidth, x2, y2 - lineWidth, x1, y1, x2, y2);
    }
}
