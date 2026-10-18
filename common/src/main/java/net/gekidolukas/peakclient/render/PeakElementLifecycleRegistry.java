package net.gekidolukas.peakclient.render;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PeakElementLifecycleRegistry {
    private static final List<PeakElementLifecycleListener> LISTENERS = new CopyOnWriteArrayList<>();

    private PeakElementLifecycleRegistry() {}

    public static void register(PeakElementLifecycleListener listener) {
        LISTENERS.add(listener);
    }

    public static void notifyRemoved(String elementId) {
        for (PeakElementLifecycleListener listener : LISTENERS) {
            listener.onElementRemoved(elementId);
        }
    }
}
