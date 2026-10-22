# Built-in Placeholders

Besides your own values, the client fills `{minecraft:name}` (short: `{mc:name}`) from the game itself. A label can show
them without any server code. For your own values, see [Values and Placeholders](/concepts/values-and-placeholders).

Syntax: `{mc:name=argument#fallback}`. The fallback is used when the value does not exist, for example when the player
looks at no block. Numbers take the number of decimals as the argument: `{mc:x=2}`.

```java
PeakLabel.create("mygame:pos", "Position: {mc:block_x} {mc:block_y} {mc:block_z}");
PeakPanel.create("mygame:sneak-hint").bind("hidden", "{mc:is_sneaking}");
```

| Group | Names |
| --- | --- |
| Identity | `player_name`, `player_display_name`, `player_uuid`, `world_name` (single player only), `ping`, `players_online` |
| Position | `x`, `y`, `z`, `block_x`, `block_y`, `block_z`, `chunk_x`, `chunk_z`, `facing` (`=8` for eight directions), `yaw`, `pitch`, `speed` (blocks per second), `distance_to=x;y;z`, `horizontal_distance_to=x;z` |
| World and time | `biome`, `dimension`, `time` (game clock `HH:MM`), `day`, `weather`, `moon_phase`, `light` (`=block` or `=sky`), `time_real` (system clock, argument is a time pattern, default `HH:mm`), `minecraft_version`, `fps` |
| Player status | `gamemode`, `player_health`, `player_max_health`, `player_absorption`, `player_hunger`, `player_saturation`, `player_armor`, `player_air`, `player_experience_progress`, `player_experience_level`, `player_xp_total`, `player_selected_slot`, `player_selected_item_name`, `player_offhand_item_name`, `held_item_count`, `held_item_durability`, `held_item_max_durability`, `effect_count` |
| Target | `target_block`, `target_block_id`, `target_entity`, `target_entity_type`, `target_player_name`, `target_entity_health`, `target_entity_max_health`, `target_entity_armor` |
| Team and score | `team_name`, `team_color` (`#RRGGBB`), `score=<objective>` |
| Flags | `is_sneaking`, `is_sprinting`, `is_swimming`, `is_flying`, `is_on_ground`, `is_in_water`, `is_riding`, `is_raining`, `is_day`, `has_target_entity`, `has_target_block` |

`player_display_name` and `target_entity` keep the colors and styles of the name, for example a team color.

## Flags and bindings

Flags return `true` or `false`, so they are made for bindings:

```java
panel.bind("hidden", "{mc:is_sneaking}");
label.bind("color", "{mc:team_color#FFFFFF}");
```

The first hides the panel while the player sneaks. The second colors the label like the player's team.
`PeakBindingHelper.builtin(name)` writes the placeholder text for you.

## Limits

- `score` and `team_*` read the client's scoreboard, which only holds what the server sends: objectives that are displayed
  somewhere (sidebar, list, below name).
- `ping` is 0 in single player. `world_name` is empty on servers.
- The server address and name are deliberately not available.
- A `#` cannot be part of an argument, because it starts the fallback.
