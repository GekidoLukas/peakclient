package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PeakSortedList extends PeakContainer<PeakSortedList> {
    private PeakSortOrder sortOrder = PeakSortOrder.DESCENDING;
    private int maxVisible = 0;
    private int moveTicks = 10;
    private PeakPopupAnimation enterAnimation = PeakPopupAnimation.FADE;
    private int enterDurationTicks = 8;
    private int slideDistance = 24;
    private PeakPopupAnimation exitAnimation = PeakPopupAnimation.NONE;
    private int exitDurationTicks = 10;

    private transient PeakMotionTracker<PeakElement<?>> motion;
    private transient List<Ghost> ghosts;

    public record Ghost(PeakElement<?> element, float position, double start) {}

    public PeakSortedList() { this(PeakId.random()); }
    public PeakSortedList(String id) { super(id); }
    public static PeakSortedList create() { return new PeakSortedList(); }
    public static PeakSortedList create(String id) { return new PeakSortedList(id); }

    public PeakSortOrder getSortOrder() { return sortOrder == null ? PeakSortOrder.DESCENDING : sortOrder; }
    public PeakSortedList sortOrder(PeakSortOrder order) { this.sortOrder = order; return this; }
    public PeakSortedList descending() { return sortOrder(PeakSortOrder.DESCENDING); }
    public PeakSortedList ascending() { return sortOrder(PeakSortOrder.ASCENDING); }
    public int getMaxVisible() { return Math.max(0, maxVisible); }
    public PeakSortedList maxVisible(int count) { this.maxVisible = Math.max(0, count); return this; }
    public int getMoveTicks() { return Math.max(0, moveTicks); }
    public PeakSortedList moveTicks(int ticks) { this.moveTicks = Math.max(0, ticks); return this; }
    public PeakPopupAnimation getEnterAnimation() { return enterAnimation == null ? PeakPopupAnimation.FADE : enterAnimation; }
    public PeakSortedList enterAnimation(PeakPopupAnimation animation) { this.enterAnimation = animation; return this; }
    public int getEnterDurationTicks() { return Math.max(0, enterDurationTicks); }
    public PeakSortedList enterDurationTicks(int ticks) { this.enterDurationTicks = Math.max(0, ticks); return this; }
    public int getSlideDistance() { return slideDistance; }
    public PeakSortedList slideDistance(int distance) { this.slideDistance = distance; return this; }

    public PeakPopupAnimation getExitAnimation() { return exitAnimation == null ? PeakPopupAnimation.NONE : exitAnimation; }
    public PeakSortedList exitAnimation(PeakPopupAnimation animation) { this.exitAnimation = animation; return this; }
    public int getExitDurationTicks() { return Math.max(0, exitDurationTicks); }
    public PeakSortedList exitDurationTicks(int ticks) { this.exitDurationTicks = Math.max(0, ticks); return this; }

    public List<PeakElement<?>> sorted(List<PeakElement<?>> items) {
        List<PeakElement<?>> ordered = new ArrayList<>(items);
        Comparator<PeakElement<?>> byKey = Comparator.comparingDouble(PeakElement::getSortKey);
        ordered.sort(getSortOrder() == PeakSortOrder.DESCENDING ? byKey.reversed() : byKey);
        return ordered;
    }

    public PeakMotionTracker<PeakElement<?>> motion() {
        if (motion == null) motion = new PeakMotionTracker<>();
        return motion;
    }

    public List<Ghost> ghosts() {
        if (ghosts == null) ghosts = new ArrayList<>();
        return ghosts;
    }

    public void resetAnchor() {
        if (motion != null) motion.reset();
        if (ghosts != null) ghosts.clear();
    }

    @Override
    public String elementType() { return "sorted_list"; }
}
