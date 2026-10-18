package net.gekidolukas.peakclient.helper;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PeakServerTask {
    void tick();

    default UUID getTargetPlayerId() { return null; }

    default Collection<UUID> getTargetPlayerIds() {
        UUID id = getTargetPlayerId();
        return id == null ? List.of() : List.of(id);
    }

    default boolean removeTarget(UUID playerId) { return true; }

    void forceStopInternal();
}
