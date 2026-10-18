package net.gekidolukas.peakclient.server;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record PeakMenuNavigationEvent(ServerPlayer player, String menuId, List<String> history, String reason) {
    public boolean isClosed() {
        return menuId == null;
    }
}
