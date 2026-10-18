# Naming and IDs

## IDs

Documents, elements, menus, actions, value keys and typed references share one id format:

- One `:` for the namespace, everything after it separated with `/`.
- Examples: `mygame:shop/title`, `mygame:menu/settings/volume`.
- An id without a namespace (`title`) is allowed.
- A second `:` is rejected with an `IllegalArgumentException`.

Use your own namespace for every id so mods do not clash. Templates build the ids of their children with `/`, for
example `mygame:tabs/tab-0` and `mygame:list/page-1`.

An element without an id gets a random one. Keep the element object or use a typed reference if you need to change it
later. See [Updating Elements](/concepts/updates).

## Method names

PeakClient uses two styles, depending on the kind of class.

| Kind | Reading | Writing |
| --- | --- | --- |
| Changeable classes (elements, styles, settings, waypoints) | `getX()` or `isX()` | `x(value)`, chainable |
| Unchangeable values (actions, sources, chart entries, references) | `x()` | built once |

For example, `style.borderColor(0xFF55FF55)` sets and `style.getBorderColor()` reads.

## Class names

Public classes start with `Peak`. Patch classes end with `Patch` (`PeakLabelPatch`) and are created through `PeakPatch`
(`PeakPatch.label()`).
