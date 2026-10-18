package net.gekidolukas.peakclient;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
public final class PeakLog {
    private static final Logger LOGGER = LogUtils.getLogger();
    private PeakLog() {}

    public static void warn(String message, Object... args) {
        LOGGER.warn("[PeakClient] " + message, args);
    }

    public static void error(String message) {
        LOGGER.error("[PeakClient] " + message);
    }

    public static void error(String message, Throwable t) {
        LOGGER.error("[PeakClient] " + message, t);
    }
}
