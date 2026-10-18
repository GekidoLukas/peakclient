package net.gekidolukas.peakclient.fabric.mixin;

import net.gekidolukas.peakclient.render.PeakVanillaHudRender;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.gekidolukas.peakclient.ui.client.PeakClientVanillaHudState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiVanillaHudMixin {

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void peakclient$hotbarBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.HOTBAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderItemHotbar", at = @At("RETURN"))
    private void peakclient$hotbarEnd(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.HOTBAR, graphics);
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void peakclient$experienceBarBegin(GuiGraphics graphics, int x, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.EXPERIENCE_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderExperienceBar", at = @At("RETURN"))
    private void peakclient$experienceBarEnd(GuiGraphics graphics, int x, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.EXPERIENCE_BAR, graphics);
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void peakclient$experienceLevelBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.EXPERIENCE_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
    private void peakclient$experienceLevelEnd(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.EXPERIENCE_BAR, graphics);
    }

    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    private void peakclient$jumpBegin(PlayerRideableJumping vehicle, GuiGraphics graphics, int x, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.JUMP_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderJumpMeter", at = @At("RETURN"))
    private void peakclient$jumpEnd(PlayerRideableJumping vehicle, GuiGraphics graphics, int x, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.JUMP_BAR, graphics);
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void peakclient$armorBegin(GuiGraphics graphics, Player player, int a, int b, int c, int d, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.ARMOR_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("RETURN"))
    private static void peakclient$armorEnd(GuiGraphics graphics, Player player, int a, int b, int c, int d, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.ARMOR_BAR, graphics);
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    private void peakclient$heartsBegin(GuiGraphics graphics, Player player, int a, int b, int c, int d, float e,
                                        int f, int g, int h, boolean blink, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.HEALTH_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderHearts", at = @At("RETURN"))
    private void peakclient$heartsEnd(GuiGraphics graphics, Player player, int a, int b, int c, int d, float e,
                                      int f, int g, int h, boolean blink, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.HEALTH_BAR, graphics);
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void peakclient$foodBegin(GuiGraphics graphics, Player player, int a, int b, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.HUNGER_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("RETURN"))
    private void peakclient$foodEnd(GuiGraphics graphics, Player player, int a, int b, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.HUNGER_BAR, graphics);
    }

    @Redirect(method = "renderPlayerHealth", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean peakclient$airFluid(Player player, TagKey<Fluid> fluid) {
        return PeakClientVanillaHudState.isEnabled(PeakVanillaElement.AIR_BAR) && player.isEyeInFluid(fluid);
    }

    @Redirect(method = "renderPlayerHealth", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAirSupply()I"))
    private int peakclient$airSupply(Player player) {
        return PeakClientVanillaHudState.isEnabled(PeakVanillaElement.AIR_BAR) ? player.getAirSupply() : player.getMaxAirSupply();
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void peakclient$vehicleBegin(GuiGraphics graphics, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.VEHICLE_HEALTH, graphics)) ci.cancel();
    }

    @Inject(method = "renderVehicleHealth", at = @At("RETURN"))
    private void peakclient$vehicleEnd(GuiGraphics graphics, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.VEHICLE_HEALTH, graphics);
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void peakclient$itemNameBegin(GuiGraphics graphics, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.HELD_ITEM_NAME, graphics)) ci.cancel();
    }

    @Inject(method = "renderSelectedItemName", at = @At("RETURN"))
    private void peakclient$itemNameEnd(GuiGraphics graphics, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.HELD_ITEM_NAME, graphics);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void peakclient$crosshairBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.CROSSHAIR, graphics)) ci.cancel();
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void peakclient$crosshairEnd(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.CROSSHAIR, graphics);
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void peakclient$effectsBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.STATUS_EFFECTS, graphics)) ci.cancel();
    }

    @Inject(method = "renderEffects", at = @At("RETURN"))
    private void peakclient$effectsEnd(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.STATUS_EFFECTS, graphics);
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void peakclient$scoreboardBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.SCOREBOARD, graphics)) ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("RETURN"))
    private void peakclient$scoreboardEnd(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.SCOREBOARD, graphics);
    }

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void peakclient$actionBarBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.ACTION_BAR, graphics)) ci.cancel();
    }

    @Inject(method = "renderOverlayMessage", at = @At("RETURN"))
    private void peakclient$actionBarEnd(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.ACTION_BAR, graphics);
    }

    @Inject(method = "renderTitle", at = @At("HEAD"), cancellable = true)
    private void peakclient$titleBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.TITLE, graphics)) ci.cancel();
    }

    @Inject(method = "renderTitle", at = @At("RETURN"))
    private void peakclient$titleEnd(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PeakVanillaHudRender.end(PeakVanillaElement.TITLE, graphics);
    }

    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
    private void peakclient$chatBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.CHAT, graphics)) ci.cancel();
    }

    @Inject(method = "renderTabList", at = @At("HEAD"), cancellable = true)
    private void peakclient$tabListBegin(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!PeakVanillaHudRender.begin(PeakVanillaElement.TAB_LIST, graphics)) ci.cancel();
    }
}
