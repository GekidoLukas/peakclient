# Values and Placeholders

Values separate what the document looks like from the data it shows. The document stays the same and only the value
travels to the client. This is the cheapest way to show numbers that change often.

## Placeholders

A placeholder is a name in curly braces inside a text: `{namespace:key}`. Add a fallback after `#` for the time before
the value exists: `{mygame:kills#0}`.

```java
PeakLabel.create("mygame:kills", "Kills: {mygame:kills#0}");
```

Placeholders work in label texts, texture paths and player ids.

## Value keys

A value key names a value and fixes its type.

```java
static final PeakValueKey<Integer> KILLS = PeakValueKey.ofInt("mygame:kills");
static final PeakValueKey<String> TEAM = PeakValueKey.ofString("mygame:team");

PeakLabel.create("mygame:kills", "Kills: " + KILLS.placeholder("0"));
```

There are keys for strings, integers, longs, doubles and booleans.

Use `PeakValueKey.ofComponent("mygame:name")` for a styled or translated value. The player sees the value in their own language, with its colors and fonts. A value that has no style of its own takes the style of the placeholder. In places that need plain text, such as a texture path or a number, only the text of the value is used.

## Setting values

```java
static final PeakValueKey<Integer> PLAYERS_LEFT = PeakValueKey.ofInt("mygame:players_left");

PeakClient.to(players).setValue(TEAM, "Red");   // one value for these players

audience.values().set(TEAM, "Red");                                    // same value for everyone
audience.values().bind(PLAYERS_LEFT, () -> game.alive(), 10);          // read every 10 ticks
audience.values().bindPerPlayer(KILLS, uuid -> stats.kills(uuid), 10); // own value per player
```

On an audience, values follow the members: new members get the current value, and the bindings end with the audience.

## Bindings

A binding connects any field of an element to a placeholder, so the value controls how the element looks.

```java
panel.bind("hidden", "{mygame:show_panel#false}");
panel.bind("opacity", "{mygame:fade#1}");
```

Field names are the names in the data, with dots for nested fields (`style.background`, `size.width.value`).
Colors take `#RRGGBB`, `#AARRGGBB` or a gradient. `PeakBindingHelper` writes these expressions for you. See
[Colors and Gradients](/styling/colors-and-gradients).

If a value cannot be resolved, the field keeps its normal value.

A binding ends when you patch the bound field yourself: the patched value stays and the field no longer follows the
placeholder. To change the value and keep the binding, send the binding again in the same patch. `unbind` ends a binding
and puts the original value back.

## Built-in placeholders

The client also knows `{mc:name}` placeholders from the game itself, such as the player name, health, position or
whether the player is sneaking. They need no server code. See [Built-in Placeholders](/reference/built-in-placeholders).

Reactions can also watch values. See [Events](/interaction/events).
