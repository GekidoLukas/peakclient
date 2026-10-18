package net.gekidolukas.peakclient.server;

import net.minecraft.server.level.ServerPlayer;

public record PeakClientReadyEvent(ServerPlayer player, int protocolVersion, int features, PeakReplaySummary replay) {}
