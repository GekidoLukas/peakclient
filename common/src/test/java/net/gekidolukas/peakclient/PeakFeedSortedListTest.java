package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakFeed;
import net.gekidolukas.peakclient.ui.PeakFeedModel;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMotionTracker;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakSortOrder;
import net.gekidolukas.peakclient.ui.PeakSortedList;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakFeedSortedListTest {
    private record Item(String name) {}

    private static final PeakFeedModel.Config CONFIG = new PeakFeedModel.Config(100, 10, 10, 3);

    private static PeakDocument roundTrip(PeakDocument document) {
        return PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
    }

    @Test
    void feedEntriesEnterHoldExitAndExpire() {
        PeakFeedModel<Item> model = new PeakFeedModel<>();
        Item entry = new Item("a");
        List<Item> items = List.of(entry);

        var entering = model.update(0, items, CONFIG).get(0);
        assertEquals(PeakFeedModel.Phase.ENTER, entering.phase());
        assertEquals(0f, entering.progress());

        var halfway = model.update(5, items, CONFIG).get(0);
        assertEquals(0.5f, halfway.progress(), 0.001f);

        assertEquals(PeakFeedModel.Phase.HOLD, model.update(50, items, CONFIG).get(0).phase());

        model.update(110, items, CONFIG);
        var exiting = model.update(115, items, CONFIG).get(0);
        assertEquals(PeakFeedModel.Phase.EXIT, exiting.phase());
        assertEquals(0.5f, exiting.progress(), 0.001f);
        assertFalse(exiting.expired());

        assertTrue(model.update(120, items, CONFIG).get(0).expired());
    }

    @Test
    void theOldestEntriesLeaveWhenThereAreTooMany() {
        PeakFeedModel<Item> model = new PeakFeedModel<>();
        Item a = new Item("a"), b = new Item("b"), c = new Item("c"), d = new Item("d");
        model.update(0, List.of(a, b, c), CONFIG);

        var entries = model.update(20, List.of(a, b, c, d), CONFIG);

        assertEquals(PeakFeedModel.Phase.EXIT, entries.get(0).phase());
        assertEquals(PeakFeedModel.Phase.HOLD, entries.get(1).phase());
        assertEquals(PeakFeedModel.Phase.ENTER, entries.get(3).phase());
    }

    @Test
    void zeroLifetimeKeepsEntriesAndZeroExitExpiresImmediately() {
        PeakFeedModel<Item> model = new PeakFeedModel<>();
        Item a = new Item("a");
        PeakFeedModel.Config forever = new PeakFeedModel.Config(0, 0, 10, 0);
        model.update(0, List.of(a), forever);
        assertEquals(PeakFeedModel.Phase.HOLD, model.update(100000, List.of(a), forever).get(0).phase());

        PeakFeedModel<Item> instant = new PeakFeedModel<>();
        PeakFeedModel.Config noExit = new PeakFeedModel.Config(5, 0, 0, 0);
        instant.update(0, List.of(a), noExit);
        assertTrue(instant.update(5, List.of(a), noExit).get(0).expired());
    }

    @Test
    void motionGlidesToANewTargetAndStartsInPlace() {
        PeakMotionTracker<Item> tracker = new PeakMotionTracker<>();
        Item a = new Item("a");
        tracker.beginFrame(List.of(a));

        assertEquals(40f, tracker.track(a, 40f, true, 0, 10, 0).position());
        assertEquals(40f, tracker.track(a, 40f, true, 5, 10, 0).position());

        float start = tracker.track(a, 0f, true, 10, 10, 0).position();
        assertEquals(40f, start, 0.001f);
        float middle = tracker.track(a, 0f, true, 15, 10, 0).position();
        assertEquals(20f, middle, 0.001f);
        assertEquals(0f, tracker.track(a, 0f, true, 20, 10, 0).position(), 0.001f);
    }

    @Test
    void motionRetargetsFromTheCurrentPosition() {
        PeakMotionTracker<Item> tracker = new PeakMotionTracker<>();
        Item a = new Item("a");
        tracker.beginFrame(List.of(a));
        tracker.track(a, 0f, true, 0, 10, 0);
        tracker.track(a, 100f, true, 10, 10, 0);
        float halfway = tracker.track(a, 100f, true, 15, 10, 0).position();
        assertEquals(50f, halfway, 0.001f);

        float retarget = tracker.track(a, 0f, true, 15, 10, 0).position();
        assertEquals(50f, retarget, 0.001f);
        assertEquals(25f, tracker.track(a, 0f, true, 20, 10, 0).position(), 0.5f);
    }

    @Test
    void motionAppearsAfterTheInitialFrame() {
        PeakMotionTracker<Item> tracker = new PeakMotionTracker<>();
        Item first = new Item("first"), later = new Item("later");
        tracker.beginFrame(List.of(first));
        assertEquals(1f, tracker.track(first, 0f, true, 0, 10, 8).appear());

        tracker.beginFrame(List.of(first, later));
        assertEquals(0f, tracker.track(later, 20f, true, 5, 10, 8).appear());
        assertEquals(0.5f, tracker.track(later, 20f, true, 9, 10, 8).appear(), 0.001f);
        assertEquals(0f, tracker.track(later, 20f, false, 10, 10, 8).appear());
    }

    @Test
    void motionForgetsRemovedItems() {
        PeakMotionTracker<Item> tracker = new PeakMotionTracker<>();
        Item a = new Item("a"), b = new Item("b");
        tracker.beginFrame(List.of(a, b));
        tracker.track(a, 0f, true, 0, 10, 0);
        tracker.track(b, 10f, true, 0, 10, 0);
        assertEquals(2, tracker.trackedCount());

        tracker.beginFrame(List.of(a));
        assertEquals(1, tracker.trackedCount());
    }

    @Test
    void motionReportsDrawnItemsThatDisappear() {
        PeakMotionTracker<Item> tracker = new PeakMotionTracker<>();
        Item a = new Item("a"), b = new Item("b"), hiddenOne = new Item("c");
        tracker.beginFrame(List.of(a, b, hiddenOne));
        tracker.track(a, 0f, true, 0, 10, 0);
        tracker.track(b, 30f, true, 0, 10, 0);
        tracker.track(hiddenOne, 60f, false, 0, 10, 0);

        List<PeakMotionTracker.Departed<Item>> departed = tracker.beginFrame(List.of(a));

        assertEquals(1, departed.size());
        assertEquals(b, departed.get(0).item());
        assertEquals(30f, departed.get(0).position(), 0.001f);
        assertTrue(tracker.beginFrame(List.of(a)).isEmpty());
    }

    @Test
    void motionFlagsAnItemThatBecomesHiddenWhileDrawn() {
        PeakMotionTracker<Item> tracker = new PeakMotionTracker<>();
        Item a = new Item("a");
        tracker.beginFrame(List.of(a));
        assertFalse(tracker.track(a, 10f, true, 0, 10, 0).departed());
        assertFalse(tracker.track(a, 10f, true, 1, 10, 0).departed());

        PeakMotionTracker.Frame hidden = tracker.track(a, 10f, false, 2, 10, 0);

        assertTrue(hidden.departed());
        assertFalse(tracker.track(a, 10f, false, 3, 10, 0).departed());
    }

    @Test
    void theExitAnimationOfASortedListDefaultsToNoneAndSurvivesTheWireAndPatches() {
        PeakSortedList plain = PeakSortedList.create("test:plain");
        assertEquals(PeakPopupAnimation.NONE, plain.getExitAnimation());
        assertEquals(10, plain.getExitDurationTicks());

        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakSortedList.create("test:list").exitAnimation(PeakPopupAnimation.SCALE).exitDurationTicks(14));
        PeakSortedList back = assertInstanceOf(PeakSortedList.class, roundTrip(document).findById("test:list"));
        assertEquals(PeakPopupAnimation.SCALE, back.getExitAnimation());
        assertEquals(14, back.getExitDurationTicks());

        back.applyPatch(PeakPatch.sortedList().exitAnimation(PeakPopupAnimation.FADE).exitDurationTicks(6).build());
        assertEquals(PeakPopupAnimation.FADE, back.getExitAnimation());
        assertEquals(6, back.getExitDurationTicks());
    }

    @Test
    void sortKeysAreAFieldOfEveryElementAndSurviveTheWire() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakSortedList.create("test:list").sortOrder(PeakSortOrder.ASCENDING).maxVisible(5).moveTicks(14)
                .enterAnimation(PeakPopupAnimation.SLIDE_FROM_LEFT).enterDurationTicks(12).slideDistance(18)
                .add(PeakLabel.create("test:a", "A").sortKey(3.5)));
        document.add(PeakFeed.create("test:feed").entryLifetimeSeconds(4).maxEntries(3)
                .animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.SCALE).durations(6, 9)
                .slideDistance(50).moveTicks(4).newestFirst(true).autoRemove(false));

        PeakDocument back = roundTrip(document);
        PeakSortedList list = assertInstanceOf(PeakSortedList.class, back.findById("test:list"));
        PeakFeed feed = assertInstanceOf(PeakFeed.class, back.findById("test:feed"));

        assertEquals(PeakSortOrder.ASCENDING, list.getSortOrder());
        assertEquals(5, list.getMaxVisible());
        assertEquals(14, list.getMoveTicks());
        assertEquals(PeakPopupAnimation.SLIDE_FROM_LEFT, list.getEnterAnimation());
        assertEquals(12, list.getEnterDurationTicks());
        assertEquals(18, list.getSlideDistance());
        assertEquals(3.5, list.getChildren().get(0).getSortKey());

        assertEquals(80, feed.getEntryLifetimeTicks());
        assertEquals(3, feed.getMaxEntries());
        assertEquals(PeakPopupAnimation.SLIDE_FROM_RIGHT, feed.getEnterAnimation());
        assertEquals(PeakPopupAnimation.SCALE, feed.getExitAnimation());
        assertEquals(6, feed.getEnterDurationTicks());
        assertEquals(9, feed.getExitDurationTicks());
        assertEquals(50, feed.getSlideDistance());
        assertEquals(4, feed.getMoveTicks());
        assertTrue(feed.isNewestFirst());
        assertFalse(feed.isAutoRemove());
    }

    @Test
    void patchesReachSortKeyAndTheContainerFields() {
        PeakLabel label = PeakLabel.create("test:a", "A");
        label.applyPatch(PeakPatch.label().sortKey(12.25).build());
        assertEquals(12.25, label.getSortKey());

        PeakSortedList list = PeakSortedList.create("test:list");
        list.applyPatch(PeakPatch.sortedList().sortOrder(PeakSortOrder.ASCENDING).maxVisible(4).moveTicks(0).build());
        assertEquals(PeakSortOrder.ASCENDING, list.getSortOrder());
        assertEquals(4, list.getMaxVisible());
        assertEquals(0, list.getMoveTicks());

        PeakFeed feed = PeakFeed.create("test:feed");
        feed.applyPatch(PeakPatch.feed().maxEntries(9).newestFirst(true).entryLifetimeTicks(0).build());
        assertEquals(9, feed.getMaxEntries());
        assertTrue(feed.isNewestFirst());
        assertEquals(0, feed.getEntryLifetimeTicks());
    }

    @Test
    void sortKeysAreOrderedStablyAndDescendingByDefault() {
        PeakSortedList list = PeakSortedList.create("test:list");
        PeakLabel low = PeakLabel.create("test:low", "low").sortKey(1);
        PeakLabel tieFirst = PeakLabel.create("test:tie-first", "tie").sortKey(5);
        PeakLabel tieSecond = PeakLabel.create("test:tie-second", "tie").sortKey(5);
        PeakLabel high = PeakLabel.create("test:high", "high").sortKey(9);
        List<PeakElement<?>> items = List.of(low, tieFirst, tieSecond, high);

        List<PeakElement<?>> descending = list.sorted(items);
        assertEquals(List.of(high, tieFirst, tieSecond, low), descending);

        list.ascending();
        List<PeakElement<?>> ascending = list.sorted(items);
        assertEquals(List.of(low, tieFirst, tieSecond, high), ascending);
    }
}
