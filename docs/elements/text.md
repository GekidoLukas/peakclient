# Text

## Label

Shows text. Use it for everything from a title to a score line.

```java
PeakLabel.create("mygame:title", "§6Capture the Flag").shadow(true);
PeakLabel.translatable("mygame:welcome", "mygame.welcome", playerName);
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
- `translatable` uses a language key, so each player sees the text in their own language.
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
the space inside.
