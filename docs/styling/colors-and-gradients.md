# Colors and Gradients

Every color in the UI is a paint. A paint is a plain color, a linear gradient or a radial gradient. Every color field
accepts all three: backgrounds, borders, shadows, text, progress bars, charts, scrollbars and more.

## Plain colors

Every color method exists twice: one takes an `int`, the other a `PeakPaint`.

```java
panel.background(0x3366CC);                 // opaque blue
panel.background(0x80000000);               // half transparent black
panel.background(PeakPaint.BLACK.withAlpha(0.6f));
```

For `int` values:

- `0xRRGGBB` is fully opaque.
- `0` means transparent, or no color.
- A set alpha byte is respected (`0x80000000` is half transparent).

Ready-made colors: `PeakPaint.WHITE`, `BLACK`, `RED`, `GREEN`, `BLUE`, `YELLOW`, `ORANGE`, `GRAY`. Create your own with
`PeakPaint.rgb(r, g, b)`, `rgba(r, g, b, a)` or `hex("#RRGGBB")`.

## Gradients

```java
PeakPaint warm = PeakPaint.gradient(0xFFFF5555, 0xFFFFFF55);                   // left to right
PeakPaint cool = PeakPaint.gradient(0xFF55FFFF, 0xFF5555FF).vertical();        // top to bottom
PeakPaint tilted = PeakPaint.gradient(0xFFFF5555, 0xFF5555FF).angle(45f);      // any angle
PeakPaint rainbow = PeakPaint.gradient(0xFFFF5555, 0xFF55FF55, 0xFF5555FF).animated(80);   // 80 ticks per loop
PeakPaint spot = PeakPaint.radial(0xFFFFFF55, 0xFF5555FF);                     // from the center outwards
```

| Option | Purpose |
| --- | --- |
| more than two colors | add more colors; they are spread evenly |
| `vertical()`, `horizontal()`, `angle(deg)` | direction of a linear gradient (0° is left to right, 90° top to bottom) |
| `animated(ticks)` | the colors move; the value is the time of one full loop |
| `PeakRadialGradient.builder()` | radial gradient with a custom center, radius and stops |

An animated gradient is a closed loop, so there is no visible seam.

## What a gradient covers

The gradient is spread over the thing it paints:

- backgrounds, borders and shadows: the element
- text: the line of text
- progress bar: the filled part
- pie chart: the whole circle

Gradients work with rounded corners too.

## Using paints

```java
PeakPanel.create("mygame:card")
        .background(warm).border(2, rainbow).borderRadius(8);
PeakLabel.create("mygame:title", "Champions").color(rainbow);
```

## Bound colors

A bound color field (see [Values and Placeholders](/concepts/values-and-placeholders)) takes a plain color (`#RRGGBB`,
`#AARRGGBB`, `0x...`) or a gradient as text. Placeholders can sit
inside it, so a value can carry a whole color or only one stop. `PeakBindingHelper` writes these expressions for you.

```java
PeakValueKey<String> TEAM_COLOR = PeakValueKey.ofString("mygame:team_color");

panel.bind(PeakBindingHelper.Paths.STYLE_BACKGROUND, PeakBindingHelper.gradient()
        .stop(0f, TEAM_COLOR, 0xFFFFFFFF)
        .stop(1f, PeakPaint.BLACK)
        .vertical().expression());

PeakClient.to(players).setValue(TEAM_COLOR, "#FF5555");
```

## Limits

- Radial gradients are circles, not ellipses.
- Colors inside rich text (`§x` codes) and the value of a color picker are plain numbers, not paints.
- Single stops of a gradient cannot be bound on their own. Bind the whole paint, or use placeholders inside the expression.
