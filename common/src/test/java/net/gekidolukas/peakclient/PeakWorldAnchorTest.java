package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakAnchorMath;
import net.gekidolukas.peakclient.render.PeakCameraProjection;
import net.gekidolukas.peakclient.render.PeakEdgeClamp;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakAnchorEdge;
import net.gekidolukas.peakclient.ui.PeakAnchorScale;
import net.gekidolukas.peakclient.ui.PeakAnchorTarget;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentValidator;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakOcclusion;
import net.gekidolukas.peakclient.ui.PeakOverflow;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakPositionMode;
import net.gekidolukas.peakclient.ui.PeakWorldAnchor;
import net.gekidolukas.peakclient.ui.client.PeakOcclusionState;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakWorldAnchorTest {
    private static final float WIDTH = 400f;
    private static final float HEIGHT = 300f;

    private static Matrix4f perspective() {
        return new Matrix4f().perspective((float) Math.toRadians(70.0), WIDTH / HEIGHT, 0.05f, 512f);
    }

    private static PeakCameraProjection.Projection project(Matrix4f view, double cameraX, double cameraY, double cameraZ,
                                                           double x, double y, double z) {
        return PeakCameraProjection.project(view, perspective(), cameraX, cameraY, cameraZ, x, y, z, WIDTH, HEIGHT);
    }

    @Test
    void aPointStraightAheadLandsInTheMiddleOfTheScreen() {
        PeakCameraProjection.Projection projection = project(new Matrix4f(), 100, 64, 100, 100, 64, 90);

        assertTrue(projection.inFront());
        assertEquals(WIDTH / 2f, projection.x(), 0.01f);
        assertEquals(HEIGHT / 2f, projection.y(), 0.01f);
        assertEquals(10.0, projection.distance(), 1e-6);
        assertTrue(projection.isInside(WIDTH, HEIGHT));
    }

    @Test
    void pointsToTheRightAndAboveMoveTheScreenPositionTheRightWay() {
        PeakCameraProjection.Projection right = project(new Matrix4f(), 0, 0, 0, 3, 0, -10);
        PeakCameraProjection.Projection above = project(new Matrix4f(), 0, 0, 0, 0, 3, -10);

        assertTrue(right.x() > WIDTH / 2f);
        assertEquals(HEIGHT / 2f, right.y(), 0.01f);
        assertTrue(above.y() < HEIGHT / 2f);
        assertEquals(WIDTH / 2f, above.x(), 0.01f);
    }

    @Test
    void nearerPointsAreFartherFromTheCenter() {
        PeakCameraProjection.Projection near = project(new Matrix4f(), 0, 0, 0, 1, 0, -5);
        PeakCameraProjection.Projection far = project(new Matrix4f(), 0, 0, 0, 1, 0, -20);

        assertTrue(near.x() - WIDTH / 2f > far.x() - WIDTH / 2f);
    }

    @Test
    void aPointBehindTheCameraIsNotInFront() {
        PeakCameraProjection.Projection projection = project(new Matrix4f(), 0, 0, 0, 2, 1, 10);

        assertFalse(projection.inFront());
        assertFalse(projection.isInside(WIDTH, HEIGHT));
        assertTrue(projection.viewZ() > 0f);
        assertTrue(projection.viewX() > 0f);
    }

    @Test
    void aPointFarOffToTheSideIsInFrontButOutsideTheScreen() {
        PeakCameraProjection.Projection projection = project(new Matrix4f(), 0, 0, 0, 40, 0, -10);

        assertTrue(projection.inFront());
        assertFalse(projection.isInside(WIDTH, HEIGHT));
        assertTrue(projection.x() > WIDTH);
    }

    @Test
    void theViewRotationTurnsTheCamera() {
        Matrix4f turnedRight = new Matrix4f().rotationY((float) Math.toRadians(90.0));
        PeakCameraProjection.Projection ahead = project(turnedRight, 0, 0, 0, 10, 0, 0);
        PeakCameraProjection.Projection formerAhead = project(turnedRight, 0, 0, 0, 0, 0, -10);

        assertTrue(ahead.inFront());
        assertEquals(WIDTH / 2f, ahead.x(), 0.5f);
        assertFalse(formerAhead.isInside(WIDTH, HEIGHT));
    }

    @Test
    void theEdgeClampPutsTheMarkerOnTheBorderInTheDirectionOfTheTarget() {
        PeakEdgeClamp.Result right = PeakEdgeClamp.clamp(10f, 0f, WIDTH, HEIGHT, 20f, 20f);
        assertEquals(WIDTH - 20f, right.x(), 0.01f);
        assertEquals(HEIGHT / 2f, right.y(), 0.01f);
        assertEquals(1f, right.directionX(), 1e-4f);

        PeakEdgeClamp.Result up = PeakEdgeClamp.clamp(0f, 5f, WIDTH, HEIGHT, 20f, 20f);
        assertEquals(20f, up.y(), 0.01f);
        assertEquals(WIDTH / 2f, up.x(), 0.01f);
        assertEquals(-1f, up.directionY(), 1e-4f);

        PeakEdgeClamp.Result behind = PeakEdgeClamp.clamp(0f, 0f, WIDTH, HEIGHT, 20f, 20f);
        assertEquals(HEIGHT - 20f, behind.y(), 0.01f);
    }

    @Test
    void aDiagonalDirectionStopsAtTheNearerBorder() {
        PeakEdgeClamp.Result result = PeakEdgeClamp.clamp(10f, -10f, WIDTH, HEIGHT, 10f, 10f);

        assertTrue(result.x() <= WIDTH - 10f + 0.01f && result.y() <= HEIGHT - 10f + 0.01f);
        assertTrue(Math.abs(result.x() - (WIDTH - 10f)) < 0.01f || Math.abs(result.y() - (HEIGHT - 10f)) < 0.01f);
        assertEquals(result.x() - WIDTH / 2f, result.y() - HEIGHT / 2f, 0.01f);
    }

    @Test
    void distanceFadesInAndOutAtTheLimits() {
        assertEquals(1f, PeakAnchorMath.distanceAlpha(500, 0f, 0f, 0f));
        assertEquals(1f, PeakAnchorMath.distanceAlpha(30, 0f, 50f, 10f));
        assertEquals(0.5f, PeakAnchorMath.distanceAlpha(45, 0f, 50f, 10f), 1e-5f);
        assertEquals(0f, PeakAnchorMath.distanceAlpha(50, 0f, 50f, 10f), 1e-5f);
        assertEquals(0f, PeakAnchorMath.distanceAlpha(60, 0f, 50f, 10f));
        assertEquals(0.5f, PeakAnchorMath.distanceAlpha(4, 5f, 0f, 2f), 1e-5f);
        assertEquals(0f, PeakAnchorMath.distanceAlpha(2, 5f, 0f, 2f));
        assertEquals(0f, PeakAnchorMath.distanceAlpha(4, 5f, 0f, 0f));
        assertEquals(1f, PeakAnchorMath.distanceAlpha(5, 5f, 0f, 0f));
    }

    @Test
    void perspectiveScaleFollowsTheDistanceWithinItsLimits() {
        assertEquals(1f, PeakAnchorMath.perspectiveScale(10, 10f, 0.5f, 1.5f), 1e-5f);
        assertEquals(1.5f, PeakAnchorMath.perspectiveScale(5, 10f, 0.5f, 1.5f), 1e-5f);
        assertEquals(0.5f, PeakAnchorMath.perspectiveScale(20, 10f, 0.5f, 1.5f), 1e-5f);
        assertEquals(0.5f, PeakAnchorMath.perspectiveScale(100, 10f, 0.5f, 1.5f), 1e-5f);
        assertEquals(1.5f, PeakAnchorMath.perspectiveScale(0, 10f, 0.5f, 1.5f), 1e-5f);
    }

    @Test
    void theOcclusionStateChecksRightAwayAndThenOnItsInterval() {
        PeakOcclusionState state = new PeakOcclusionState();
        assertTrue(state.due(100));

        state.record(false, 100, 3);
        assertFalse(state.due(101));
        assertFalse(state.due(102));
        assertTrue(state.due(103));
        assertTrue(state.isKnown());
        assertFalse(state.isOccluded());
    }

    @Test
    void theOcclusionFactorSnapsFirstAndThenFadesOverTheConfiguredTicks() {
        PeakOcclusionState state = new PeakOcclusionState();
        state.record(true, 0, 3);

        assertEquals(0.35f, state.advance(10.0, 0.35f, 4), 1e-5f);
        assertEquals(0.35f, state.advance(11.0, 0.35f, 4), 1e-5f);

        assertEquals(0.60f, state.advance(12.0, 1f, 4), 1e-5f);
        assertEquals(0.85f, state.advance(13.0, 1f, 4), 1e-5f);
        assertEquals(1f, state.advance(15.0, 1f, 4), 1e-5f);
        assertEquals(0.75f, state.advance(16.0, 0f, 4), 1e-5f);
    }

    @Test
    void disablingTheOcclusionResetsToFullVisibility() {
        PeakOcclusionState state = new PeakOcclusionState();
        state.record(true, 0, 3);
        state.advance(1.0, 0f, 4);

        state.disable();

        assertTrue(state.isKnown());
        assertFalse(state.isOccluded());
        assertEquals(1f, state.factor());
        assertEquals(1f, state.advance(2.0, 1f, 4));
    }

    @Test
    void theBudgetLimitsTheChecksPerTickAndResetsOnTheNextTick() {
        PeakOcclusionState.Budget budget = new PeakOcclusionState.Budget();
        for (int i = 0; i < PeakOcclusionState.Budget.MAX_PER_TICK; i++) assertTrue(budget.tryAcquire(50));
        assertFalse(budget.tryAcquire(50));
        assertTrue(budget.tryAcquire(51));
    }

    @Test
    void theAnchorKeepsAllItsSettingsOnTheWire() {
        UUID uuid = UUID.randomUUID();
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakWorldAnchor.create("test:anchor")
                .target(PeakAnchorTarget.entity(uuid).point(PeakAnchorTarget.EntityPoint.CENTER).offset(0, 0.25, 0))
                .screenOffset(3, -4).pivot(PeakAnchor.TOP_CENTER).distanceRange(4f, 60f).fadeDistance(8f)
                .scaleWithDistance(12f, 0.6f, 1.4f).clampToEdge(20).edgeArrow(0xFFFFAA00, 9).edgeShowContent(false)
                .occlusion(PeakOcclusion.blocksAndEntities().hide().dimTo(0.2f).fadeTicks(8).checkIntervalTicks(5)
                        .entities(PeakOcclusion.EntitySet.PLAYERS).blockShape(PeakOcclusion.BlockShape.COLLIDER).ignoreTarget(false))
                .dimension("minecraft:the_nether")
                .add(PeakLabel.create("test:label", "Name")));

        PeakWorldAnchor back = assertInstanceOf(PeakWorldAnchor.class,
                PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document)).findById("test:anchor"));

        assertEquals(PeakAnchorTarget.Kind.ENTITY, back.getTarget().getKind());
        assertEquals(uuid, back.getTarget().parsedEntityUuid());
        assertEquals(PeakAnchorTarget.EntityPoint.CENTER, back.getTarget().getPoint());
        assertEquals(0.25, back.getTarget().getOffsetY());
        assertEquals(3, back.getScreenOffsetX());
        assertEquals(-4, back.getScreenOffsetY());
        assertEquals(PeakAnchor.TOP_CENTER, back.getPivot());
        assertEquals(4f, back.getMinDistance());
        assertEquals(60f, back.getMaxDistance());
        assertEquals(8f, back.getFadeDistance());
        assertEquals(PeakAnchorScale.PERSPECTIVE, back.getScaleMode());
        assertEquals(12f, back.getReferenceDistance());
        assertEquals(0.6f, back.getMinScale());
        assertEquals(1.4f, back.getMaxScale());
        assertEquals(PeakAnchorEdge.CLAMP, back.getEdge());
        assertEquals(20, back.getEdgeMargin());
        assertEquals(9, back.getEdgeArrowSize());
        assertFalse(back.isEdgeShowContent());
        PeakOcclusion occlusion = back.getOcclusion();
        assertEquals(PeakOcclusion.Mode.BLOCKS_AND_ENTITIES, occlusion.getMode());
        assertEquals(PeakOcclusion.Behavior.HIDE, occlusion.getBehavior());
        assertEquals(0.2f, occlusion.getDimTo(), 1e-6f);
        assertEquals(8, occlusion.getFadeTicks());
        assertEquals(5, occlusion.getCheckIntervalTicks());
        assertEquals(PeakOcclusion.EntitySet.PLAYERS, occlusion.getEntities());
        assertEquals(PeakOcclusion.BlockShape.COLLIDER, occlusion.getBlockShape());
        assertFalse(occlusion.isIgnoreTarget());
        assertEquals("minecraft:the_nether", back.getDimension());
    }

    @Test
    void thePartialSettingsSurviveTheWire() {
        PeakDocument document = PeakDocument.create("test:partial");
        document.add(PeakWorldAnchor.at("test:dim", 1, 2, 3).occlusion(PeakOcclusion.partial().dim().dimTo(0.5f).depthBias(1.5f)));
        document.add(PeakWorldAnchor.at("test:default", 1, 2, 3));

        PeakDocument read = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));
        PeakOcclusion dim = ((PeakWorldAnchor) read.findById("test:dim")).getOcclusion();
        PeakOcclusion standard = ((PeakWorldAnchor) read.findById("test:default")).getOcclusion();

        assertEquals(PeakOcclusion.Mode.PARTIAL, dim.getMode());
        assertEquals(PeakOcclusion.Behavior.DIM, dim.getBehavior());
        assertEquals(0.5f, dim.getDimTo());
        assertEquals(1.5f, dim.getDepthBias());
        assertEquals(PeakOcclusion.Mode.PARTIAL, standard.getMode());
        assertEquals(PeakOcclusion.Behavior.HIDE, standard.getBehavior());
    }

    @Test
    void theDefaultsMatchTheDecisions() {
        PeakWorldAnchor anchor = PeakWorldAnchor.at(1, 2, 3);

        assertEquals(PeakOcclusion.Mode.PARTIAL, anchor.getOcclusion().getMode());
        assertEquals(PeakOcclusion.Behavior.HIDE, anchor.getOcclusion().getBehavior());
        assertEquals(0.6f, anchor.getOcclusion().getDepthBias());
        assertEquals(0.35f, anchor.getOcclusion().getDimTo());
        assertEquals(PeakOcclusion.EntitySet.LIVING, anchor.getOcclusion().getEntities());
        assertEquals(PeakAnchorEdge.HIDE, anchor.getEdge());
        assertEquals(PeakAnchorScale.NONE, anchor.getScaleMode());
        assertEquals(PeakAnchor.BOTTOM_CENTER, anchor.getPivot());
        assertEquals(PeakAnchorTarget.Kind.POSITION, anchor.getTarget().getKind());
        assertEquals(1.0, anchor.getTarget().getX());
    }

    @Test
    void theBehaviorDefaultDependsOnTheMode() {
        assertEquals(PeakOcclusion.Behavior.HIDE, PeakOcclusion.partial().getBehavior());
        assertEquals(PeakOcclusion.Behavior.DIM, PeakOcclusion.blocks().getBehavior());
        assertEquals(PeakOcclusion.Behavior.DIM, PeakOcclusion.blocksAndEntities().getBehavior());
        assertEquals(PeakOcclusion.Behavior.DIM, PeakOcclusion.partial().dim().getBehavior());
        assertEquals(PeakOcclusion.Behavior.HIDE, PeakOcclusion.blocks().hide().getBehavior());
        assertTrue(PeakOcclusion.partial().isPartial());
        assertTrue(PeakOcclusion.partial().checksEntities());
        assertFalse(PeakOcclusion.blocks().isPartial());
        assertFalse(PeakOcclusion.none().isEnabled());
    }

    @Test
    void theDepthOfAPointGrowsWithTheDistanceAndTheBiasMovesItNearer() {
        PeakCameraProjection.Projection near = project(new Matrix4f(), 0, 0, 0, 0, 0, -5);
        PeakCameraProjection.Projection far = project(new Matrix4f(), 0, 0, 0, 0, 0, -20);
        PeakCameraProjection.Projection biased = project(new Matrix4f(), 0, 0, 0, 0, 0, -19.4);

        assertTrue(near.ndcZ() > -1f && near.ndcZ() < 1f);
        assertTrue(far.ndcZ() > near.ndcZ());
        assertTrue(biased.ndcZ() < far.ndcZ());
    }

    @Test
    void anAnchorIsAlwaysAbsoluteEvenAfterAPatch() {
        PeakWorldAnchor anchor = PeakWorldAnchor.at(0, 0, 0);
        assertEquals(PeakPositionMode.ABSOLUTE, anchor.getPositionMode());

        anchor.applyPatch(PeakPatch.worldAnchor().field("positionMode", "RELATIVE").build());

        assertEquals(PeakPositionMode.ABSOLUTE, anchor.getPositionMode());
    }

    @Test
    void patchesMoveAndRetargetTheAnchorAndSwitchTheOcclusion() {
        PeakWorldAnchor anchor = PeakWorldAnchor.at(0, 0, 0);
        UUID uuid = UUID.randomUUID();

        anchor.applyPatch(PeakPatch.worldAnchor().moveTo(5, 6, 7).build());
        assertEquals(5.0, anchor.getTarget().getX());
        assertEquals(7.0, anchor.getTarget().getZ());

        anchor.applyPatch(PeakPatch.worldAnchor().followEntity(uuid).build());
        assertEquals(PeakAnchorTarget.Kind.ENTITY, anchor.getTarget().getKind());
        assertEquals(uuid, anchor.getTarget().parsedEntityUuid());

        anchor.applyPatch(PeakPatch.worldAnchor().followWaypoint("test:wp").build());
        assertEquals("test:wp", anchor.getTarget().getWaypointId());

        anchor.applyPatch(PeakPatch.worldAnchor().noOcclusion().build());
        assertEquals(PeakOcclusion.Mode.NONE, anchor.getOcclusion().getMode());
        assertEquals(PeakOcclusion.Behavior.DIM, anchor.getOcclusion().getBehavior());

        anchor.applyPatch(PeakPatch.worldAnchor().occlusion(PeakOcclusion.blocksAndEntities().hide()).edge(PeakAnchorEdge.CLAMP)
                .maxDistance(30f).build());
        assertEquals(PeakOcclusion.Mode.BLOCKS_AND_ENTITIES, anchor.getOcclusion().getMode());
        assertEquals(PeakAnchorEdge.CLAMP, anchor.getEdge());
        assertEquals(30f, anchor.getMaxDistance());
    }

    @Test
    void theValidatorFlagsAnchorsThatCannotWork() {
        PeakDocument hud = PeakDocument.create("test:hud");
        hud.add(PeakWorldAnchor.at("test:fine", 0, 0, 0));
        hud.add(PeakPanel.create("test:rotated").rotationDegrees(10f).add(PeakWorldAnchor.at("test:in-rotated", 0, 0, 0)));
        hud.add(PeakPanel.create("test:scroll").overflow(PeakOverflow.SCROLL).add(PeakWorldAnchor.at("test:in-scroll", 0, 0, 0)));
        hud.add(PeakWorldAnchor.create("test:bad-entity").target(PeakAnchorTarget.entity("not-a-uuid")));
        hud.add(PeakWorldAnchor.create("test:no-waypoint").target(PeakAnchorTarget.waypoint("")));
        hud.add(PeakWorldAnchor.at("test:range", 0, 0, 0).distanceRange(50f, 20f));

        List<String> issues = PeakDocumentValidator.validate(hud, 0, 0).issues();

        assertTrue(issues.stream().anyMatch(issue -> issue.contains("test:in-rotated")), issues.toString());
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("test:in-scroll")), issues.toString());
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("test:bad-entity") && issue.contains("uuid")), issues.toString());
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("waypoint id")), issues.toString());
        assertTrue(issues.stream().anyMatch(issue -> issue.contains("test:range")), issues.toString());
        assertFalse(issues.stream().anyMatch(issue -> issue.contains("'test:fine'")), issues.toString());
    }

    @Test
    void anchorsInMenusAreReported() {
        PeakDocument menu = PeakMenu.create("test:menu");
        menu.add(PeakWorldAnchor.at("test:anchor", 0, 0, 0));

        List<String> issues = PeakDocumentValidator.validate(menu, 0, 0).issues();

        assertTrue(issues.stream().anyMatch(issue -> issue.contains("test:anchor") && issue.contains("menu")), issues.toString());
        assertNotEquals(0, issues.size());
    }
}
