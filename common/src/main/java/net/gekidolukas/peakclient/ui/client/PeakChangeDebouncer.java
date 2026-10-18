package net.gekidolukas.peakclient.ui.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PeakChangeDebouncer {
    public record Fire(String elementId, String value) {}

    private static final class Pending {
        String value;
        long deadline;

        Pending(String value, long deadline) {
            this.value = value;
            this.deadline = deadline;
        }
    }

    private final Map<String, Pending> pending = new LinkedHashMap<>();

    public void change(String elementId, String value, int delayTicks, long nowTick) {
        Pending existing = pending.get(elementId);
        if (existing != null) {
            existing.value = value;
            return;
        }
        pending.put(elementId, new Pending(value, nowTick + Math.max(0, delayTicks)));
    }

    public List<Fire> due(long nowTick) {
        List<Fire> fires = new ArrayList<>();
        pending.entrySet().removeIf(entry -> {
            if (entry.getValue().deadline > nowTick) return false;
            fires.add(new Fire(entry.getKey(), entry.getValue().value));
            return true;
        });
        return fires;
    }

    public Fire flush(String elementId) {
        Pending removed = pending.remove(elementId);
        return removed == null ? null : new Fire(elementId, removed.value);
    }

    public List<Fire> flushAll() {
        List<Fire> fires = new ArrayList<>();
        pending.forEach((id, entry) -> fires.add(new Fire(id, entry.value)));
        pending.clear();
        return fires;
    }

    public boolean hasPending(String elementId) {
        return pending.containsKey(elementId);
    }

    public boolean isEmpty() {
        return pending.isEmpty();
    }
}
