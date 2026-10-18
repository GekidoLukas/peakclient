# Elements Overview

Every element shares a set of properties. The following pages in this section describe the individual elements:
[Text](/elements/text), [Images, Items and Players](/elements/media), [Progress and Charts](/elements/progress-and-charts),
[Maps and Navigation](/elements/maps-and-navigation), [Layout and Containers](/elements/layout) and
[Animation and Displays](/elements/animation-and-displays).

## Size

| Call | Purpose |
| --- | --- |
| `size(w, h)`, `width(px)`, `height(px)` | fixed size in pixels |
| `widthPercent(p)`, `heightPercent(p)` | share of the parent's content box |
| `minSize(w, h)`, `maxSize(w, h)` | limits for automatic sizes |

Without a size an element is as big as its content. A percent size inside a parent without a fixed size counts as automatic.

## Position

Elements are laid out by their container, one after the other. To place an element freely, use an anchor:

```java
element.absolute(PeakAnchor.TOP_RIGHT, -10, 10);
```

An absolute element does not take part in the layout of its siblings.

## Spacing

- `padding(...)` is the space inside an element.
- `margin(...)` is the space around it.
- `alignSelf(...)` overrides how the container aligns this one child.

A border takes no space in the layout. Use a padding at least as large as the border.

## Appearance

| Call | Purpose |
| --- | --- |
| `background`, `border`, `borderRadius`, `boxShadow` | box style, see [Box Style](/styling/box-style) and [Colors and Gradients](/styling/colors-and-gradients) |
| `opacity(0..1)` | transparency of the element and all of its children |
| `scale(f)` | draw size factor |
| `rotationDegrees(d)` | rotates the drawing only; layout and clipping still use the unrotated box |

## Visibility

`hidden(true)` hides an element. By default it also gives up its space; `collapseWhenHidden(false)` keeps the space.

## Containers

Containers add these options for their children:

| Call | Purpose |
| --- | --- |
| `row()`, `column()` | direction of the layout |
| `gap(n)` | space between children |
| `alignChildren(...)` | alignment across the layout direction |
| `mainAxisAlign(...)` | distribution along the layout direction (start, center, end, space between, space around) |
| `overflow(...)` | clip children that stick out, or scroll them in menus |
| `add(a, b, c)` | add children |

## Behavior

- `bind(field, expression)` connects a field to a value. See [Values and Placeholders](/concepts/values-and-placeholders).
- `onClick(...)`, `onValueChange(...)` and related calls attach reactions. See [Events](/interaction/events).
- `sortKey(n)` is the number a sorted list orders its children by.
- `addClass(name)` groups elements for updates by class.

## Inspecting elements

Press F3 and 0 together to toggle the debug overlay on the client. It outlines every element, shows the details of the
element under the mouse (size, position, style, bindings and live values) and shows how long drawing takes. The mouse wheel
selects the parent or child of that element, and a click copies its id. It also works inside menus.
