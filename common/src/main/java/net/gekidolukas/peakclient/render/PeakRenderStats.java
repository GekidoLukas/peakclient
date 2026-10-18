package net.gekidolukas.peakclient.render;

import java.util.Locale;

public final class PeakRenderStats {
    private record Breakdown(int fills, int blits, int texts, int shapes, int elements) {}

    private static final int WINDOW = 20;

    private static int fills;
    private static int blits;
    private static int texts;
    private static int shapes;
    private static int elements;
    private static long frameStart;
    private static long layoutNanos;
    private static long bindingNanos;
    private static int layoutDepth;
    private static long layoutStart;
    private static Breakdown lastBreakdown = new Breakdown(0, 0, 0, 0, 0);

    private static final int[] drawCallHistory = new int[WINDOW];
    private static final long[] nanosHistory = new long[WINDOW];
    private static final long[] layoutHistory = new long[WINDOW];
    private static final long[] bindingHistory = new long[WINDOW];
    private static int historyIndex;
    private static int historyCount;

    private PeakRenderStats() {}

    static void beginFrame() {
        fills = 0;
        blits = 0;
        texts = 0;
        shapes = 0;
        elements = 0;
        layoutNanos = 0;
        bindingNanos = 0;
        layoutDepth = 0;
        frameStart = System.nanoTime();
    }

    static void endFrame() {
        lastBreakdown = new Breakdown(fills, blits, texts, shapes, elements);
        drawCallHistory[historyIndex] = fills + blits + texts + shapes;
        nanosHistory[historyIndex] = System.nanoTime() - frameStart;
        layoutHistory[historyIndex] = layoutNanos;
        bindingHistory[historyIndex] = bindingNanos;
        historyIndex = (historyIndex + 1) % WINDOW;
        historyCount = Math.min(historyCount + 1, WINDOW);
    }

    static void countFill() { fills++; }

    static void countBlit() { blits++; }

    static void countText() { texts++; }

    static void countShape() { shapes++; }

    static void countElement() { elements++; }

    static void beginLayout() {
        if (layoutDepth++ == 0) layoutStart = System.nanoTime();
    }

    static void endLayout() {
        if (--layoutDepth == 0) layoutNanos += System.nanoTime() - layoutStart;
    }

    static long beginBindings() {
        return System.nanoTime();
    }

    static void endBindings(long start) {
        bindingNanos += System.nanoTime() - start;
    }

    public static String summary() {
        if (historyCount == 0) return "Draw calls: - | UI: - ms";
        long calls = 0;
        long nanos = 0;
        long layout = 0;
        long bindings = 0;
        for (int i = 0; i < historyCount; i++) {
            calls += drawCallHistory[i];
            nanos += nanosHistory[i];
            layout += layoutHistory[i];
            bindings += bindingHistory[i];
        }
        double toMs = 1.0 / historyCount / 1_000_000.0;
        double total = nanos * toMs;
        double layoutMs = layout * toMs;
        double bindingMs = bindings * toMs;
        return String.format(Locale.ROOT,
                "Draw calls: %d (fill %d, blit %d, text %d, shape %d) | Elements: %d | UI: %.2f ms (bindings %.2f, layout %.2f, rest %.2f)",
                calls / historyCount, lastBreakdown.fills(), lastBreakdown.blits(), lastBreakdown.texts(), lastBreakdown.shapes(), lastBreakdown.elements(),
                total, bindingMs, layoutMs, total - bindingMs - layoutMs);
    }
}
