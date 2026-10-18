package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.gekidolukas.peakclient.ui.PeakPlayerIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.PlayerSkin;

public final class PeakPlayerIconRenderer implements PeakElementRenderer<PeakPlayerIcon> {

    @Override
    public void renderContent(PeakRenderContext ctx, PeakPlayerIcon icon, int width, int height) {
        PlayerSkin skin = PeakPlayerSkinLookup.resolve(icon.getSource(), icon.getTargetUuid(), icon.getTexturePath(),
                icon.isTextureSlim(), Minecraft.getInstance());
        if (skin == null) return;
        PeakPlayerSkinRenderer.drawFitted(ctx.graphics(), skin, icon.getView(), 0, 0, width, height);
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakPlayerIcon icon) {
        return PeakPixelSize.ZERO;
    }
}
