package net.gekidolukas.peakclient.render;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Team;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

final class PeakBuiltInPlaceholders {
    private static final String DEFAULT_REAL_TIME_PATTERN = "HH:mm";
    private static final Map<String, Function<Context, String>> RESOLVERS = new LinkedHashMap<>();

    private record Context(Minecraft minecraft, LocalPlayer player, ClientLevel level, String argument) {
        int decimals(int fallback) {
            return intOrDefault(argument, fallback);
        }
    }

    static {
        registerPosition();
        registerWorld();
        registerIdentity();
        registerPlayerStatus();
        registerTargets();
        registerTeamAndScore();
        registerFlags();
    }

    private PeakBuiltInPlaceholders() {}

    static Set<String> names() {
        return Set.copyOf(RESOLVERS.keySet());
    }

    static String resolve(String key, String argument) {
        Function<Context, String> resolver = RESOLVERS.get(key);
        if (resolver == null) return null;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return null;
        return resolver.apply(new Context(minecraft, minecraft.player, minecraft.level, argument));
    }

    private static void register(String name, Function<Context, String> resolver) {
        RESOLVERS.put(name, resolver);
    }

    private static void registerPosition() {
        register("x", context -> format(context.player().getX(), context.decimals(1)));
        register("y", context -> format(context.player().getY(), context.decimals(1)));
        register("z", context -> format(context.player().getZ(), context.decimals(1)));
        register("block_x", context -> Integer.toString(context.player().getBlockX()));
        register("block_y", context -> Integer.toString(context.player().getBlockY()));
        register("block_z", context -> Integer.toString(context.player().getBlockZ()));
        register("chunk_x", context -> Integer.toString(context.player().getBlockX() >> 4));
        register("chunk_z", context -> Integer.toString(context.player().getBlockZ() >> 4));
        register("facing", context -> facing(context.player().getYRot(), context.decimals(4)));
        register("yaw", context -> format(Mth.wrapDegrees(context.player().getYRot()), context.decimals(1)));
        register("pitch", context -> format(context.player().getXRot(), context.decimals(1)));
        register("speed", context -> {
            Vec3 movement = context.player().getDeltaMovement();
            return format(Math.sqrt(movement.x * movement.x + movement.z * movement.z) * 20.0, context.decimals(1));
        });
        register("distance_to", context -> distanceTo(context, true));
        register("horizontal_distance_to", context -> distanceTo(context, false));
    }

    private static void registerWorld() {
        register("biome", context -> biome(context));
        register("dimension", context -> dimension(context));
        register("time", context -> gameClock(context.level().getDayTime()));
        register("day", context -> Long.toString(context.level().getDayTime() / 24000L));
        register("weather", context -> I18n.get("peakclient.weather." + weather(context.level())));
        register("moon_phase", context -> Integer.toString(context.level().getMoonPhase()));
        register("light", context -> light(context));
        register("time_real", context -> realTime(context.argument()));
        register("minecraft_version", context -> SharedConstants.getCurrentVersion().getName());
        register("fps", context -> Integer.toString(context.minecraft().getFps()));
    }

    private static void registerIdentity() {
        register("player_name", context -> context.player().getGameProfile().getName());
        register("player_display_name", context -> context.player().getDisplayName().getString());
        register("player_uuid", context -> context.player().getUUID().toString());
        register("world_name", context -> worldName(context.minecraft()));
        register("ping", context -> ping(context));
        register("players_online", context -> playersOnline(context));
    }

    private static void registerPlayerStatus() {
        register("player_health", context -> format(context.player().getHealth(), context.decimals(1)));
        register("player_max_health", context -> format(context.player().getMaxHealth(), context.decimals(1)));
        register("player_absorption", context -> format(context.player().getAbsorptionAmount(), context.decimals(1)));
        register("player_hunger", context -> format(context.player().getFoodData().getFoodLevel(), context.decimals(1)));
        register("player_saturation", context -> format(context.player().getFoodData().getSaturationLevel(), context.decimals(1)));
        register("player_armor", context -> format(context.player().getArmorValue(), context.decimals(1)));
        register("player_air", context -> format(context.player().getAirSupply(), context.decimals(1)));
        register("player_experience_progress", context -> format(context.player().experienceProgress, context.decimals(1)));
        register("player_experience_level", context -> Integer.toString(context.player().experienceLevel));
        register("player_xp_total", context -> Integer.toString(context.player().totalExperience));
        register("player_selected_slot", context -> Integer.toString(context.player().getInventory().selected + 1));
        register("player_selected_item_name", context -> itemName(context.player().getMainHandItem()));
        register("player_offhand_item_name", context -> itemName(context.player().getOffhandItem()));
        register("held_item_count", context -> heldCount(context.player().getMainHandItem()));
        register("held_item_durability", context -> heldDurability(context.player().getMainHandItem(), false));
        register("held_item_max_durability", context -> heldDurability(context.player().getMainHandItem(), true));
        register("effect_count", context -> Integer.toString(context.player().getActiveEffects().size()));
        register("gamemode", context -> gameMode(context.minecraft()));
    }

