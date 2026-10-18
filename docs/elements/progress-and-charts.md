# Progress and Charts

## Progress Bar

A horizontal bar that shows how full something is.

```java
PeakProgressBar bar = PeakProgressBar.create("mygame:flag-progress")
        .maxValue(100).size(120, 8)
        .fillColor(PeakPaint.gradient(0xFF55FF55, 0xFFFFFF55))
        .transitionTicks(10);
bar.setValue(40);
```

| Option | Purpose |
| --- | --- |
| `source(...)` | `MANUAL` (you set the value) or a stat of the viewer: `HEALTH`, `ARMOR`, `HUNGER`, `SATURATION`, `EXPERIENCE_PROGRESS`, `EXPERIENCE_LEVEL`, `AIR` |
| `maxValue(n)` | value that counts as full |
| `transitionTicks(n)` | smooth movement to a new value |
| `autoIncrementPerTick(f)` | fills by itself, for example a capture bar |
| `fillColor`, `backgroundColor` | colors or gradients |
| `fillTexture`, `backgroundTexture` | textures instead of colors |
| `hideIfEmpty`, `hideIfFull` | hide the bar at the ends |

Change the value later with a progress bar patch (`PeakPatch.progressBar().value(60)`).

## Radial Progress

A ring that fills like a clock. It has the same value options as the progress bar.

```java
PeakRadialProgress.create("mygame:cooldown").size(32, 32).thickness(4).clockwise(true);
```

`thickness(px)` sets the ring width, `startAngle(deg)` where it starts, `arcDegrees(deg)` makes a partial ring.

## Icon Bar

A row of icons, like the hearts or hunger in the vanilla HUD. It can follow a stat of the viewer or a value you set.

```java
PeakIconBar.create("mygame:lives").value(3).maxValue(5).maxIcons(5)
        .fullIconTexture("mygame:textures/gui/heart_full.png")
        .emptyIconTexture("mygame:textures/gui/heart_empty.png");
```

Without textures it draws colored squares (`fallbackFullColor` and related options). `useHalfIcons(true)` allows half
icons.

## Bar Chart

Compares a few values as bars.

```java
PeakBarChart.create()
        .addEntry("Red", 12, PeakPaint.RED)
        .addEntry("Blue", 9, PeakPaint.BLUE)
        .showValueLabels(true).size(160, 80);
```

`maxValue(n)` fixes the scale, otherwise it follows the largest value. Labels, spacing and colors are options.

## Pie Chart

Shows shares of a whole.

```java
PeakPieChart.create()
        .addEntry("Red", 60, PeakPaint.RED)
        .addEntry("Blue", 40, PeakPaint.BLUE)
        .legendPosition(PeakLegendPosition.RIGHT).donutThickness(10);
```

`donutThickness(px)` turns it into a ring. `startAngle(deg)` rotates the first slice. The legend can sit at any side or be
turned off.

Charts are generic. Use them for votes, team shares or anything else with a few categories.

## Updating charts

An update replaces the whole list of entries.

```java
PeakClient.to(players).updateElement("mygame:hud", "mygame:votes", PeakPatch.barChart().entries(List.of(
        PeakChartEntry.of("Red", 14, PeakPaint.RED),
        PeakChartEntry.of("Blue", 9, PeakPaint.BLUE))));
```

The same works for pie charts with `PeakPatch.pieChart().entries(...)`. Other options, such as `maxValue` or the legend, can
be patched on their own.
