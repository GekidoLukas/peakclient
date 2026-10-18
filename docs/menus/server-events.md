# Server Events

Menus run on the client. The server hears from a menu only through actions you define, and through navigation events.
The same actions are used by [Events](/interaction/events) on any element.

## Actions

A click or an input change runs a list of actions in order.

| Action | Purpose |
| --- | --- |
| `PeakAction.send(actionId)` | tells the server. Add data with `.arg(key, value)` |
| `PeakAction.open(menuId)`, `replaceWith(menuId)` | open another menu, or replace the current one in the history |
| `PeakAction.close()`, `back()` | close all menus, or go back one step |
| `PeakAction.patch(elementId, patch)` | change an element on the client, without the server |
| `PeakAction.toggle(elementId)` | show or hide an element on the client |
| `PeakAction.copyText(text)` | copy text to the clipboard |
| `PeakAction.setColor(pickerId, argb)` | set a color picker |
| `PeakAction.format(boxId, format)` | format a rich text box |
| `PeakAction.sound(id)`, `trigger(id, effect)` | play a sound or a highlight effect |

Actions that stay on the client are cheap. Use them for everything that does not need the server.

## Handling actions

```java
PeakClient.menu("mygame:shop").on("buy", event -> {
    event.player().sendSystemMessage(Component.literal("You bought " + event.arg("item")));
    PeakClient.to(event.player()).menus().back();
});
```

Handlers run on the server thread. `PeakClient.onMenuAction(listener)` receives every action of every menu.

The server ignores actions from menus it never sent to that player, limits the size of ids and arguments, and allows 30
actions per second per player. Treat the arguments as untrusted input.

## Reading input values

An action can collect the values of [input elements](/menus/inputs).

```java
button.onClick(PeakAction.send("save").collectAll());
button.onClick(PeakAction.send("save").collect("mygame:name", "mygame:amount"));
```

The arguments are `elementId -> value` (at most 32). `onSubmit` and `onChange` also add `value`. A checkbox gives `true`
or `false`, a color picker gives `#RRGGBB`.

## Forms and validation

Mark inputs with `required(true)` and add `.validate()` to the action. The client then checks the required inputs
before sending. An empty one is marked invalid, shows its error text and stops the whole action list, so a following
`close()` does not run either.

```java
saveButton.onClick(PeakAction.send("save").collectAll().validate(), PeakAction.close());
```

The server can answer with errors it found itself:

```java
PeakClient.to(player).menus().formError(menuId, fieldId, inputId, "At least 3 characters");
PeakClient.to(player).menus().clearFormError(menuId, fieldId, inputId);
```

## Keyboard shortcuts

Shortcuts belong to the menu:

```java
PeakMenu.create("mygame:shop", settings -> settings.shortcut("E", PeakAction.close()));
```

Keys: letters, digits, `F1` to `F12` and a few special keys. Modifiers (`ctrl+S`, `shift+TAB`) must match exactly. A held
key fires once. Shortcuts are skipped while a text box has the focus, unless Ctrl or Alt is held. The server sees the
action with the element id `shortcut:<spec>`.

## Navigation events

```java
PeakClient.onMenuNavigation(event -> {
    // current menu, history and the reason for every change
});
```

The event has `player()`, `menuId()` (null when all menus are closed), `history()` and `reason()`. The reason tells why
the menu changed:

| Reason | Meaning |
| --- | --- |
| `open`, `back`, `close` | a menu was opened, the player went back, or all menus were closed |
| `action` | an action of a menu did it |
| `escape` | the player pressed Esc |
| `server` | your server code did it |
| `remove` | the menu was removed |
| `screen` | another screen replaced the menu |
| `timeout` | a menu was waiting for an opening that never came |

Ask `PeakClient.currentMenu(uuid)` and `menuHistory(uuid)` for the current state.
