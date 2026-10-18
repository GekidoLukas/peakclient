package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.api.PeakClient;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record PeakElementTestStep(String label, int durationTicks, BiConsumer<ServerPlayer, String> action) {
    public static PeakElementTestStep of(String label, int durationTicks, BiConsumer<ServerPlayer, String> action) {
        return new PeakElementTestStep(label, durationTicks, action);
    }

    public static PeakElementTestStep document(String label, int durationTicks, Consumer<PeakDocument> content) {
        return new PeakElementTestStep(label, durationTicks, (player, docId) -> {
            PeakDocument doc = PeakDocument.create(docId);
            content.accept(doc);
            PeakClient.to(player).sendUi(doc);
        });
    }
}
