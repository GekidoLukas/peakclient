package net.gekidolukas.peakclienttest.common;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface PeakMenuTest {
    String name();
    String description();
    List<PeakMenuTestStep> steps();

    default void registerHandlers() {}

    default void cleanup(ServerPlayer player) {}
}
