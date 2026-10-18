# Theme

The [menu settings](/menus/overview) also define the look of the parts a menu draws itself: backdrop, focus ring, tooltips,
sounds and scrollbars. Every value can be overridden on a single element. The order is element, then menu, then the default.

```java
PeakMenu.create("mygame:shop", settings -> settings
        .backdrop(0x90102030)
        .focus(f -> f.color(0xFF55FF55).width(2))
        .clickSound("minecraft:block.note_block.bell", 1f, 1f));
```

The theme is part of the menu document. Change it by sending the menu again. It cannot be patched.

## Backdrop

The backdrop is drawn behind the menu, in this order: blur, panorama, texture, backdrop color.

| Call | Purpose |
| --- | --- |
| `backdrop(paint)`, `noBackdrop()` | color or gradient behind the menu (default is a dark layer), see [Colors and Gradients](/styling/colors-and-gradients) |
| `backdropTexture(path, width, height)` | a picture |
| `backdropFit(...)` | `COVER`, `CONTAIN` or `FILL` for the picture |
| `backdropBlur(true)` | the vanilla menu blur (follows the player's blur setting) |
| `vanillaPanorama()` | the turning title screen background |
| `backdropPanorama(path)` | your own panorama (six images `<path>_0.png` to `<path>_5.png`) |
| `backdropPanoramaSpeed(f)` | speed; negative turns the other way |

The panorama covers the world. Use `noBackdrop()` to keep it undimmed. The images must exist on the client as resources.
A missing image logs one warning and draws nothing.

## Focus ring

`settings.focus(f -> f.color(...).width(n))` styles the ring around the focused element. `settings.focus(PeakFocusStyle::none)` hides it.
On an element: `focusColor(...)` and `focusWidth(...)`.

## Tooltips

```java
settings.tooltip(t -> t.background(0xFF0A0A30).border(0xFFFFAA00).borderWidth(2).padding(6).maxWidth(110));
```

`border(top, bottom)` makes a two-tone frame. `maxWidth` wraps long tooltips (0 means no wrapping). On an element:
`tooltipStyle(...)`.

## Sounds

`clickSound(id, volume, pitch)` and `hoverSound(...)` set the sounds of the menu. `noClickSound()` turns the click off.
The hover sound plays once when the pointer enters an enabled, clickable element. On an element, `clickSound`,
`hoverSound` and `silent(true)` (no sounds at all).

## Scrollbars

`settings.scrollbar(bar -> bar.thickness(6).radius(3).thumbColor(0x55FF55))` sets the look of all scrollbars, and
`settings.hideScrollbars()` hides them. A container can override single values with `scrollbar(...)`. Options: thickness,
margin, radius, track color, thumb colors (normal, hover, dragging) and minimum thumb size.

## Invalid color

`settings.invalidColor(...)` sets the border color of invalid inputs (red by default).