    private static void registerTargets() {
        register("target_block", context -> targetBlock(context));
        register("target_block_id", context -> targetBlockId(context));
        register("target_entity", context -> targetEntity(context).map(entity -> entity.getDisplayName().getString()).orElse(""));
        register("target_entity_type", context -> targetEntity(context)
                .map(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()).orElse(""));
        register("target_player_name", context -> targetEntity(context)
                .filter(Player.class::isInstance).map(entity -> ((Player) entity).getGameProfile().getName()).orElse(""));
        register("target_entity_health", context -> targetLiving(context)
                .map(living -> format(living.getHealth(), context.decimals(1))).orElse(""));
        register("target_entity_max_health", context -> targetLiving(context)
                .map(living -> format(living.getMaxHealth(), context.decimals(1))).orElse(""));
        register("target_entity_armor", context -> targetLiving(context)
                .map(living -> format(living.getArmorValue(), context.decimals(1))).orElse(""));
    }

    private static void registerTeamAndScore() {
        register("team_name", context -> teamName(context.player()));
        register("team_color", context -> teamColor(context.player()));
        register("score", context -> score(context));
    }

    private static void registerFlags() {
        register("is_sneaking", context -> Boolean.toString(context.player().isShiftKeyDown()));
        register("is_sprinting", context -> Boolean.toString(context.player().isSprinting()));
        register("is_swimming", context -> Boolean.toString(context.player().isSwimming()));
        register("is_flying", context -> Boolean.toString(context.player().getAbilities().flying));
        register("is_on_ground", context -> Boolean.toString(context.player().onGround()));
        register("is_in_water", context -> Boolean.toString(context.player().isInWater()));
        register("is_riding", context -> Boolean.toString(context.player().isPassenger()));
        register("is_raining", context -> Boolean.toString(context.level().isRaining()));
        register("is_day", context -> Boolean.toString(context.level().isDay()));
        register("has_target_entity", context -> Boolean.toString(targetEntity(context).isPresent()));
        register("has_target_block", context -> Boolean.toString(!targetBlock(context).isEmpty()));
    }

    static int intOrDefault(String raw, int fallback) {
        if (raw == null) return fallback;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    static String format(double value, int decimals) {
        return String.format(Locale.ROOT, "%." + Math.max(0, Math.min(decimals, 8)) + "f", value);
    }

    static String gameClock(long dayTime) {
        long ticksOfDay = Math.floorMod(dayTime, 24000L);
        long totalMinutes = (ticksOfDay * 60L / 1000L + 6L * 60L) % (24L * 60L);
        return String.format(Locale.ROOT, "%02d:%02d", totalMinutes / 60L, totalMinutes % 60L);
    }

    static String realTime(String pattern) {
        String used = pattern == null || pattern.isBlank() ? DEFAULT_REAL_TIME_PATTERN : pattern.trim();
        try {
            return LocalTime.now().format(DateTimeFormatter.ofPattern(used, Locale.ROOT));
        } catch (IllegalArgumentException | DateTimeException e) {
            return "";
        }
    }

    private static String facing(float yaw, int mode) {
        if (mode >= 8) {
            String[] directions = {"south", "southwest", "west", "northwest", "north", "northeast", "east", "southeast"};
            int index = Math.floorMod(Math.round(yaw / 45f), 8);
            return I18n.get("peakclient.direction." + directions[index]);
        }
        String[] directions = {"south", "west", "north", "east"};
        int index = Math.floorMod(Math.round(yaw / 90f), 4);
        return I18n.get("peakclient.direction." + directions[index]);
    }

    private static String biome(Context context) {
        return context.level().getBiome(context.player().blockPosition()).unwrapKey()
                .map(key -> translatedOrFallback(
                        "biome." + key.location().getNamespace() + "." + key.location().getPath(),
                        key.location().getPath()))
                .orElse("?");
    }

    private static String dimension(Context context) {
        var location = context.level().dimension().location();
        return translatedOrFallback("dimension." + location.getNamespace() + "." + location.getPath(), location.getPath());
    }

    private static String translatedOrFallback(String key, String fallback) {
        String translated = I18n.get(key);
        return translated.equals(key) ? fallback : translated;
    }

    private static String weather(ClientLevel level) {
        if (level.isThundering()) return "thunder";
        return level.isRaining() ? "rain" : "clear";
    }

    private static String light(Context context) {
        var position = context.player().blockPosition();
        String mode = context.argument() == null ? "" : context.argument().trim().toLowerCase(Locale.ROOT);
        int block = context.level().getBrightness(LightLayer.BLOCK, position);
        int sky = context.level().getBrightness(LightLayer.SKY, position);
        return switch (mode) {
            case "block" -> Integer.toString(block);
            case "sky" -> Integer.toString(sky);
            default -> Integer.toString(Math.max(block, sky));
        };
    }

    private static String worldName(Minecraft minecraft) {
        var server = minecraft.getSingleplayerServer();
        return server == null ? "" : server.getWorldData().getLevelName();
    }

    private static String ping(Context context) {
        ClientPacketListener connection = context.minecraft().getConnection();
        if (connection == null) return "";
        var info = connection.getPlayerInfo(context.player().getUUID());
        return info == null ? "" : Integer.toString(info.getLatency());
    }

    private static String playersOnline(Context context) {
        ClientPacketListener connection = context.minecraft().getConnection();
        return connection == null ? "" : Integer.toString(connection.getOnlinePlayers().size());
    }

    private static String itemName(ItemStack stack) {
        return stack.isEmpty() ? "" : stack.getHoverName().getString();
    }

    private static String heldCount(ItemStack stack) {
        return stack.isEmpty() ? "0" : Integer.toString(stack.getCount());
    }

    private static String heldDurability(ItemStack stack, boolean max) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return "";
        int maxDamage = stack.getMaxDamage();
        return Integer.toString(max ? maxDamage : maxDamage - stack.getDamageValue());
    }

