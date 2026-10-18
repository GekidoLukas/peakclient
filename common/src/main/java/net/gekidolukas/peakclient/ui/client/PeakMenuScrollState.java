package net.gekidolukas.peakclient.ui.client;

import java.util.HashMap;
import java.util.Map;

public final class PeakMenuScrollState {
    public static final int WHEEL_STEP = 14;

    private final Map<String, Integer> offsets = new HashMap<>();
    private final Map<String, Integer> maxima = new HashMap<>();

    public int offset(String elementId) {
        return offsets.getOrDefault(elementId, 0);
    }

    public int max(String elementId) {
        return maxima.getOrDefault(elementId, 0);
    }

    public int report(String elementId, int max) {
        int clampedMax = Math.max(0, max);
        maxima.put(elementId, clampedMax);
        int offset = Math.max(0, Math.min(offset(elementId), clampedMax));
        if (offset == 0) offsets.remove(elementId);
        else offsets.put(elementId, offset);
        return offset;
    }

    public boolean scrollBy(String elementId, int delta) {
        int max = max(elementId);
        int before = offset(elementId);
        int after = Math.max(0, Math.min(before + delta, max));
        if (after == before) return false;
        if (after == 0) offsets.remove(elementId);
        else offsets.put(elementId, after);
        return true;
    }

    public void set(String elementId, int offset) {
        int clamped = Math.max(0, Math.min(offset, max(elementId)));
        if (clamped == 0) offsets.remove(elementId);
        else offsets.put(elementId, clamped);
    }

    public void clear() {
        offsets.clear();
        maxima.clear();
    }
}
