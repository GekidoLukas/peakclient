package net.gekidolukas.peakclient.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PeakAudiences {
    private static final long REFRESH_INTERVAL_TICKS = 20L;
    private static final List<PeakAudience> AUDIENCES = new CopyOnWriteArrayList<>();

    private PeakAudiences() {}

    static void register(PeakAudience audience) { AUDIENCES.add(audience); }

    static void unregister(PeakAudience audience) { AUDIENCES.remove(audience); }

    public static void refreshAll() {
        for (PeakAudience audience : AUDIENCES) audience.refresh();
    }

    public static void tick(long nowTick) {
        if (AUDIENCES.isEmpty()) return;
        if (nowTick % REFRESH_INTERVAL_TICKS == 0) refreshAll();
        for (PeakAudience audience : AUDIENCES) audience.flushViews();
    }

    public static void clear() {
        AUDIENCES.clear();
    }
}
