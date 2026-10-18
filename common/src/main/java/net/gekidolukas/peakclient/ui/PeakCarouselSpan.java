package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class PeakCarouselSpan {
    public record Placement<T>(T item, int start, int length) {}

    public record Span<T>(List<Placement<T>> placements, int total) {}

    private PeakCarouselSpan() {}

    public static <T> Span<T> layout(List<T> items, ToIntFunction<T> lengthOf, int gap) {
        List<Placement<T>> placements = new ArrayList<>(items.size());
        int cursor = 0;
        for (T item : items) {
            int length = Math.max(0, lengthOf.applyAsInt(item));
            placements.add(new Placement<>(item, cursor, length));
            cursor += length + Math.max(0, gap);
        }
        int total = items.isEmpty() ? 0 : cursor - Math.max(0, gap);
        return new Span<>(placements, total);
    }

    public static float overflow(int total, int viewport) {
        return Math.max(0, total - Math.max(0, viewport));
    }

    public static float pingPongOffset(double elapsedTicks, float overflow, float speedPixelsPerSecond, int pauseTicks) {
        if (overflow <= 0f || speedPixelsPerSecond <= 0f) return 0f;
        double travel = overflow / speedPixelsPerSecond * 20.0;
        double pause = Math.max(0, pauseTicks);
        double period = 2.0 * (travel + pause);
        double time = elapsedTicks % period;
        if (time < pause) return 0f;
        time -= pause;
        if (time < travel) return (float) (overflow * time / travel);
        time -= travel;
        if (time < pause) return overflow;
        time -= pause;
        return (float) (overflow * (1.0 - time / travel));
    }
}
