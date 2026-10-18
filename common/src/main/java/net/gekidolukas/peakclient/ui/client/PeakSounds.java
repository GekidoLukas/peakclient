package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakSoundSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PeakSounds {
    private static final Map<String, SoundEvent> EVENTS = new HashMap<>();
    private static final Set<String> WARNED = new HashSet<>();

    private PeakSounds() {}

    public static void play(PeakSoundSpec spec) {
        if (spec == null || spec.isNone() || spec.getVolume() <= 0f) return;
        SoundEvent event = resolve(spec.getSound());
        if (event == null) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, spec.getPitch(), spec.getVolume()));
    }

    private static SoundEvent resolve(String sound) {
        SoundEvent cached = EVENTS.get(sound);
        if (cached != null) return cached;
        ResourceLocation location = ResourceLocation.tryParse(sound);
        if (location == null) {
            if (WARNED.add(sound)) PeakLog.warn("Menu sound '{}' is not a valid resource location", sound);
            return null;
        }
        SoundEvent event = SoundEvent.createVariableRangeEvent(location);
        EVENTS.put(sound, event);
        return event;
    }
}
