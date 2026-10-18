# Events

An element can react to things that happen to it. A reaction is a trigger plus a list of actions. The client runs it on
its own, so the server only sends data and the interface responds.

```java
PeakAnimated scoreBox = PeakAnimated.create("mygame:score-box").borderRadius(8)
        .add(PeakLabel.create("Score: {mygame:score}"))
        .onIncrease("{mygame:score}", PeakAction.trigger("mygame:score-box", PeakAnimationEffect.FLASH, 0xFF55FF55))
        .onDecrease("{mygame:score}", PeakAction.trigger("mygame:score-box", PeakAnimationEffect.FLASH, 0xFFFF5555));

PeakClient.to(players).setValue("mygame:score", 12);
```

Nothing is sent back to the server unless an action is `PeakAction.send(...)`.

## Triggers

| Trigger | Builder | Fires when |
| --- | --- | --- |
| Click | `onClick`, `onRightClick`, `onDoubleClick` | the element is clicked (menus) |
| Hover | `onHoverEnter`, `onHoverLeave` | the mouse enters or leaves (menus) |
| Focus | `onFocus`, `onBlur` | keyboard focus moves onto or off the element (menus) |
| Input | `onChange`, `onSubmit` | an input is edited, or a text box is confirmed with Enter (menus) |
| Value | `onValueChange(watch, ...)` | the watched value is different from before |
| Increase, decrease | `onIncrease(watch, ...)`, `onDecrease(watch, ...)` | a watched number goes up or down |
| Threshold | `onAbove(watch, limit, ...)`, `onBelow(watch, limit, ...)` | a watched number crosses the limit |
| Appear | `onAppear` | the element is shown for the first time |
| Show, hide | `onShow`, `onHide` | the `hidden` flag changes |
| Timer end | `onTimerEnd` | a timer reaches its target |
| Time | `after(ticks, ...)`, `every(ticks, ...)` | once after a delay, or repeatedly |

Click, hover, focus and input triggers need a [menu](/menus/overview). All other triggers also work in HUD documents.

## Watching values

`watch` is a text with placeholders, for example `{mygame:score}` or `{mc:player_health}` (see
[Values and Placeholders](/concepts/values-and-placeholders) and [Built-in Placeholders](/reference/built-in-placeholders)). Without one, a reaction watches
the element itself: the text of a label, the value of a progress bar, the remaining time of a timer or the value of an
input.

- The first value is only remembered, so nothing fires when the document appears.
- If a placeholder cannot be resolved yet, nothing happens.
- Numbers are read from the start of the text (`12/20` counts as 12).

## More control

```java
element.on(PeakReaction.of(PeakEvent.VALUE_BELOW).watch("{mygame:health}").threshold(6)
        .cooldown(40)
        .once()
        .then(PeakAction.sound("minecraft:entity.experience_orb.pickup"),
              PeakAction.trigger("mygame:health-box", PeakAnimationEffect.OUTLINE, 0xFFFF5555)));
```

- `cooldown(ticks)` limits how often a reaction fires.
- `once()` fires only the first time.
- `fireOnStart()` also fires for the first value.

Value triggers wait at least two ticks between firings.

## Actions

| Action | Purpose |
| --- | --- |
| `PeakAction.patch(id, patch)` | change an element |
| `PeakAction.toggle(id)` | show or hide an element |
| `PeakAction.trigger(id, effect, ...)` | play a highlight effect on an animated element |
| `PeakAction.sound(id)` | play a sound |
| `PeakAction.open(menuId)`, `close()`, `back()` | menu navigation (see [Server Events](/menus/server-events)) |
| `PeakAction.copyText(text)` | copy text to the clipboard |
| `PeakAction.send(actionId)` | report to the server |

In HUD documents, `patch`, `toggle`, `trigger`, `sound`, `open`, `close`, `copyText` and `send` work. Actions that belong
to menu inputs do not.

## Reporting to the server

`PeakAction.send(actionId)` is the only way the client talks to the server. In a HUD document the server receives it as a
document action.

```java
doc.add(PeakTimer.countDownFrom("mygame:round-timer", 1200).onTimerEnd(PeakAction.send("round-over")));

PeakClient.document("mygame:hud").on("round-over", event -> startNextRound(event.player()));
PeakClient.onDocumentAction(event -> log(event.documentId() + "/" + event.actionId()));
```

The event has `player()`, `documentId()`, `elementId()`, `actionId()` and the arguments. Value triggers add `old`, `new`
and `delta`.

::: warning
The arguments come from the client. Do not trust them. The server accepts an event only for a document it sent to that
player, and allows 30 events per second per player.
:::

## Highlight shortcuts

[`PeakAnimated`](/elements/animation-and-displays) builds the most common reactions for you:

```java
PeakAnimated.create("mygame:score-box").add(PeakLabel.create("Score: {mygame:score}"))
        .highlightOnTrend("{mygame:score}");
```

`highlightOnIncrease`, `highlightOnDecrease` and `highlightOnChange` set up single directions.

## Changing reactions later

Reactions are part of the element. An update replaces the whole list: `PeakPatch.panel().events(...)`,
`onClick(...)` or `clearEvents()`. There are no patches for single reactions.
