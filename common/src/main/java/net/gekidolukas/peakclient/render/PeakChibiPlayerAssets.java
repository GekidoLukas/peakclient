package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class PeakChibiPlayerAssets {
    static final float HEAD_SCALE = 2f;
    static final float HEAD_SIDE_CLEARANCE = 1.0125f;
    private static final float BODY_HEIGHT = 10f;
    private static final float LIMB_HEIGHT = 10f;
    private static final float LEG_HEIGHT = 6f;
    private static final float HAT_DEFORM = 0.5f;
    private static final float LAYER_DEFORM = 0.25f;
    private static final float HAT_HALF_SIZE = (4f + HAT_DEFORM) * HEAD_SCALE;
    private static final float LIMB_HALF_WIDTH = 5f + 3f + LAYER_DEFORM;
    private static final float LIMB_HALF_DEPTH = 2f + LAYER_DEFORM;

    public static final float MODEL_TOP = -(8f + HAT_DEFORM) * HEAD_SCALE;
    public static final float MODEL_BOTTOM = BODY_HEIGHT + LEG_HEIGHT + LAYER_DEFORM;
    public static final float MODEL_HEIGHT = MODEL_BOTTOM - MODEL_TOP;
    public static final float MODEL_CENTER_Y = (MODEL_TOP + MODEL_BOTTOM) / 2f;
    public static final float REFERENCE_BLOCK_SCALE = 30f;

    private static ModelPart classicModel;
    private static ModelPart slimModel;

    private PeakChibiPlayerAssets() {}

    public static float halfWidth(float rotationRadians) {
        float cos = Math.abs((float) Math.cos(rotationRadians));
        float sin = Math.abs((float) Math.sin(rotationRadians));
        return Math.max(HAT_HALF_SIZE * (cos + sin), LIMB_HALF_WIDTH * cos + LIMB_HALF_DEPTH * sin);
    }

    public static float pixelsPerModelUnit(float width, float height, float rotationRadians) {
        if (width <= 0f || height <= 0f) return 0f;
        return Math.min(width / (2f * halfWidth(rotationRadians)), height / MODEL_HEIGHT);
    }

    public static PeakPixelSize naturalSize(float rotationRadians) {
        float unit = REFERENCE_BLOCK_SCALE / 16f;
        return new PeakPixelSize(
                (int) Math.ceil(2f * halfWidth(rotationRadians) * unit),
                (int) Math.ceil(MODEL_HEIGHT * unit)
        );
    }

    public static ModelPart get(boolean slim) {
        if (slim) {
            if (slimModel == null) slimModel = bake(true);
            return slimModel;
        }
        if (classicModel == null) classicModel = bake(false);
        return classicModel;
    }

    private static ModelPart bake(boolean slim) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f, CubeDeformation.NONE),
                PartPose.offset(0f, 0f, 0f));

        float bodyHeight = BODY_HEIGHT;
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4f, 0f, -2f, 8f, bodyHeight, 4f, CubeDeformation.NONE),
                PartPose.offset(0f, 0f, 0f));

        float armHeight = LIMB_HEIGHT;
        float armWidth = slim ? 3f : 4f;
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3f, -2f, -2f, armWidth, LIMB_HEIGHT, 4f, CubeDeformation.NONE),
                PartPose.offset(-5f, 2f, 0f));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1f, -2f, -2f, armWidth, LIMB_HEIGHT, 4f, CubeDeformation.NONE),
                PartPose.offset(5f, 2f, 0f));

        float legHeight = LEG_HEIGHT;
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2f, 0f, -2f, 4f, legHeight, 4f, CubeDeformation.NONE),
                PartPose.offset(-1.9f, bodyHeight, 0f));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 48).addBox(-2f, 0f, -2f, 4f, legHeight, 4f, CubeDeformation.NONE),
                PartPose.offset(1.9f, bodyHeight, 0f));

        root.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f, new CubeDeformation(0.5f)),
                PartPose.offset(0f, 0f, 0f));

        root.addOrReplaceChild("jacket",
                CubeListBuilder.create().texOffs(16, 32).addBox(-4f, 0f, -2f, 8f, BODY_HEIGHT, 4f, new CubeDeformation(0.25f)),
                PartPose.offset(0f, 0f, 0f));

        root.addOrReplaceChild("right_sleeve",
                CubeListBuilder.create().texOffs(40, 32).addBox(-3f, -2f, -2f, armWidth, LIMB_HEIGHT, 4f, new CubeDeformation(0.25f)),
                PartPose.offset(-5f, 2f, 0f));

        root.addOrReplaceChild("left_sleeve",
                CubeListBuilder.create().texOffs(48, 48).addBox(-1f, -2f, -2f, armWidth, LIMB_HEIGHT, 4f, new CubeDeformation(0.25f)),
                PartPose.offset(5f, 2f, 0f));

        root.addOrReplaceChild("right_pants",
                CubeListBuilder.create().texOffs(0, 32).addBox(-2f, 0f, -2f, 4f, LEG_HEIGHT, 4f, new CubeDeformation(0.25f)),
                PartPose.offset(-1.9f, BODY_HEIGHT, 0f));

        root.addOrReplaceChild("left_pants",
                CubeListBuilder.create().texOffs(0, 48).addBox(-2f, 0f, -2f, 4f, LEG_HEIGHT, 4f, new CubeDeformation(0.25f)),
                PartPose.offset(1.9f, BODY_HEIGHT, 0f));

        LayerDefinition layer = LayerDefinition.create(mesh, 64, 64);
        ModelPart rootPart = layer.bakeRoot();

        ModelPart head = rootPart.getChild("head");
        head.xScale = HEAD_SCALE; head.yScale = HEAD_SCALE; head.zScale = HEAD_SCALE;

        return rootPart;
    }
}