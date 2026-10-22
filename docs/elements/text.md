# Text

## Label

Shows text. Use it for everything from a title to a score line.

```java
PeakLabel.create("mygame:title", "§6Capture the Flag").shadow(true);
PeakLabel.create("mygame:welcome", Component.translatable("mygame.welcome", playerName));
```

| Option | Purpose |
| --- | --- |
| `color(...)` | text color or gradient |
| `shadow(boolean)` | drop shadow |
| `wrapWidth(px)` | wraps long text at this width (0 = no wrapping) |
| `lineSpacing(px)` | space between wrapped lines |
| `countUp(ticks)` | a changed number counts up or down to its new value |
| `typewriter(charsPerSecond)` | reveals the text character by character |

- Text may contain format codes (`§6`, `§l`) and placeholders such as `{mygame:score#0}`. See
  [Values and Placeholders](/concepts/values-and-placeholders) and [Built-in Placeholders](/reference/built-in-placeholders).
- A gradient in `color(...)` is spread over the line. See [Colors and Gradients](/styling/colors-and-gradients).
- Text is a Minecraft `Component`. A `String` becomes plain text. A `Component.translatable(...)` uses a language key, so each
  player sees the text in their own language. Colors, fonts, bold, italic, underline, strikethrough and obfuscated of a `Component`
  are drawn. A style that ends with its part does not carry into the next part. Hover and click events are removed and never used.
- `PeakLabel.translatable(key, args...)` is a short form for a translatable label.
- Count-up looks at the first number of the text only. It is meant for values you set now and then, such as a score.
  For time, use a timer.

## Timer

Counts time down or up on the client. You set it once and do not have to send every second.

```java
PeakTimer.countDownFrom(600).format(PeakTimerFormat.MM_SS).prefix("Time left: ");
```

| Option | Purpose |
| --- | --- |
| `countDownFrom(ticks)`, `countUpFrom(ticks)` | start value in ticks (20 ticks = 1 second) |
| `target(ticks)` | value where the timer stops |
| `format(...)` | how the time is written (`MM_SS`, `HH_MM_SS`, `SECONDS`, `DYNAMIC` and more) |
| `prefix(...)`, `suffix(...)` | text around the time |
| `color(...)`, `shadow(...)` | look |
| `running(boolean)` | pause and resume |

Control a running timer from the server with `pauseTimer`, `resumeTimer` and `syncTimer` on the targets. Use
`onTimerEnd(...)` to react when it reaches its target. See [Events](/interaction/events).

## Tooltip Label

A label with a tooltip-style background, for hints and short captions.

```java
PeakTooltipLabel.create("mygame:hint", "Hold sneak to aim")
        .backgroundStyle(PeakTooltipBackgroundStyle.VANILLA);
```

The background is a drawn box, the vanilla tooltip look, or a custom texture. `tooltipPadding(horizontal, vertical)` sets
the space between text and edge on each side. A custom texture is split into nine parts with `backgroundTexture(path, width, height, corner)`:
the corners stay as they are and the edges and the middle are repeated from their centre, so the cut-off parts are equal on both sides. The text keeps
the corner size as its minimum distance to the edge.

## Components everywhere

Every text that is shown to the player accepts a `String` or a Minecraft `Component`: labels, tooltips, timer prefix and
suffix, chart entry names, waypoint names, text box placeholders, dropdown placeholders and options, radio group options,
checkbox text, the item count of an item slot, menu titles and the texts of templates. Use `Component.translatable(...)`
for text in the player's language and `Component.literal(...)` with styles for colors and bold.

```java
PeakBarChart.create().addEntry(Component.translatable("mygame.team.red"), 12, PeakPaint.RED);
PeakWaypoint.of(120, 70, -40, Component.translatable("mygame.flag"));
PeakTimer.countDownFrom(600).prefix(Component.translatable("mygame.time_left"));
```

Placeholders such as `{mygame:score#0}` work inside these texts. A placeholder takes the style of its first character, and a value with format codes
keeps them. Custom fonts, for example `Style.EMPTY.withFont(ResourceLocation.parse("minecraft:alt"))`, are drawn. Hover events, click events and
insertions of a `Component` are always removed.

## Languages

PeakClient ships its own texts in English and German: the units of timers, the compass directions, the weather and the texts of
[templates](/menus/templates#texts-and-languages). They follow the language of the player. To change or add a language, add a resource pack or
mod resource with `assets/peakclient/lang/<language>.json` and the same keys.

Your own texts use `Component.translatable("mygame.key")` and a lang file in your mod. The client logs one warning per
translation key that is missing in the current language, so typos show up in the log.
