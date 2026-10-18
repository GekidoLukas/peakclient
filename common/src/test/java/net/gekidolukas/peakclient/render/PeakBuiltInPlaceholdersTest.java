package net.gekidolukas.peakclient.render;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakBuiltInPlaceholdersTest {
    @Test
    void theDocumentedNamesAreRegistered() {
        Set<String> names = PeakBuiltInPlaceholders.names();
        for (String name : new String[]{
                "x", "y", "z", "block_x", "block_y", "block_z", "chunk_x", "chunk_z", "facing", "yaw", "pitch", "speed",
                "distance_to", "horizontal_distance_to", "biome", "dimension", "time", "day", "weather", "moon_phase",
                "light", "time_real", "minecraft_version", "fps", "player_name", "player_display_name", "player_uuid",
                "world_name", "ping", "players_online", "player_health", "player_max_health", "player_absorption",
                "player_hunger", "player_saturation", "player_armor", "player_air", "player_experience_progress",
                "player_experience_level", "player_xp_total", "player_selected_slot", "player_selected_item_name",
                "player_offhand_item_name", "held_item_count", "held_item_durability", "held_item_max_durability",
                "effect_count", "gamemode", "target_block", "target_block_id", "target_entity", "target_entity_type",
                "target_player_name", "target_entity_health", "target_entity_max_health", "target_entity_armor",
                "team_name", "team_color", "score", "is_sneaking", "is_sprinting", "is_swimming", "is_flying",
                "is_on_ground", "is_in_water", "is_riding", "is_raining", "is_day", "has_target_entity",
                "has_target_block"}) {
            assertTrue(names.contains(name), name);
        }
        assertFalse(names.contains("server_name"));
        assertFalse(names.contains("server_address"));
    }

    @Test
    void unknownNamesAreNotResolved() {
        assertNull(PeakBuiltInPlaceholders.resolve("no_such_placeholder", null));
    }

    @Test
    void numbersUseTheRootLocaleAndAClampedPrecision() {
        assertEquals("3.14", PeakBuiltInPlaceholders.format(3.14159, 2));
        assertEquals("3", PeakBuiltInPlaceholders.format(3.14159, 0));
        assertEquals("3", PeakBuiltInPlaceholders.format(3.14159, -4));
        assertEquals("3.14159000", PeakBuiltInPlaceholders.format(3.14159, 40));
    }

    @Test
    void argumentsFallBackToTheDefault() {
        assertEquals(2, PeakBuiltInPlaceholders.intOrDefault("2", 1));
        assertEquals(1, PeakBuiltInPlaceholders.intOrDefault("many", 1));
        assertEquals(1, PeakBuiltInPlaceholders.intOrDefault(null, 1));
    }

    @Test
    void theGameClockStartsAtSixInTheMorning() {
        assertEquals("06:00", PeakBuiltInPlaceholders.gameClock(0));
        assertEquals("12:00", PeakBuiltInPlaceholders.gameClock(6000));
        assertEquals("18:00", PeakBuiltInPlaceholders.gameClock(12000));
        assertEquals("00:00", PeakBuiltInPlaceholders.gameClock(18000));
        assertEquals("06:00", PeakBuiltInPlaceholders.gameClock(24000));
        assertEquals("06:30", PeakBuiltInPlaceholders.gameClock(24500));
    }

    @Test
    void theRealTimeAcceptsPatternsAndRejectsBrokenOnes() {
        assertTrue(PeakBuiltInPlaceholders.realTime(null).matches("\\d{2}:\\d{2}"));
        assertTrue(PeakBuiltInPlaceholders.realTime("HH:mm:ss").matches("\\d{2}:\\d{2}:\\d{2}"));
        assertEquals("", PeakBuiltInPlaceholders.realTime("nonsense pattern {"));
    }
}
