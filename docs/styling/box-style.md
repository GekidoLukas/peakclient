# Box Style

Every element has a box. These options style it. Colors can be plain colors or gradients, see
[Colors and Gradients](/styling/colors-and-gradients).

## Options

| Call | Purpose |
| --- | --- |
| `background(...)` | fill color or gradient |
| `padding(...)` | space inside the element |
| `margin(...)` | space around the element |
| `border(width, color)` | frame |
| `borderRadius(px)` | rounded corners |
| `boxShadow(color, offsetX, offsetY, blur)` | shadow behind the element |
| `opacity(0..1)` | transparency of the element and its children |
| `overflow(...)` | what happens to children that stick out |

```java
PeakPanel.create("mygame:card")
        .padding(6)
        .background(0xC0202830)
        .border(1, PeakPaint.GRAY)
        .borderRadius(6)
        .boxShadow(0x80000000, 2, 2, 4);
```

`padding` and `margin` accept one value for all sides, two values (vertical, horizontal) or four (top, right, bottom,
left).

## Border and layout

A border takes no space in the layout. Children start at the padding, so use a padding at least as large as the border.

The background lies under the border, like CSS `border-box`. A half transparent border over a dark panel looks darker.
For a see-through border, make the background transparent.

## Overflow

| Value | Behavior |
| --- | --- |
| `VISIBLE` | children may draw outside |
| `HIDDEN` | children are clipped at the inner edge of the border |
| `SCROLL` | clipped, and scrollable in menus; in a HUD it behaves like `HIDDEN` |

Clipping is rectangular, even with a `borderRadius`. A scrolling container needs a fixed or maximum size.

## Opacity

`opacity` applies to the whole element including its children, so overlapping children do not show through each other.
Items, entities, blocks and player icons fade correctly too.

## Rotation and scale

`rotationDegrees(d)` and `scale(f)` change only how the element is drawn. Layout, automatic sizes and clipping keep using
the original box, so a rotated element can overlap its neighbors.

## Changing one thing

In an [update](/concepts/updates), change single fields instead of replacing the whole style:

```java
PeakPatch.panel().background(0xFF336699).padding(8);
```

`style(PeakStyle)` replaces everything.
