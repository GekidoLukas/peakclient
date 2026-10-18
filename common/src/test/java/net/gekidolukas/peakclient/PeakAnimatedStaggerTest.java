package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakAnimated;
import net.gekidolukas.peakclient.ui.PeakAnimatedClock;
import net.gekidolukas.peakclient.ui.PeakAnimationEffect;
import net.gekidolukas.peakclient.ui.PeakCycleOrder;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakEffects;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakStagger;
import net.gekidolukas.peakclient.ui.PeakStaggerTimeline;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakAnimatedStaggerTest {
    private static PeakDocument roundTrip(PeakDocument document) {
        return PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
    }

    @Test
    void loopEffectsStartAtTheirRestingPose() {
        assertTrue(PeakEffects.loop(PeakAnimationEffect.PULSE, 0, 40, 0f).isIdentity());
        assertTrue(PeakEffects.loop(PeakAnimationEffect.BOB, 0, 40, 0f).isIdentity());
        assertTrue(PeakEffects.loop(PeakAnimationEffect.SPIN, 0, 40, 0f).isIdentity());
        assertTrue(PeakEffects.loop(PeakAnimationEffect.BLINK, 0, 40, 0f).isIdentity());
        assertTrue(PeakEffects.loop(PeakAnimationEffect.NONE, 13, 40, 0f).isIdentity());
    }

    @Test
    void loopEffectsReachTheirAmplitude() {
        assertEquals(1.1f, PeakEffects.loop(PeakAnimationEffect.PULSE, 20, 40, 0f).scale(), 0.001f);
        assertEquals(1.5f, PeakEffects.loop(PeakAnimationEffect.PULSE, 20, 40, 0.5f).scale(), 0.001f);
        assertEquals(-3f, PeakEffects.loop(PeakAnimationEffect.BOB, 10, 40, 0f).offsetY(), 0.001f);
        assertEquals(3f, PeakEffects.loop(PeakAnimationEffect.SWAY, 10, 40, 0f).offsetX(), 0.001f);
        assertEquals(5f, PeakEffects.loop(PeakAnimationEffect.WOBBLE, 10, 40, 0f).rotationDegrees(), 0.001f);
        assertEquals(180f, PeakEffects.loop(PeakAnimationEffect.SPIN, 20, 40, 0f).rotationDegrees(), 0.001f);
        assertEquals(0.3f, PeakEffects.loop(PeakAnimationEffect.BLINK, 20, 40, 0f).alpha(), 0.001f);
    }

    @Test
    void loopEffectsRepeatEveryPeriod() {
        PeakEffectTransform first = PeakEffects.loop(PeakAnimationEffect.SWAY, 7, 40, 0f);
        PeakEffectTransform second = PeakEffects.loop(PeakAnimationEffect.SWAY, 47, 40, 0f);
        assertEquals(first.offsetX(), second.offsetX(), 0.001f);
    }

    @Test
    void oneShotEffectsStartAndEndAtRest() {
        for (PeakAnimationEffect effect : PeakAnimationEffect.values()) {
            boolean startsVisible = effect == PeakAnimationEffect.FLASH || effect == PeakAnimationEffect.SHINE
                    || effect == PeakAnimationEffect.OUTLINE;
            assertTrue(startsVisible || PeakEffects.oneShot(effect, 0f, 0f, 1f).isIdentity(), effect + " start");
            assertTrue(PeakEffects.oneShot(effect, 1f, 0f, 1f).isIdentity() || effect == PeakAnimationEffect.SPIN, effect + " end");
        }
        assertEquals(1.1f, PeakEffects.oneShot(PeakAnimationEffect.PULSE, 0.5f, 0f, 1f).scale(), 0.001f);
        assertEquals(1.2f, PeakEffects.oneShot(PeakAnimationEffect.PULSE, 0.5f, 0f, 2f).scale(), 0.001f);
    }

    @Test
    void theClockDelaysThePausesAndTriggers() {
        PeakAnimatedClock clock = new PeakAnimatedClock();
        PeakAnimatedClock.Config running = new PeakAnimatedClock.Config(PeakAnimationEffect.SWAY, 40, 0f, 10, true);
        PeakAnimatedClock.Config paused = new PeakAnimatedClock.Config(PeakAnimationEffect.SWAY, 40, 0f, 10, false);

        assertTrue(clock.transform(running, 100).isIdentity());
        assertTrue(clock.transform(running, 105).isIdentity());
        float moving = clock.transform(running, 120).offsetX();
        assertEquals(3f, moving, 0.001f);

        float frozen = clock.transform(paused, 125).offsetX();
        assertEquals(frozen, clock.transform(paused, 400).offsetX(), 0.001f);
        float resumed = clock.transform(running, 400).offsetX();
        assertEquals(frozen, resumed, 0.001f);

        PeakAnimatedClock idle = new PeakAnimatedClock();
        PeakAnimatedClock.Config none = new PeakAnimatedClock.Config(PeakAnimationEffect.NONE, 40, 0f, 0, true);
        idle.transform(none, 0);
        idle.trigger(PeakAnimationEffect.PULSE, 10, 1f);
        assertTrue(idle.transform(none, 50).isIdentity());
        assertEquals(1.1f, idle.transform(none, 55).scale(), 0.001f);
        assertTrue(idle.transform(none, 70).isIdentity());
    }

    @Test
    void staggerRevealsOneChildAfterTheOther() {
        PeakStaggerTimeline timeline = new PeakStaggerTimeline();
        PeakStaggerTimeline.Config config = new PeakStaggerTimeline.Config(4, 10, 10, 2);
        timeline.ensureStarted(100, true);

        assertEquals(0f, timeline.appearance(101, 0, 3, config));
        assertEquals(0.5f, timeline.appearance(107, 0, 3, config), 0.001f);
        assertEquals(1f, timeline.appearance(120, 0, 3, config));
        assertEquals(0f, timeline.appearance(106, 1, 3, config));
        assertEquals(0.5f, timeline.appearance(111, 1, 3, config), 0.001f);
        assertEquals(1f, timeline.appearance(140, 2, 3, config));
    }

    @Test
    void staggerHidesInOrderAndFinishes() {
        PeakStaggerTimeline timeline = new PeakStaggerTimeline();
        PeakStaggerTimeline.Config config = new PeakStaggerTimeline.Config(4, 10, 10, 0);
        timeline.ensureStarted(0, true);
        timeline.hide(100);

        assertEquals(PeakStaggerTimeline.Mode.OUT, timeline.getMode());
        assertEquals(1f, timeline.appearance(100, 0, 3, config));
        assertEquals(0.5f, timeline.appearance(105, 0, 3, config), 0.001f);
        assertEquals(0.9f, timeline.appearance(105, 1, 3, config), 0.001f);
        timeline.finish(100 + 8 + 10, 3, config);
        assertEquals(PeakStaggerTimeline.Mode.GONE, timeline.getMode());
        assertEquals(0f, timeline.appearance(200, 0, 3, config));

        timeline.play(300);
        assertEquals(PeakStaggerTimeline.Mode.IN, timeline.getMode());
    }

    @Test
    void staggerWithoutAutoPlayWaitsForReplay() {
        PeakStaggerTimeline timeline = new PeakStaggerTimeline();
        timeline.ensureStarted(0, false);
        assertEquals(PeakStaggerTimeline.Mode.GONE, timeline.getMode());
        assertEquals(0f, timeline.appearance(50, 0, 1, new PeakStaggerTimeline.Config(4, 10, 10, 0)));
    }

    @Test
    void staggerRanksFollowTheOrder() {
        PeakStagger plain = PeakStagger.create("test:plain");
        assertEquals("[0, 1, 2]", Arrays.toString(plain.ranksFor(3)));

        PeakStagger reversed = PeakStagger.create("test:reversed").reverse(true);
        assertEquals("[2, 1, 0]", Arrays.toString(reversed.ranksFor(3)));

        PeakStagger random = PeakStagger.create("test:random").order(PeakCycleOrder.RANDOM);
        Set<Integer> seen = new HashSet<>();
        for (int rank : random.ranksFor(6)) seen.add(rank);
        assertEquals(6, seen.size());
    }

    @Test
    void zeroDurationsAreInstant() {
        PeakStaggerTimeline timeline = new PeakStaggerTimeline();
        PeakStaggerTimeline.Config instant = new PeakStaggerTimeline.Config(5, 0, 0, 0);
        timeline.ensureStarted(0, true);

        assertEquals(1f, timeline.appearance(0, 0, 3, instant));
        assertEquals(0f, timeline.appearance(4, 1, 3, instant));
        assertEquals(1f, timeline.appearance(5, 1, 3, instant));
    }

    @Test
    void theElementsSurviveTheWireFormat() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakAnimated.create("test:animated").effect(PeakAnimationEffect.BOB).periodSeconds(1.5)
                .amplitude(4f).delayTicks(6).playing(false).add(PeakLabel.create("test:a", "A")));
        document.add(PeakStagger.create("test:stagger").animations(PeakPopupAnimation.SLIDE_FROM_LEFT, PeakPopupAnimation.SCALE)
                .delayTicks(3).durations(8, 6).startDelayTicks(5).slideDistance(30).order(PeakCycleOrder.RANDOM)
                .reverse(true).autoPlay(false).add(PeakLabel.create("test:b", "B")));

        PeakDocument back = roundTrip(document);
        PeakAnimated animated = assertInstanceOf(PeakAnimated.class, back.findById("test:animated"));
        PeakStagger stagger = assertInstanceOf(PeakStagger.class, back.findById("test:stagger"));

        assertEquals(PeakAnimationEffect.BOB, animated.getEffect());
        assertEquals(30, animated.getPeriodTicks());
        assertEquals(4f, animated.getAmplitude());
        assertEquals(6, animated.getDelayTicks());
        assertFalse(animated.isPlaying());

        assertEquals(PeakPopupAnimation.SLIDE_FROM_LEFT, stagger.getEnterAnimation());
        assertEquals(PeakPopupAnimation.SCALE, stagger.getExitAnimation());
        assertEquals(3, stagger.getDelayTicks());
        assertEquals(8, stagger.getEnterDurationTicks());
        assertEquals(6, stagger.getExitDurationTicks());
        assertEquals(5, stagger.getStartDelayTicks());
        assertEquals(30, stagger.getSlideDistance());
        assertEquals(PeakCycleOrder.RANDOM, stagger.getOrder());
        assertTrue(stagger.isReverse());
        assertFalse(stagger.isAutoPlay());
    }

    @Test
    void patchesReachTheFieldsAndTheTriggers() {
        PeakAnimated animated = PeakAnimated.create("test:animated");
        animated.applyPatch(PeakPatch.animated().effect(PeakAnimationEffect.WOBBLE).periodTicks(20).playing(false)
                .shake().build());
        assertEquals(PeakAnimationEffect.WOBBLE, animated.getEffect());
        assertEquals(20, animated.getPeriodTicks());
        assertFalse(animated.isPlaying());

        PeakAnimatedClock.Config none = new PeakAnimatedClock.Config(PeakAnimationEffect.NONE, 40, 0f, 0, true);
        animated.clock().transform(none, 0);
        PeakEffectTransform shaking = animated.clock().transform(none, 3);
        assertFalse(shaking.isIdentity());

        PeakStagger stagger = PeakStagger.create("test:stagger");
        stagger.applyPatch(PeakPatch.stagger().replay().delayTicks(9).build());
        assertEquals(9, stagger.getDelayTicks());
        assertTrue(stagger.takePlayRequest());
        assertFalse(stagger.takePlayRequest());
        stagger.applyPatch(PeakPatch.stagger().hide().build());
        assertTrue(stagger.takeHideRequest());
    }
}
