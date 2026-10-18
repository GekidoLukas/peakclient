package net.gekidolukas.peakclient;

import net.minecraft.server.MinecraftServer;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class PeakServerThread {
    private static final BooleanSupplier DEFAULT_CHECK = () -> {
        MinecraftServer server = PeakClientServer.server();
        return server == null || server.isSameThread();
    };
    private static final Consumer<Runnable> DEFAULT_EXECUTOR = action -> {
        MinecraftServer server = PeakClientServer.server();
        if (server == null) action.run();
        else server.execute(action);
    };

    private static volatile BooleanSupplier check = DEFAULT_CHECK;
    private static volatile Consumer<Runnable> executor = DEFAULT_EXECUTOR;

    private PeakServerThread() {}

    public static boolean isServerThread() {
        return check.getAsBoolean();
    }

    public static void run(Runnable action) {
        if (isServerThread()) {
            action.run();
            return;
        }
        executor.accept(() -> {
            try {
                action.run();
            } catch (Exception e) {
                PeakLog.error("A call from another thread failed on the server thread", e);
            }
        });
    }

    public static void install(BooleanSupplier threadCheck, Consumer<Runnable> threadExecutor) {
        check = threadCheck;
        executor = threadExecutor;
    }

    public static void reset() {
        check = DEFAULT_CHECK;
        executor = DEFAULT_EXECUTOR;
    }
}
