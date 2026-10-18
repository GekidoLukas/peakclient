package net.gekidolukas.peakclient.render;

import com.mojang.authlib.GameProfile;
import net.gekidolukas.peakclient.ui.PeakEntityCopyMode;
import net.gekidolukas.peakclient.ui.PeakEntityElement;
import net.gekidolukas.peakclient.ui.PeakPixelSize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public final class PeakEntityElementRenderer implements PeakElementRenderer<PeakEntityElement>, PeakElementLifecycleListener {
    @Override
    public boolean writesDepth() { return true; }

    private static final float FEET_OFFSET_BLOCKS = 0.1f;

    private final Map<String, Entity> renderCache = new HashMap<>();

    public PeakEntityElementRenderer() {
        PeakElementLifecycleRegistry.register(this);
    }

    @Override
    public void renderContent(PeakRenderContext ctx, PeakEntityElement element, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Entity resolved = resolveRenderEntity(element, mc);
        if (!(resolved instanceof LivingEntity livingEntity)) return;

        float entityScale = Math.max(livingEntity.getScale(), 0.0001f);
        EntityDimensions standing = livingEntity.getDimensions(Pose.STANDING);
        float blocksHigh = Math.max(standing.height(), 0.1f);
        float blocksWide = Math.max(standing.width(), 0.1f);
        float pixelsPerBlock = Math.min(height / blocksHigh, width / blocksWide) * 0.9f * element.getZoom();
        float scale = pixelsPerBlock / entityScale;

        Vector3f translation = new Vector3f(0, -FEET_OFFSET_BLOCKS * entityScale, 0);
        Quaternionf poseRotation = new Quaternionf()
                .rotateZ((float) Math.PI)
                .rotateX((float) Math.toRadians(element.getPitch()))
                .rotateY((float) Math.toRadians(element.getYaw()));

        Quaternionf cameraOrientation = new Quaternionf()
                .rotateX((float) Math.toRadians(element.getPitch()))
                .rotateY((float) Math.toRadians(element.getYaw()));
        var dispatcher = mc.getEntityRenderDispatcher();
        Quaternionf previousCameraOrientation = dispatcher.cameraOrientation();

        RotationSnapshot snapshot = captureAndResetRotation(livingEntity);

        boolean offscreen = element.getOpacity() < 0.999f;
        if (offscreen) PeakOffscreenOpacity.begin(mc, ctx.graphics());
        PeakEntityNameTagState.set(element.isShowNameTag());
        try {
            InventoryScreen.renderEntityInInventory(
                    ctx.graphics(), width / 2f, height, scale, translation, poseRotation, cameraOrientation, livingEntity);
        } finally {
            PeakEntityNameTagState.reset();
            if (offscreen) PeakOffscreenOpacity.end(mc, ctx.graphics(), element.getOpacity());
            dispatcher.overrideCameraOrientation(previousCameraOrientation);
            restoreRotation(livingEntity, snapshot);
        }
    }

    @Override
    public PeakPixelSize measureContent(Font font, PeakEntityElement element) {
        return PeakPixelSize.ZERO;
    }

    @Override
    public void onElementRemoved(String elementId) {
        renderCache.remove(elementId);
        renderCache.keySet().removeIf(key -> key.startsWith(elementId + ":"));
    }

    private record RotationSnapshot(float xRot, float yRot, float xRotO, float yRotO,
                                    float yBodyRot, float yBodyRotO, float yHeadRot, float yHeadRotO) {}

    private RotationSnapshot captureAndResetRotation(LivingEntity entity) {
        RotationSnapshot snapshot = new RotationSnapshot(entity.getXRot(), entity.getYRot(),
                entity.xRotO, entity.yRotO, entity.yBodyRot, entity.yBodyRotO, entity.yHeadRot, entity.yHeadRotO);
        entity.setXRot(0);
        entity.setYRot(0);
        entity.xRotO = 0;
        entity.yRotO = 0;
        entity.yBodyRot = 0;
        entity.yBodyRotO = 0;
        entity.yHeadRot = 0;
        entity.yHeadRotO = 0;
        return snapshot;
    }

    private void restoreRotation(LivingEntity entity, RotationSnapshot snapshot) {
        entity.setXRot(snapshot.xRot());
        entity.setYRot(snapshot.yRot());
        entity.xRotO = snapshot.xRotO();
        entity.yRotO = snapshot.yRotO();
        entity.yBodyRot = snapshot.yBodyRot();
        entity.yBodyRotO = snapshot.yBodyRotO();
        entity.yHeadRot = snapshot.yHeadRot();
        entity.yHeadRotO = snapshot.yHeadRotO();
    }

    private Entity resolveRenderEntity(PeakEntityElement element, Minecraft mc) {
        return switch (element.getSource().kind()) {
            case PLAYER -> element.getCopyMode() == PeakEntityCopyMode.FULL
                    ? mc.player : proxyFor(mc.player, element, mc);
            case TARGETED_ENTITY -> {
                if (!(mc.hitResult instanceof EntityHitResult hit)) yield null;
                Entity target = hit.getEntity();
                yield element.getCopyMode() == PeakEntityCopyMode.FULL ? target : proxyFor(target, element, mc);
            }
            case PLAYER_TEXTURE -> {
                var source = element.getSource();
                String texturePath = PeakValueResolver.string(source.texturePath());
                if (texturePath == null) yield null;
                String key = element.getId() + ":tex:" + texturePath + ":" + source.slim();
                Entity ghost = renderCache.computeIfAbsent(key, k -> createTextureGhost(texturePath, source.slim(), mc));
                if (ghost != null) ghost.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                yield ghost;
            }
            case FIXED -> {
                ResourceLocation typeLocation = PeakValueResolver.location(element.getSource().entityTypeId());
                if (typeLocation == null) yield null;
                yield renderCache.computeIfAbsent(element.getId() + ":fixed:" + typeLocation, k -> {
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(typeLocation);
                    return type == null ? null : type.create(mc.level);
                });
            }
        };
    }

    private Entity createTextureGhost(String texturePath, boolean slim, Minecraft mc) {
        PlayerSkin skin = PeakTextureSkins.get(texturePath, slim);
        if (skin == null) return null;
        PeakGhostPlayer ghost = new PeakGhostPlayer(mc.level, new GameProfile(UUID.randomUUID(), "Player"));
        ghost.skinOverride(skin);
        ghost.showAllModelParts();
        ghost.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        return ghost;
    }

    private Entity proxyFor(Entity source, PeakEntityElement element, Minecraft mc) {
        if (source == null) return null;
        String key = element.getId() + ":" + source.getUUID() + ":" + element.getCopyMode();
        Entity proxy = renderCache.computeIfAbsent(key, k -> createProxy(source, element, mc));
        if (proxy != null) syncProxy(source, proxy, element.getCopyMode());
        return proxy;
    }

    private Entity createProxy(Entity source, PeakEntityElement element, Minecraft mc) {
        if (source instanceof Player player) {
            PeakGhostPlayer ghost = new PeakGhostPlayer(mc.level, player.getGameProfile());
            ghost.setPos(player.getX(), player.getY(), player.getZ());
            return ghost;
        }

        Entity copy = source.getType().create(mc.level);
        if (copy == null) return null;

        CompoundTag tag = new CompoundTag();
        source.saveWithoutId(tag);
        copy.load(tag);
        copy.setUUID(UUID.randomUUID());

        if (copy instanceof LivingEntity livingCopy && element.getCopyMode() == PeakEntityCopyMode.TYPE_AND_SKIN) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                livingCopy.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        return copy;
    }

    private void syncProxy(Entity source, Entity proxy, PeakEntityCopyMode mode) {
        if (proxy instanceof PeakGhostPlayer ghost && source instanceof Player player) {
            ghost.copyModelParts(player);
            ghost.setPos(player.getX(), player.getY(), player.getZ());
        }
        if (mode != PeakEntityCopyMode.TYPE_AND_SKIN_EQUIPMENT) return;
        if (!(source instanceof LivingEntity livingSource) || !(proxy instanceof LivingEntity livingProxy)) return;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack wanted = livingSource.getItemBySlot(slot);
            if (!ItemStack.matches(livingProxy.getItemBySlot(slot), wanted)) {
                livingProxy.setItemSlot(slot, wanted.copy());
            }
        }
    }
}
