package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakAnimState;
import net.gekidolukas.peakclient.ui.PeakCarousel;
import net.gekidolukas.peakclient.ui.PeakCarouselDirection;
import net.gekidolukas.peakclient.ui.PeakCarouselMode;
import net.gekidolukas.peakclient.ui.PeakCycleOrder;
import net.gekidolukas.peakclient.ui.PeakCycler;
import net.gekidolukas.peakclient.ui.PeakCyclerTransition;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPopupAnimation;
import net.gekidolukas.peakclient.ui.PeakRevealState;
import net.gekidolukas.peakclient.ui.PeakTransitions;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakCarouselCyclerElementsTest {
    private static PeakDocument roundTrip(PeakDocument document) {
        return PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
    }

    @Test
    void transitionsSampleTheAnimations() {
        PeakAnimState fade = PeakTransitions.sample(PeakPopupAnimation.FADE, 0.25f, 40);
        assertEquals(0.25f, fade.alpha());
        assertEquals(1f, fade.scale());

        PeakAnimState scale = PeakTransitions.sample(PeakPopupAnimation.SCALE, 0.5f, 40);
        assertEquals(0.5f, scale.scale());

        PeakAnimState slide = PeakTransitions.sample(PeakPopupAnimation.SLIDE_FROM_LEFT, 0.5f, 40);
        assertEquals(-20f, slide.offsetX());

        assertEquals(PeakAnimState.VISIBLE, PeakTransitions.sample(PeakPopupAnimation.NONE, 0f, 40));
        assertEquals(1f, PeakTransitions.sample(PeakPopupAnimation.FADE, 7f, 40).alpha());
    }

    @Test
    void wipeTransitionsClipTheBoxAndKeepItOpaque() {
        PeakAnimState fromLeft = PeakTransitions.sample(PeakPopupAnimation.WIPE_FROM_LEFT, 0.25f, 40);
        assertEquals(0f, fromLeft.reveal().clipLeft());
        assertEquals(0.25f, fromLeft.reveal().clipRight());
        assertEquals(1f, fromLeft.alpha());
        assertEquals(1f, fromLeft.scale());
        assertTrue(fromLeft.reveal().isClipped());
        assertFalse(fromLeft.reveal().isScaled());

        PeakRevealState fromRight = PeakTransitions.sample(PeakPopupAnimation.WIPE_FROM_RIGHT, 0.25f, 40).reveal();
        assertEquals(0.75f, fromRight.clipLeft());
        assertEquals(1f, fromRight.clipRight());

        PeakRevealState fromTop = PeakTransitions.sample(PeakPopupAnimation.WIPE_FROM_TOP, 0.5f, 40).reveal();
        assertEquals(0f, fromTop.clipTop());
        assertEquals(0.5f, fromTop.clipBottom());

        PeakRevealState fromBottom = PeakTransitions.sample(PeakPopupAnimation.WIPE_FROM_BOTTOM, 0.5f, 40).reveal();
        assertEquals(0.5f, fromBottom.clipTop());
        assertEquals(1f, fromBottom.clipBottom());
    }

    @Test
    void wipesStartEmptyAndEndComplete() {
        for (PeakPopupAnimation animation : new PeakPopupAnimation[] {PeakPopupAnimation.WIPE_FROM_LEFT, PeakPopupAnimation.WIPE_FROM_RIGHT,
                PeakPopupAnimation.WIPE_FROM_TOP, PeakPopupAnimation.WIPE_FROM_BOTTOM, PeakPopupAnimation.FLIP_HORIZONTAL,
                PeakPopupAnimation.FLIP_VERTICAL}) {
            assertTrue(PeakTransitions.sample(animation, 0f, 40).reveal().isEmpty(), animation + " at 0");
            assertTrue(PeakTransitions.sample(animation, 1f, 40).reveal().isIdentity(), animation + " at 1");
            assertFalse(PeakTransitions.sample(animation, 0.5f, 40).reveal().isEmpty(), animation + " at 0.5");
        }
    }

    @Test
    void flipTransitionsSqueezeOneAxisOnly() {
        PeakRevealState horizontal = PeakTransitions.sample(PeakPopupAnimation.FLIP_HORIZONTAL, 0.25f, 40).reveal();
        assertEquals(0.25f, horizontal.scaleX());
        assertEquals(1f, horizontal.scaleY());
        assertTrue(horizontal.isScaled());
        assertFalse(horizontal.isClipped());

        PeakRevealState vertical = PeakTransitions.sample(PeakPopupAnimation.FLIP_VERTICAL, 0.25f, 40).reveal();
        assertEquals(1f, vertical.scaleX());
        assertEquals(0.25f, vertical.scaleY());
        assertEquals(1f, PeakTransitions.sample(PeakPopupAnimation.FLIP_HORIZONTAL, 0.25f, 40).alpha());
    }

    @Test
    void theRevealStateTravelsWithTheAnimationIntoTheEffectTransform() {
        PeakAnimState state = PeakTransitions.sample(PeakPopupAnimation.WIPE_FROM_LEFT, 0.5f, 40);
        PeakEffectTransform transform = PeakEffectTransform.of(state);

        assertEquals(state.reveal(), transform.reveal());
        assertFalse(transform.isIdentity());
        assertTrue(PeakEffectTransform.of(PeakTransitions.sample(PeakPopupAnimation.WIPE_FROM_LEFT, 1f, 40)).isIdentity());
        assertEquals(state.reveal(), PeakEffectTransform.IDENTITY.then(transform).reveal());
        assertTrue(transform.withOverlay(null).reveal().isClipped());
        assertTrue(PeakEffectTransform.IDENTITY.reveal().isIdentity());
    }

    @Test
    void oldAnimStateConstructorsStayIdentityRevealed() {
        assertTrue(new PeakAnimState(false, 1f, 2f, 1f, 1f).reveal().isIdentity());
        assertTrue(PeakAnimState.VISIBLE.reveal().isIdentity());
        assertTrue(PeakTransitions.sample(PeakPopupAnimation.SCALE, 0.5f, 40).reveal().isIdentity());
    }

    @Test
    void newAnimationsSurviveTheWireOfEveryUser() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakCycler.create("test:cycler").animations(PeakPopupAnimation.FLIP_HORIZONTAL, PeakPopupAnimation.WIPE_FROM_LEFT));
        PeakCycler back = assertInstanceOf(PeakCycler.class, roundTrip(document).findById("test:cycler"));
        assertEquals(PeakPopupAnimation.FLIP_HORIZONTAL, back.getEnterAnimation());
        assertEquals(PeakPopupAnimation.WIPE_FROM_LEFT, back.getExitAnimation());

        back.applyPatch(PeakPatch.cycler().enterAnimation(PeakPopupAnimation.WIPE_FROM_BOTTOM)
                .exitAnimation(PeakPopupAnimation.FLIP_VERTICAL).build());
        assertEquals(PeakPopupAnimation.WIPE_FROM_BOTTOM, back.getEnterAnimation());
        assertEquals(PeakPopupAnimation.FLIP_VERTICAL, back.getExitAnimation());
    }

    @Test
    void thePingPongSettingsHaveDefaultsAndSurviveTheWireAndPatches() {
        PeakCarousel plain = PeakCarousel.create("test:plain");
        assertEquals(PeakCarouselMode.LOOP, plain.getMode());
        assertFalse(plain.isOnlyWhenOverflowing());
        assertEquals(40, plain.getPauseTicks());

        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakCarousel.create("test:ping").pingPong().onlyWhenOverflowing(true).pauseTicks(15));
        PeakCarousel back = assertInstanceOf(PeakCarousel.class, roundTrip(document).findById("test:ping"));
        assertEquals(PeakCarouselMode.PINGPONG, back.getMode());
        assertTrue(back.isOnlyWhenOverflowing());
        assertEquals(15, back.getPauseTicks());

        back.applyPatch(PeakPatch.carousel().loop().onlyWhenOverflowing(false).pauseTicks(5).build());
        assertEquals(PeakCarouselMode.LOOP, back.getMode());
        assertFalse(back.isOnlyWhenOverflowing());
        assertEquals(5, back.getPauseTicks());
    }

    @Test
    void theCarouselSurvivesTheWireFormat() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakCarousel.create("test:carousel").movement(PeakCarouselDirection.DOWN).speed(12.5f)
                .order(PeakCycleOrder.RANDOM).fade(8, 16).gap(4).size(120, 40)
                .add(PeakLabel.create("test:a", "A")));

        PeakCarousel back = assertInstanceOf(PeakCarousel.class, roundTrip(document).findById("test:carousel"));

        assertEquals(PeakCarouselDirection.DOWN, back.getMovement());
        assertEquals(12.5f, back.getSpeed());
        assertEquals(PeakCycleOrder.RANDOM, back.getOrder());
        assertEquals(8, back.getFadeStart());
        assertEquals(16, back.getFadeEnd());
        assertEquals(4, back.getGap());
        assertEquals(1, back.getChildren().size());
    }

    @Test
    void theCyclerSurvivesTheWireFormat() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakCycler.create("test:cycler").intervalSeconds(2.5).order(PeakCycleOrder.RANDOM)
                .transition(PeakCyclerTransition.OVERLAP).animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.SCALE)
                .durations(12, 7).slideDistance(25).paused(true)
                .add(PeakLabel.create("test:a", "A"), PeakLabel.create("test:b", "B")));

        PeakCycler back = assertInstanceOf(PeakCycler.class, roundTrip(document).findById("test:cycler"));

        assertEquals(50, back.getIntervalTicks());
        assertEquals(PeakCycleOrder.RANDOM, back.getOrder());
        assertEquals(PeakCyclerTransition.OVERLAP, back.getTransition());
        assertEquals(PeakPopupAnimation.SLIDE_FROM_RIGHT, back.getEnterAnimation());
        assertEquals(PeakPopupAnimation.SCALE, back.getExitAnimation());
        assertEquals(12, back.getEnterDurationTicks());
        assertEquals(7, back.getExitDurationTicks());
        assertEquals(25, back.getSlideDistance());
        assertTrue(back.isPaused());
        assertEquals(2, back.getChildren().size());
    }

    @Test
    void defaultsAreUsedWhenNothingIsSet() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakCarousel.create("test:carousel"), PeakCycler.create("test:cycler"));

        PeakDocument back = roundTrip(document);
        PeakCarousel carousel = (PeakCarousel) back.findById("test:carousel");
        PeakCycler cycler = (PeakCycler) back.findById("test:cycler");

        assertEquals(PeakCarouselDirection.LEFT, carousel.getMovement());
        assertEquals(30f, carousel.getSpeed());
        assertEquals(0, carousel.getFadeStart());
        assertEquals(100, cycler.getIntervalTicks());
        assertEquals(PeakCyclerTransition.SEQUENTIAL, cycler.getTransition());
        assertFalse(cycler.isPaused());
    }

    @Test
    void patchesReachTheFieldsAndTheCustomKeys() {
        PeakCarousel carousel = PeakCarousel.create("test:carousel");
        carousel.applyPatch(PeakPatch.carousel().movement(PeakCarouselDirection.UP).speed(60f).fade(10).build());
        assertEquals(PeakCarouselDirection.UP, carousel.getMovement());
        assertEquals(60f, carousel.getSpeed());
        assertEquals(10, carousel.getFadeStart());
        assertEquals(10, carousel.getFadeEnd());

        PeakCycler cycler = PeakCycler.create("test:cycler");
        cycler.applyPatch(PeakPatch.cycler().paused(true).intervalSeconds(3).next().showIndex(2).build());
        assertTrue(cycler.isPaused());
        assertEquals(60, cycler.getIntervalTicks());
        assertTrue(cycler.takeNextRequest());
        assertFalse(cycler.takeNextRequest());
        assertEquals(2, cycler.takeIndexRequest());
        assertEquals(-1, cycler.takeIndexRequest());
    }

    @Test
    void timelineConfigTreatsNoneAnimationsAsInstant() {
        PeakCycler cycler = PeakCycler.create("test:cycler").intervalTicks(30)
                .enterAnimation(PeakPopupAnimation.NONE).exitAnimation(PeakPopupAnimation.FADE).durations(20, 8);

        var config = cycler.timelineConfig();

        assertEquals(30.0, config.intervalTicks());
        assertEquals(0.0, config.enterTicks());
        assertEquals(8.0, config.exitTicks());
    }
}