    private static String gameMode(Minecraft minecraft) {
        if (minecraft.gameMode == null) return "";
        GameType type = minecraft.gameMode.getPlayerMode();
        return translatedOrFallback("selectWorld.gameMode." + type.getName(), type.getName());
    }

    private static String targetBlock(Context context) {
        HitResult hit = context.minecraft().hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) return "";
        return context.level().getBlockState(blockHit.getBlockPos()).getBlock().getName().getString();
    }

    private static String targetBlockId(Context context) {
        HitResult hit = context.minecraft().hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() == HitResult.Type.MISS) return "";
        return BuiltInRegistries.BLOCK.getKey(context.level().getBlockState(blockHit.getBlockPos()).getBlock()).toString();
    }

    private static Optional<Entity> targetEntity(Context context) {
        if (context.minecraft().hitResult instanceof EntityHitResult entityHit) {
            return Optional.of(entityHit.getEntity());
        }
        return Optional.empty();
    }

    private static Optional<LivingEntity> targetLiving(Context context) {
        return targetEntity(context).filter(LivingEntity.class::isInstance).map(entity -> (LivingEntity) entity);
    }

    private static String teamName(LocalPlayer player) {
        Team team = player.getTeam();
        if (team instanceof PlayerTeam playerTeam) return playerTeam.getDisplayName().getString();
        return team == null ? "" : team.getName();
    }

    private static String teamColor(LocalPlayer player) {
        if (!(player.getTeam() instanceof PlayerTeam team)) return "";
        Integer rgb = team.getColor().getColor();
        return rgb == null ? "" : String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    private static String score(Context context) {
        if (context.argument() == null || context.argument().isBlank()) return "";
        Objective objective = context.level().getScoreboard().getObjective(context.argument().trim());
        if (objective == null) return "";
        ReadOnlyScoreInfo info = context.level().getScoreboard().getPlayerScoreInfo(context.player(), objective);
        return info == null ? "" : Integer.toString(info.value());
    }

    private static String distanceTo(Context context, boolean includeY) {
        int expected = includeY ? 3 : 2;
        double[] coordinates = parseCoordinates(context.argument(), expected);
        if (coordinates == null) return "";

        double dx = context.player().getX() - coordinates[0];
        double distanceSquared;
        if (includeY) {
            double dy = context.player().getY() - coordinates[1];
            double dz = context.player().getZ() - coordinates[2];
            distanceSquared = dx * dx + dy * dy + dz * dz;
        } else {
            double dz = context.player().getZ() - coordinates[1];
            distanceSquared = dx * dx + dz * dz;
        }
        return format(Math.sqrt(distanceSquared), 1);
    }

    private static double[] parseCoordinates(String raw, int expectedCount) {
        if (raw == null) return null;
        String[] parts = raw.split(";");
        if (parts.length != expectedCount) return null;
        try {
            double[] result = new double[expectedCount];
            for (int i = 0; i < expectedCount; i++) result[i] = Double.parseDouble(parts[i].trim());
            return result;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
