# Layout and Containers

Containers hold other elements and decide where they go. All containers share the options from the
[Elements Overview](/elements/overview):
`row()`, `column()`, `gap`, `alignChildren`, `mainAxisAlign`, `overflow` and `add(...)`.

## Div

A plain container without a look. Use it to group elements and arrange them in a row or a column.

```java
PeakDiv.create("mygame:stats").row().gap(6).add(
        PeakIconBar.create(), PeakLabel.create("Lives"));
```

## Panel

A container with a dark, semi-transparent background. Use it for boxes, cards and rows of a list. Its style is easy to
change (`background`, `border`, `borderRadius`, `padding`).

```java
PeakPanel.create("mygame:card").padding(6).borderRadius(4).add(PeakLabel.create("Hello"));
```

## Table

Places children in a grid with columns and rows, for scoreboards and stat lists.

```java
PeakTableLayoutPanel scores = PeakTableLayoutPanel.create("mygame:scores")
        .columns("24px 1fr 60px")
        .spacing(4).width(200);
scores.row(PeakLabel.create("#"), PeakLabel.create("Player"), PeakLabel.create("Score"));
scores.row(PeakLabel.create("1"), PeakLabel.create("Alex"), PeakLabel.create("120"));
```

| Option | Purpose |
| --- | --- |
| `columns(...)`, `rows(...)` | track sizes: `auto`, pixels (`60px`), percent (`25%`) or a share of the free space (`1fr`) |
| `spacing(n)`, `columnSpacing`, `rowSpacing` | space between cells |
| `cellPadding(...)` | space inside every cell |
| `cellAlign(horizontal, vertical)` | where a cell's content sits in its cell |
| `row(...)`, `empty()`, `emptyRow(height)` | add a row, skip a cell, add a spacer row |
| `colSpan(n)`, `rowSpan(n)` on a cell | cell covers several columns or rows |
| `decor(...)` | header row, striped rows, lines between rows and columns, styles for single rows or columns |

`1fr` and percent sizes need a known table size. In a table without a fixed width they behave like `auto`.

## Flow

Places children in lines and wraps to the next line when the line is full. Use it for tags, badges, player heads or
inventories.

```java
PeakFlow tags = PeakFlow.create("mygame:tags").width(180).spacing(4);
tags.add(tag("PvP"), tag("Survival"), PeakFlow.lineBreak(), tag("Events"));
```

| Option | Purpose |
| --- | --- |
| `width(...)` or `wrapSize(px)` | where the line breaks |
| `gap(n)`, `lineGap(n)`, `spacing(n)` | space inside a line, between lines, or both |
| `mainAxisAlign`, `alignChildren`, `lineAlign` | placement inside a line, across it, and of the lines in a taller box |
| `maxLines(n)` | cuts off more lines |
| `reverse(true)` | mirrors the order |
| `PeakFlow.lineBreak()` | forces a new line |

`direction(COLUMN)` makes it wrap into columns. A flow without any width limit stays on one line.

## Conditional

Shows its children only while the viewer meets a condition.

```java
PeakConditional.create("mygame:low-health")
        .addCondition(PeakCondition.healthBelow(6))
        .add(PeakLabel.create("§cLow health!"));
```

Conditions: dimension, game mode, sneaking, sprinting, swimming, flying, riding, underwater, day, night, health below or
above, looking at an entity, looking at a block. `requireAll(false)` shows the children if any condition is met.

## Popup

A container that appears with an animation and goes away on its own. Use it for notifications and short messages.

```java
PeakPopUpContainer.create("mygame:toast")
        .enterAnimation(PeakPopupAnimation.SLIDE_FROM_RIGHT)
        .exitAnimation(PeakPopupAnimation.FADE)
        .timeToLiveTicks(60)
        .add(PeakLabel.create("Flag captured!"));
```

For plain notifications, skip the container and use `PeakClient.to(players).notify("Flag captured!")`, or
`notifySuccess`, `notifyWarning` and `notifyError`.

Animations: `NONE`, `FADE`, `SCALE`, `SLIDE_FROM_*`, `WIPE_FROM_*` and `FLIP_*`. The same animations work in the cycler,
stagger, feed and sorted list. See [Animation and Displays](/elements/animation-and-displays).

## Shape

Clips its children to a rounded rectangle, a circle or a polygon. Use it for round avatars, badges and pill-shaped panels.
For avatars, see [Images, Items and Players](/elements/media).

```java
PeakShape.circle().size(48, 48).border(2, 0xFFFFD700)
        .add(PeakPlayerIcon.clientPlayer().size(48, 48));
PeakShape.rounded(10).size(120, 60).background(0xFF202830);
```

Other shapes: `triangle()`, `diamond()`, `pentagon()`, `hexagon()`, `hexagonFlat()`, `octagon()`, `star()`.
`cornerRadius(n)` rounds the corners of polygons.

`popOutTop(f)`, `popOutBottom(f)`, `popOutLeft(f)` and `popOutRight(f)` free one side from the clip, so a head can stick
out of a round badge. `f` is the part of the box, from that edge, that is not clipped.

A shape inside another shape, or inside an element with `opacity` below 1, is clipped with a plain rectangle instead.

## Spacer

An empty element of a fixed size, to leave a gap.

```java
PeakSpacer.create(0, 8);
```
