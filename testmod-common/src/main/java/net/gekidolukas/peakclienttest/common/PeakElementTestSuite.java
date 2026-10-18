package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclienttest.common.elements.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakElementTestSuite {
    private static final Map<String, PeakElementTest> TESTS = new LinkedHashMap<>();

    static {
        register(new GeneralElementTest());
        register(new PaintElementTest());
        register(new LabelElementTest());
        register(new TimerElementTest());
        register(new ImageElementTest());
        register(new SpacerElementTest());
        register(new ProgressBarElementTest());
        register(new RadialProgressElementTest());
        register(new IconBarElementTest());
        register(new TooltipLabelElementTest());
        register(new PopUpContainerElementTest());
        register(new CarouselElementTest());
        register(new CyclerElementTest());
        register(new ReelElementTest());
        register(new ShapeElementTest());
        register(new FlowElementTest());
        register(new EventsElementTest());
        register(new WorldAnchorElementTest());
        register(new AnimatedElementTest());
        register(new StaggerElementTest());
        register(new FeedElementTest());
        register(new SortedListElementTest());
        register(new LeaderboardElementTest());
        register(new ConditionalElementTest());
        register(new TableLayoutPanelElementTest());
        register(new BarChartElementTest());
        register(new PieChartElementTest());
        register(new CompassElementTest());
        register(new MinimapElementTest());
        register(new PlayerIconElementTest());
        register(new ChibiPlayerElementTest());
        register(new EntityElementTest());
        register(new ItemElementTest());
        register(new BlockElementTest());
        register(new VanillaHudElementTest());
    }

    private PeakElementTestSuite() {}

    private static void register(PeakElementTest test) {
        TESTS.put(test.name(), test);
    }

    public static boolean runSingle(String name, ServerPlayer player, int step) {
        PeakElementTest test = TESTS.get(name);
        if (test == null) return false;
        PeakElementTestRunner.startSingle(player, test, step);
        return true;
    }

    public static void runAll(ServerPlayer player) {
        PeakElementTestRunner.startAll(player, allTests());
    }

    public static Set<String> availableTests() { return TESTS.keySet(); }

    public static List<String> describeTests() {
        List<String> result = new ArrayList<>();
        for (PeakElementTest test : TESTS.values()) result.add(test.name() + " (" + test.steps().size() + ")");
        return result;
    }

    static List<PeakElementTest> allTests() { return new ArrayList<>(TESTS.values()); }
}
