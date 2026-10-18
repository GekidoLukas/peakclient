# Menus Overview

A menu is a document that opens as a screen instead of a HUD overlay. The player can click, type and scroll in it. Use it
for shops, settings, kit selection, votes and dialogs.

```java
PeakDocument shop = PeakMenu.create("mygame:shop", settings -> settings.title("Shop"));
shop.add(PeakPanel.create().absolute(PeakAnchor.CENTER, 0, 0).column().gap(6).padding(10).width(200)
        .add(PeakLabel.create("mygame:shop/title", "Shop"),
             PeakButton.text("mygame:shop/buy", "Buy sword")
                     .onClick(PeakAction.send("buy").arg("item", "sword"))
                     .tooltip("Costs 10 coins"),
             PeakButton.text("mygame:shop/close", "Close").onClick(PeakAction.close())));

PeakClient.to(player).menus().sendAndOpen(shop);
```

A menu contains the same [elements](/elements/overview) as a HUD document, plus the interactive ones from
[Input Elements](/menus/inputs) and [Templates](/menus/templates). What the server learns from clicks is covered in
[Server Events](/menus/server-events).

## Sending, opening and closing

| Call | Purpose |
| --- | --- |
| `menus().send(menu)` | stores the menu on the client without opening it |
| `menus().open(id)` | opens a stored menu |
| `menus().sendAndOpen(menu)` | both in one step |
| `menus().update(menuId, elementId, patch)` | edits a stored menu, open or closed |
| `menus().close()`, `back()` | close all menus, or go one step back |
| `menus().remove(id)`, `clear()` | delete stored menus |

Menus are stored on the client by their id. Only one menu is open at a time, and each open menu remembers the menus
before it, so `back()` returns to the previous one. A menu is gone after a disconnect. Use
`audience.menuView(menu).persistent(true)` if it must come back after a reconnect.

## Settings

`PeakMenu.create(id, settings -> ...)` configures the menu.

| Setting | Purpose |
| --- | --- |
| `title(...)` | screen title, read by the narrator (not drawn; use a label for a visible heading) |
| `escape(...)` | what Esc does: `CLOSE_ALL` (default), `BACK` or `NONE` |
| `pauseGame(boolean)` | pause the game in single player |
| `shortcut(key, actions...)` | keyboard shortcut for the whole menu |
| theme | backdrop, focus ring, tooltips, sounds, scrollbars (see [Theme](/menus/theme)) |

## Buttons and clicks

`PeakButton` is a container with a ready button look, with hover, pressed and disabled states.

```java
PeakButton.text("mygame:shop/more", "More").onClick(PeakAction.open("mygame:shop/more"));
```

- Every element can be made clickable with `onClick(...)`, `onRightClick(...)` and `onDoubleClick(...)`. See
  [Events](/interaction/events).
- Put an image or item into a `PeakButton` to give it a button look.
- `tooltip("text")` shows a tooltip. Pass an element instead for a custom look.
- `enabled(false)` disables an element.
- A click on a child, such as the label of a button, counts for the nearest clickable parent.

## States and tooltips

An element that can be clicked changes its look with its state. Each style only names what differs from the normal look.

| Call | Purpose |
| --- | --- |
| `hoverStyle(style)` | while the mouse is over it |
| `pressedStyle(style)` | while the mouse button is held |
| `disabledStyle(style)` | when `enabled(false)` |
| `activeStyle(style)` | while it is active, for example a toggled formatting button |
| `keepFocus(true)` | a click does not take the keyboard focus from a text box |

```java
button.hoverStyle(PeakStyle.create().background(0xFF4080C0))
      .pressedStyle(PeakStyle.create().background(0xFF2060A0));
```

Hover and pressed styles change colors, borders, radius and shadows of containers. To give an image or an item a state,
put it into a `PeakButton`.

A tooltip is a text (`tooltip("Costs 10 coins")`, with `\n` for more lines) or any element
(`tooltip(PeakPanel.create()...)`). Style it per element with `tooltipStyle(...)`, or for the whole menu in
[Theme](/menus/theme). A tooltip element is not part of the document, so it cannot be updated by id.

## Scrolling

Give a container `overflow(PeakOverflow.SCROLL)` and a fixed height or a `maxSize`. It then scrolls with the mouse wheel
and shows a scrollbar that can be dragged. Lists inside lists scroll first, the outer one takes over at the end. Use
fixed sizes for the children, because a percent height refers to the visible box.

Scrollbars follow the menu theme. Per container, `scrollbar(bar -> ...)` changes them and `hideScrollbar()` hides them.

## Keyboard

- Tab and Shift+Tab move the focus, and scroll the focused element into view.
- Space or Enter activates the focused element.
- Arrow keys, Page Up and Page Down, Home and End scroll lists.
- Esc leaves a text box first, then acts as set by `escape`.

## Good to know

- The server ignores actions from menus it never sent to that player. It allows 30 actions per second per player.
- When a menu is sent, the validator warns about actions that point to elements the menu does not contain.
- Rotated elements and animated popups are not hit exactly. There is no input method preview, but text confirmed by an
  input method works.
- `hideVanilla(...)` and a document time to live have no effect on a menu.
