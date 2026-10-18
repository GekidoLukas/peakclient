package net.gekidolukas.peakclient.ui;

public final class PeakOcclusion {
    public enum Mode { NONE, PARTIAL, BLOCKS, BLOCKS_AND_ENTITIES }

    public enum Behavior { DIM, HIDE }

    public enum BlockShape { VISUAL, COLLIDER }

    public enum EntitySet { PLAYERS, LIVING, ALL }

    private Mode mode = Mode.PARTIAL;
    private Behavior behavior;
    private float dimTo = 0.35f;
    private BlockShape blockShape = BlockShape.VISUAL;
    private EntitySet entities = EntitySet.LIVING;
    private int fadeTicks = 4;
    private int checkIntervalTicks = 3;
    private boolean ignoreTarget = true;
    private float depthBias = 0.6f;

    public static PeakOcclusion none() { return new PeakOcclusion().mode(Mode.NONE); }
    public static PeakOcclusion partial() { return new PeakOcclusion().mode(Mode.PARTIAL); }
    public static PeakOcclusion blocks() { return new PeakOcclusion().mode(Mode.BLOCKS); }
    public static PeakOcclusion blocksAndEntities() { return new PeakOcclusion().mode(Mode.BLOCKS_AND_ENTITIES); }

    public Mode getMode() { return mode == null ? Mode.PARTIAL : mode; }
    public PeakOcclusion mode(Mode mode) { this.mode = mode; return this; }

    public Behavior getBehavior() {
        if (behavior != null) return behavior;
        return getMode() == Mode.PARTIAL ? Behavior.HIDE : Behavior.DIM;
    }
    public PeakOcclusion behavior(Behavior behavior) { this.behavior = behavior; return this; }
    public PeakOcclusion hide() { return behavior(Behavior.HIDE); }
    public PeakOcclusion dim() { return behavior(Behavior.DIM); }

    public float getDimTo() { return Math.max(0f, Math.min(1f, dimTo)); }
    public PeakOcclusion dimTo(float factor) { this.dimTo = Math.max(0f, Math.min(1f, factor)); return this; }

    public BlockShape getBlockShape() { return blockShape == null ? BlockShape.VISUAL : blockShape; }
    public PeakOcclusion blockShape(BlockShape shape) { this.blockShape = shape; return this; }

    public EntitySet getEntities() { return entities == null ? EntitySet.LIVING : entities; }
    public PeakOcclusion entities(EntitySet entities) { this.entities = entities; return this; }

    public int getFadeTicks() { return Math.max(1, fadeTicks); }
    public PeakOcclusion fadeTicks(int ticks) { this.fadeTicks = Math.max(1, ticks); return this; }

    public int getCheckIntervalTicks() { return Math.max(1, checkIntervalTicks); }
    public PeakOcclusion checkIntervalTicks(int ticks) { this.checkIntervalTicks = Math.max(1, ticks); return this; }

    public boolean isIgnoreTarget() { return ignoreTarget; }
    public PeakOcclusion ignoreTarget(boolean ignore) { this.ignoreTarget = ignore; return this; }

    public float getDepthBias() { return Math.max(0f, depthBias); }
    public PeakOcclusion depthBias(float blocks) { this.depthBias = Math.max(0f, blocks); return this; }

    public boolean isEnabled() { return getMode() != Mode.NONE; }
    public boolean isPartial() { return getMode() == Mode.PARTIAL; }
    public boolean checksEntities() { return getMode() == Mode.BLOCKS_AND_ENTITIES || getMode() == Mode.PARTIAL; }
}
