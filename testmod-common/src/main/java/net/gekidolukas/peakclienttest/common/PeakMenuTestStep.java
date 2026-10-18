package net.gekidolukas.peakclienttest.common;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class PeakMenuTestStep {
    public static final int DEFAULT_TIMEOUT_TICKS = 20 * 120;

    private final String label;
    private Consumer<ServerPlayer> setup = player -> { };
    private final List<PeakMenuCheck> checks = new ArrayList<>();
    private final List<Predicate<Object>> guards = new ArrayList<>();
    private int timeoutTicks = DEFAULT_TIMEOUT_TICKS;

    private PeakMenuTestStep(String label) {
        this.label = label;
    }

    public static PeakMenuTestStep of(String label) {
        return new PeakMenuTestStep(label);
    }

    public PeakMenuTestStep setup(Consumer<ServerPlayer> setup) {
        this.setup = setup;
        return this;
    }

    public PeakMenuTestStep check(PeakMenuCheck check) {
        checks.add(check);
        return this;
    }

    public PeakMenuTestStep forbid(Predicate<Object> guard) {
        guards.add(guard);
        return this;
    }

    public PeakMenuTestStep timeoutSeconds(int seconds) {
        this.timeoutTicks = Math.max(1, seconds) * 20;
        return this;
    }

    public String label() { return label; }
    public Consumer<ServerPlayer> setup() { return setup; }
    public List<PeakMenuCheck> checks() { return checks; }
    public List<Predicate<Object>> guards() { return guards; }
    public int timeoutTicks() { return timeoutTicks; }
}
