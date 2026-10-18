package net.gekidolukas.peakclient.server;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public record PeakDocumentActionEvent(ServerPlayer player, String documentId, String elementId, String actionId,
                                      Map<String, String> args) {
    public String arg(String key) {
        return args.get(key);
    }

    public String arg(String key, String fallback) {
        return args.getOrDefault(key, fallback);
    }
}
