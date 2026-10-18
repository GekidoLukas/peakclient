package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakAnimated;
import net.gekidolukas.peakclient.ui.PeakAnimatedClock;
import net.gekidolukas.peakclient.ui.PeakAnimationEffect;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentValidator;
import net.gekidolukas.peakclient.ui.PeakEffectOverlay;
import net.gekidolukas.peakclient.ui.PeakEffectTransform;
import net.gekidolukas.peakclient.ui.PeakEffects;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakEffectsTest {
    @Test
    void popScalesUpAndComesBack() {
        assertTrue(PeakEffects.oneShot(PeakAnimationEffect.POP, 0f, 0f, 1f).isIdentity());
        assertTrue(PeakEffects.oneShot(PeakAnimationEffect.POP, 1f, 0f, 1f).isIdentity());
        float peak = 1f;
        for (float p = 0f; p <= 1f; p += 0.01f) peak = Math.max(peak, PeakEffects.oneShot(PeakAnimationEffect.POP, p, 0f, 1f).scale());
        assertEquals(1.15f, peak, 0.01f);
        assertTrue(PeakEffects.oneShot(PeakAnimationEffect.POP, 0.2f, 0f, 1f).scale()
                > PeakEffects.oneShot(PeakAnimationEffect.POP, 0.8f, 0f, 1f).scale());
    }

    @Test
    void flashStartsStrongAndFadesOut() {
        PeakEffectTransform start = PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0f, 0f, 1f);
        PeakEffectTransform middle = PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0.5f, 0f, 1f);
        PeakEffectTransform end = PeakEffects.oneShot(PeakAnimationEffect.FLASH, 1f, 0f, 1f);

        assertEquals(PeakEffectOverlay.Kind.FLASH, start.overlay().kind());
        assertEquals(0.6f, start.overlay().strength(), 0.001f);
        assertTrue(middle.overlay().strength() < start.overlay().strength());
        assertTrue(middle.overlay().strength() > 0f);
        assertTrue(end.isIdentity());
    }

    @Test
    void glowAndOutlineRiseAndFall() {
        PeakEffectTransform glowStart = PeakEffects.oneShot(PeakAnimationEffect.GLOW, 0f, 0f, 1f);
        PeakEffectTransform glowMiddle = PeakEffects.oneShot(PeakAnimationEffect.GLOW, 0.5f, 0f, 1f);
        assertTrue(glowStart.isIdentity());
        assertEquals(0.8f, glowMiddle.overlay().strength(), 0.001f);
        assertTrue(PeakEffects.oneShot(PeakAnimationEffect.GLOW, 1f, 0f, 1f).isIdentity());

        assertEquals(1f, PeakEffects.oneShot(PeakAnimationEffect.OUTLINE, 0f, 0f, 1f).overlay().strength(), 0.001f);
        assertTrue(PeakEffects.oneShot(PeakAnimationEffect.OUTLINE, 1f, 0f, 1f).isIdentity());
    }

    @Test
    void shineTravelsOnceAndEndsInvisible() {
        PeakEffectOverlay first = PeakEffects.oneShot(PeakAnimationEffect.SHINE, 0.25f, 0f, 1f).overlay();
        PeakEffectOverlay second = PeakEffects.oneShot(PeakAnimationEffect.SHINE, 0.75f, 0f, 1f).overlay();

        assertEquals(PeakEffectOverlay.Kind.SHINE, first.kind());
        assertEquals(0.25f, first.progress(), 1e-6f);
        assertEquals(0.75f, second.progress(), 1e-6f);
        assertTrue(PeakEffects.oneShot(PeakAnimationEffect.SHINE, 1f, 0f, 1f).isIdentity());
    }

    @Test
    void intensityScalesTheOverlayAndTheColorIsResolved() {
        assertEquals(0.3f, PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0f, 0f, 0.5f).overlay().strength(), 0.001f);
        assertEquals(1f, PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0f, 0f, 5f).overlay().strength(), 0.001f);

        assertEquals(0xFFFFFFFF, PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0f, 0f, 1f).overlay().color());
        assertEquals(0xFFFFD700, PeakEffects.oneShot(PeakAnimationEffect.GLOW, 0.5f, 0f, 1f).overlay().color());
        assertEquals(0xFF55FF55, PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0f, 0f, 1f, 0x55FF55).overlay().color());
        assertEquals(0x80FF0000, PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0f, 0f, 1f, 0x80FF0000).overlay().color());
    }

    @Test
    void loopsBreatheAndGlint() {
        assertTrue(PeakEffects.loop(PeakAnimationEffect.GLOW, 0, 40, 0f).isIdentity());
        assertEquals(0.8f, PeakEffects.loop(PeakAnimationEffect.GLOW, 20, 40, 0f).overlay().strength(), 0.001f);
        assertEquals(0.6f, PeakEffects.loop(PeakAnimationEffect.FLASH, 20, 40, 0f).overlay().strength(), 0.001f);

        PeakEffectTransform glint = PeakEffects.loop(PeakAnimationEffect.SHINE, 9, 40, 0f);
        assertEquals(PeakEffectOverlay.Kind.SHINE, glint.overlay().kind());
        assertEquals(0.5f, glint.overlay().progress(), 0.001f);
        assertTrue(PeakEffects.loop(PeakAnimationEffect.SHINE, 30, 40, 0f).isIdentity());
        assertTrue(PeakEffects.loop(PeakAnimationEffect.POP, 0, 40, 0f).isIdentity());
    }

    @Test
    void combiningKeepsTheLatestVisibleOverlay() {
        PeakEffectTransform glow = PeakEffects.oneShot(PeakAnimationEffect.GLOW, 0.5f, 0f, 1f);
        PeakEffectTransform flash = PeakEffects.oneShot(PeakAnimationEffect.FLASH, 0f, 0f, 1f);
        PeakEffectTransform shake = PeakEffects.oneShot(PeakAnimationEffect.SHAKE, 0.3f, 0f, 1f);

        assertEquals(PeakEffectOverlay.Kind.FLASH, glow.then(flash).overlay().kind());
        assertEquals(PeakEffectOverlay.Kind.GLOW, glow.then(shake).overlay().kind());
        assertEquals(shake.offsetX(), glow.then(shake).offsetX(), 1e-6f);
        assertFalse(PeakEffectTransform.IDENTITY.withOverlay(new PeakEffectOverlay(PeakEffectOverlay.Kind.FLASH, 0xFFFFFFFF, 0f, 0f))
                .hasOverlay());
    }

    @Test
    void theClockRunsTriggeredOverlaysWithTheirColor() {
        PeakAnimatedClock clock = new PeakAnimatedClock();
        PeakAnimatedClock.Config config = new PeakAnimatedClock.Config(PeakAnimationEffect.NONE, 40, 0f, 0, true, 0xFF3366CC);

        assertTrue(clock.transform(config, 100.0).isIdentity());
        clock.trigger(PeakAnimationEffect.FLASH, 10, 1f, 0xFF55FF55);
        PeakEffectTransform shot = clock.transform(config, 101.0);
        assertEquals(0xFF55FF55, shot.overlay().color());

        clock.trigger(PeakAnimationEffect.GLOW, 10, 1f);
        clock.transform(config, 105.0);
        PeakEffectTransform fallback = clock.transform(config, 110.0);
        assertEquals(0xFF3366CC, fallback.overlay().color());
        assertTrue(clock.transform(config, 200.0).isIdentity());
    }

    @Test
    void theColorSurvivesTheWireAndTheTriggerPatchCarriesIt() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakAnimated.create("test:anim").effect(PeakAnimationEffect.GLOW).color(0xFFCC3333)
                .add(PeakLabel.create("test:label", "x")));
        PeakAnimated back = assertInstanceOf(PeakAnimated.class,
                PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document)).findById("test:anim"));

        assertEquals(PeakPaint.solid(0xFFCC3333), back.getColor());
        assertEquals(PeakAnimationEffect.GLOW, back.getEffect());
        assertEquals(0xFFCC3333, back.clockConfig().color());
        assertEquals(0, PeakAnimated.create("test:plain").clockConfig().color());

        back.applyPatch(PeakPatch.animated().flash(0x55FF55).build());
        PeakEffectTransform shot = back.clock().transform(back.clockConfig(), 10.0);
        assertEquals(PeakEffectOverlay.Kind.FLASH, shot.overlay().kind());
        assertEquals(0xFF55FF55, shot.overlay().color());
    }

    @Test
    void theNamedTriggerPatchesUseTheNewEffects() {
        assertEquals("POP", PeakPatch.animated().pop().build().getAsJsonObject("trigger").get("effect").getAsString());
        assertEquals("FLASH", PeakPatch.animated().flash().build().getAsJsonObject("trigger").get("effect").getAsString());
        assertEquals("GLOW", PeakPatch.animated().glow(0xFFD700).build().getAsJsonObject("trigger").get("effect").getAsString());
        assertEquals("SHINE", PeakPatch.animated().shine().build().getAsJsonObject("trigger").get("effect").getAsString());
        assertEquals("OUTLINE", PeakPatch.animated().outline(0xFF0000).build().getAsJsonObject("trigger").get("effect").getAsString());
        assertNull(PeakPatch.animated().shine().build().getAsJsonObject("trigger").get("color"));
    }

    @Test
    void theTriggerActionIsALocalPatchOfTheTrigger() {
        PeakAction action = PeakAction.trigger("test:anim", PeakAnimationEffect.FLASH, 20, 0.5f, 0xFF55FF55);

        assertEquals(PeakActionType.PATCH, action.type());
        assertEquals("test:anim", action.target());
        assertEquals("FLASH", action.patch().getAsJsonObject("trigger").get("effect").getAsString());
        assertEquals(20, action.patch().getAsJsonObject("trigger").get("ticks").getAsInt());
        assertNotNull(action.patch().getAsJsonObject("trigger").get("color"));
    }

    @Test
    void theValidatorWarnsWhenATriggerTargetsAnotherElement() {
        PeakDocument menu = PeakMenu.create("test:menu");
        menu.add(PeakPanel.create("test:plain"));
        menu.add(PeakAnimated.create("test:anim"));
        menu.add(PeakPanel.create("test:button").onClick(
                PeakAction.trigger("test:plain", PeakAnimationEffect.FLASH),
                PeakAction.trigger("test:anim", PeakAnimationEffect.FLASH)));

        var issues = PeakDocumentValidator.validate(menu, 0, 0).issues();

        assertTrue(issues.stream().anyMatch(issue -> issue.contains("test:plain") && issue.contains("animated")), issues.toString());
        assertFalse(issues.stream().anyMatch(issue -> issue.contains("test:anim") && issue.contains("animated container")));
    }
}
