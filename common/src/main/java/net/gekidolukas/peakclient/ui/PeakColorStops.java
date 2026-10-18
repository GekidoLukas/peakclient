package net.gekidolukas.peakclient.ui;

import net.gekidolukas.peakclient.PeakClientTickCounter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntUnaryOperator;

public final class PeakColorStops {
    private PeakColorStops() {}

    static List<PeakColorStop> sorted(List<PeakColorStop> stops) {
        if (stops.isEmpty()) throw new IllegalArgumentException("A gradient needs at least one color stop");
        List<PeakColorStop> sorted = new ArrayList<>(stops);
        sorted.sort(Comparator.comparing(PeakColorStop::position));
        return List.copyOf(sorted);
    }

    static List<PeakColorStop> evenly(int... colors) {
        if (colors.length == 0) throw new IllegalArgumentException("A gradient needs at least one color");
        List<PeakColorStop> stops = new ArrayList<>(colors.length);
        for (int i = 0; i < colors.length; i++) {
            float position = colors.length == 1 ? 0f : (float) i / (colors.length - 1);
            stops.add(new PeakColorStop(position, PeakArgb.normalize(colors[i])));
        }
        return stops;
    }

    static List<PeakColorStop> map(List<PeakColorStop> stops, IntUnaryOperator operator) {
        List<PeakColorStop> mapped = new ArrayList<>(stops.size());
        for (PeakColorStop stop : stops) mapped.add(new PeakColorStop(stop.position(), operator.applyAsInt(stop.color())));
        return mapped;
    }

    static int sample(List<PeakColorStop> stops, int cycleTicks, float position) {
        if (stops.size() == 1) return stops.get(0).color();
        if (cycleTicks <= 0) return sampleClamped(stops, position);
        double phase = (PeakClientTickCounter.exact() % cycleTicks) / cycleTicks;
        return sampleLoop(stops, position, (float) phase);
    }

    public static int sampleLoop(List<PeakColorStop> stops, float position, float phase) {
        PeakColorStop first = stops.get(0);
        PeakColorStop last = stops.get(stops.size() - 1);
        if (stops.size() == 1) return first.color();
        float span = last.position() - first.position();
        float gap = first.color() == last.color() ? 0f : span / (stops.size() - 1);
        float period = span + gap;
        if (period <= 1e-6f) return first.color();
        float along = (position - first.position()) + phase * period;
        along -= (float) Math.floor(along / period) * period;
        if (along <= span || gap <= 0f) return sampleClamped(stops, first.position() + Math.min(along, span));
        return PeakArgb.lerp(last.color(), first.color(), (along - span) / gap);
    }

    private static int sampleClamped(List<PeakColorStop> stops, float position) {
        float clamped = Math.max(0f, Math.min(1f, position));
        PeakColorStop first = stops.get(0);
        PeakColorStop last = stops.get(stops.size() - 1);
        if (clamped <= first.position()) return first.color();
        if (clamped >= last.position()) return last.color();
        for (int i = 0; i < stops.size() - 1; i++) {
            PeakColorStop from = stops.get(i);
            PeakColorStop to = stops.get(i + 1);
            if (clamped >= from.position() && clamped <= to.position()) {
                float span = to.position() - from.position();
                float local = span <= 0f ? 0f : (clamped - from.position()) / span;
                return PeakArgb.lerp(from.color(), to.color(), local);
            }
        }
        return last.color();
    }
}
