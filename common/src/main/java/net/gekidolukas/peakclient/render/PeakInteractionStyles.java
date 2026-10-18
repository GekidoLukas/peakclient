package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakStyle;
import net.gekidolukas.peakclient.ui.client.PeakMenuInteractionState;

public final class PeakInteractionStyles {
    private PeakInteractionStyles() {}

    public static PeakStyle resolve(PeakElement<?> element) {
        PeakStyle base = element.getStyle();
        if (!PeakMenuInteractionState.isActive()) return base;
        PeakInteraction interaction = element.getInteraction();
        if (interaction == null) return base;

        PeakStyle override = null;
        if (!interaction.isEnabled()) {
            override = interaction.getDisabledStyle();
        } else if (PeakMenuInteractionState.isPressed(element) && PeakMenuInteractionState.isHovered(element)) {
            override = interaction.getPressedStyle() != null ? interaction.getPressedStyle() : interaction.getHoverStyle();
        } else if (PeakMenuInteractionState.isHovered(element)) {
            override = interaction.getHoverStyle();
        }
        PeakStyle resolved = override == null ? base : merge(base, override);
        if (interaction.getActiveStyle() != null && isFormatActive(element)) {
            resolved = merge(resolved, interaction.getActiveStyle());
        }
        if (PeakMenuInteractionState.isFocused(element) && PeakMenuInteractionState.focusVisible(element)) {
            resolved = merge(resolved, PeakStyle.create()
                    .borderWidth(Math.max(PeakMenuInteractionState.focusWidth(element), resolved.getBorderWidth()))
                    .borderColor(PeakMenuInteractionState.focusColor(element)));
        }
        return resolved;
    }

    private static boolean isFormatActive(PeakElement<?> element) {
        for (PeakAction action : element.actionsFor(PeakEvent.CLICK)) {
            if (action.type() == PeakActionType.FORMAT) {
                return PeakMenuInteractionState.formatActive(action);
            }
        }
        return false;
    }

    static PeakStyle merge(PeakStyle base, PeakStyle override) {
        PeakStyle result = PeakStyle.create();
        result.background(override.getBackground().isNone() ? base.getBackground() : override.getBackground());
        result.padding(base.getPadding());
        result.margin(base.getMargin());
        result.borderWidth(override.getBorderWidth() > 0 ? override.getBorderWidth() : base.getBorderWidth());
        result.borderColor(override.getBorderColor().isNone() ? base.getBorderColor() : override.getBorderColor());
        result.borderRadius(override.getBorderRadius() > 0 ? override.getBorderRadius() : base.getBorderRadius());
        PeakStyle shadow = override.getShadowColor().isNone() ? base : override;
        result.boxShadow(shadow.getShadowColor(), shadow.getShadowOffsetX(), shadow.getShadowOffsetY(), shadow.getShadowBlur());
        return result;
    }
}
