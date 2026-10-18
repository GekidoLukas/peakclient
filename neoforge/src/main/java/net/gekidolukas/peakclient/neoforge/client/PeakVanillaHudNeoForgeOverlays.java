package net.gekidolukas.peakclient.neoforge.client;


import net.gekidolukas.peakclient.PeakClientMod;
import net.gekidolukas.peakclient.render.PeakVanillaHudRender;
import net.gekidolukas.peakclient.ui.PeakVanillaElement;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = PeakClientMod.MOD_ID, value = Dist.CLIENT)
public final class PeakVanillaHudNeoForgeOverlays {

    private static final Map<String, PeakVanillaElement> MAPPING = new HashMap<>();

    static {
        MAPPING.put("crosshair", PeakVanillaElement.CROSSHAIR);
        MAPPING.put("hotbar", PeakVanillaElement.HOTBAR);
        MAPPING.put("jump_meter", PeakVanillaElement.JUMP_BAR);
        MAPPING.put("experience_bar", PeakVanillaElement.EXPERIENCE_BAR);
        MAPPING.put("experience_level", PeakVanillaElement.EXPERIENCE_BAR);
        MAPPING.put("player_health", PeakVanillaElement.HEALTH_BAR);
        MAPPING.put("armor_level", PeakVanillaElement.ARMOR_BAR);
        MAPPING.put("food_level", PeakVanillaElement.HUNGER_BAR);
        MAPPING.put("vehicle_health", PeakVanillaElement.VEHICLE_HEALTH);
        MAPPING.put("air_level", PeakVanillaElement.AIR_BAR);
        MAPPING.put("selected_item_name", PeakVanillaElement.HELD_ITEM_NAME);
        MAPPING.put("effects", PeakVanillaElement.STATUS_EFFECTS);
        MAPPING.put("boss_overlay", PeakVanillaElement.BOSS_BAR);
        MAPPING.put("scoreboard_sidebar", PeakVanillaElement.SCOREBOARD);
        MAPPING.put("overlay_message", PeakVanillaElement.ACTION_BAR);
        MAPPING.put("title", PeakVanillaElement.TITLE);
        MAPPING.put("chat", PeakVanillaElement.CHAT);
        MAPPING.put("tab_list", PeakVanillaElement.TAB_LIST);
        MAPPING.put("subtitle_overlay", PeakVanillaElement.SOUND_SUBTITLES);
    }

    public static Set<PeakVanillaElement> supportedElements() {
        Set<PeakVanillaElement> supported = EnumSet.copyOf(MAPPING.values());
        supported.add(PeakVanillaElement.OFFHAND);
        supported.addAll(PeakVanillaElement.TOASTS);
        return supported;
    }

    private static PeakVanillaElement elementOf(RenderGuiLayerEvent event) {
        if (!"minecraft".equals(event.getName().getNamespace())) return null;
        return MAPPING.get(event.getName().getPath());
    }

    @SubscribeEvent
    static void onPre(RenderGuiLayerEvent.Pre event) {
        PeakVanillaElement element = elementOf(event);
        if (element == null) return;
        if (!PeakVanillaHudRender.begin(element, event.getGuiGraphics())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onPost(RenderGuiLayerEvent.Post event) {
        PeakVanillaElement element = elementOf(event);
        if (element == null) return;
        PeakVanillaHudRender.end(element, event.getGuiGraphics());
    }
}
