# Documents and Elements

## Documents

A document is the root of one interface. It has an id and holds a tree of elements. You build it on the server and send
it to the players. The client shows it until you remove it.

```java
PeakDocument hud = PeakDocument.create("mygame:hud");
hud.add(PeakLabel.create("mygame:title", "Capture the Flag"));

PeakClient.to(players).sendUi(hud);
PeakClient.to(players).removeUi("mygame:hud");
```

- Several documents can be visible at the same time. Use one per purpose (scoreboard, notifications, minimap).
- Sending a document with an id that is already shown replaces it.
- A document can also be a menu, which the player opens and clicks. See [Menus](/menus/overview).

## Elements

An element is one building block of the interface: a label, a panel, an image, a timer and so on.

- **Containers** hold other elements and lay them out (panel, div, table, flow and others).
- **Leaf elements** show something and have no children (label, image, progress bar and others).

Every element is created with a builder (`PeakLabel.create(id, text)`) and configured with chained calls. Containers take
children with `add(...)`.

## Document options

| Call | Purpose |
| --- | --- |
| `scaling(...)` | `GUI_SCALE_DEPENDENT` (default) follows the player's GUI scale. `FIXED` keeps pixel sizes the same on every screen |
| `documentTimeToLiveTicks(n)` | removes the document on the client when it was not updated for `n` ticks (0 = stays until removed) |
| `deleteIfEmpty(true)` | an empty document removes the shown one with the same id |
| `hideVanilla(...)` | hides parts of the vanilla HUD while the document is shown, see [Vanilla HUD](/vanilla-hud) |

Menus have their own settings. See [Menus](/menus/overview).

## Ids

An id lets you find an element later to update or delete it.

- Format: one `:` for the namespace, everything after it separated with `/`, for example `mygame:shop/title`.
- Use your own namespace so your ids never clash with other mods. See [Naming and IDs](/reference/naming-and-ids).
- A second `:` is rejected with an `IllegalArgumentException`.
- An element without an id gets a random one. Keep the element object or a typed reference if you want to change it
  later.

## Classes

An element can carry class names (`addClass("score-row")`). One update by class changes every element with that class. See
[Updating Elements](/concepts/updates).

## Visibility

- `hidden(true)` hides an element.
- A hidden element gives up its space by default. `collapseWhenHidden(false)` keeps the space free instead.
