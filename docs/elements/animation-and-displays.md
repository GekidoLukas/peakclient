# Animation and Displays

Containers that animate, rotate or rank their children. Every child can be any element: labels, panels, items, players.
Children keep their ids, bindings and patches.

## Animated

Wraps its children and applies an effect to them as one group. Use it for pulsing timers, shaking health bars or
highlighting a score that just changed.

```java
PeakAnimated timer = PeakAnimated.of(PeakAnimationEffect.PULSE)
        .periodSeconds(1.2)
        .add(PeakTimer.countDownFrom(600));
```

| Effect | What it does |
| --- | --- |
| `PULSE`, `BOB`, `SWAY`, `WOBBLE`, `SPIN`, `BLINK`, `SHAKE` | movement, scale or fade in a loop |
| `POP` | grows a little and settles |
| `FLASH` | tints the content and fades back |
| `GLOW` | soft glow around the content |
| `SHINE` | a light streak runs across |
| `OUTLINE` | a frame flashes around the content |

`amplitude(...)` sets the strength, `delayTicks(...)` delays the start, `playing(false)` freezes the loop, `color(...)`
sets the color of the highlight effects. Highlights follow the shape of the content.

One-shot effects for feedback are sent as a patch and play on top of the loop:

```java
PeakClient.to(players).updateElement("mygame:hud", "mygame:score-box", PeakPatch.animated().flash(0xFF55FF55));
PeakClient.to(players).updateElement("mygame:hud", "mygame:health", PeakPatch.animated().shake());
```

`highlightOnIncrease(...)`, `highlightOnDecrease(...)` and `highlightOnChange(...)` set up a highlight that fires by
itself when a watched value changes. See [Events](/interaction/events).

## Stagger

Reveals its children one after the other. Use it for result screens and countdown reveals. The layout is final from the
start, only the appearance is delayed.

```java
PeakStagger.create("mygame:results").column().gap(4)
        .animations(PeakPopupAnimation.SLIDE_FROM_LEFT, PeakPopupAnimation.FADE)
        .delayTicks(4)
        .add(row1, row2, row3);
```

Options: `startDelayTicks`, `durations(enter, exit)`, `order(RANDOM)`, `reverse(true)`, `autoPlay(false)`. Patches
`replay()` run the reveal again and `hide()` makes the children leave one after the other.

## Carousel

Moves its children past the box like a ticker. Use it for sponsors, tips or a list that is longer than the box.

```java
PeakCarousel.create("mygame:sponsors").size(220, 40)
        .movement(PeakCarouselDirection.LEFT).speed(40f).gap(8).fade(24)
        .add(logoA, logoB, logoC);
```

| Option | Purpose |
| --- | --- |
| `movement(...)` | `LEFT`, `RIGHT`, `UP`, `DOWN` |
| `speed(px/s)` | speed |
| `order(RANDOM)` | shuffled order |
| `fade(px)` | soft edges |
| `pingPong()` | travels to the end, waits and travels back instead of looping |
| `onlyWhenOverflowing(true)` | stands still while everything fits |

A carousel needs a fixed size along the movement. Without one it grows to show all children at once.

## Cycler

Shows one child at a time and switches after an interval, like an advertising display.

```java
PeakCycler.create("mygame:news").size(160, 60)
        .intervalSeconds(4)
        .animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.SLIDE_FROM_LEFT)
        .add(slideA, slideB, slideC);
```

Options: `order(RANDOM)` (every child once per round), `transition(OVERLAP)` (enter and exit at the same time). The box has
the size of the biggest child. Control it with patches: `paused(true)`, `next()`, `showIndex(n)`.

## Reel

A slot machine or roulette. The server picks the winner, the client plays the spin and stops exactly on it.

```java
PeakReel reel = PeakReel.create("mygame:slot").size(100, 90)
        .spinSeconds(4).minLoops(2)
        .add(card("Red"), card("Blue"), card("Gold"));

PeakClient.to(players).updateElement("mygame:hud", "mygame:slot", PeakPatch.reel().spinTo(2));
```

`spinTo` accepts an index, a child id or an element. A new element is inserted at a random place and the reel lands on
it. `spinOnStart(...)` spins as soon as the reel is shown. The client does not report back when the spin ends. Start your
own timer with the same duration if the server needs to react.

## Feed

A stack of short-lived entries, for kill feeds and join messages. The server adds entries. Each entry enters, stays,
leaves and is removed by the client.

```java
PeakFeed feed = PeakFeed.create("mygame:killfeed").size(240, 150)
        .entryLifetimeSeconds(5).maxEntries(6)
        .animations(PeakPopupAnimation.SLIDE_FROM_RIGHT, PeakPopupAnimation.FADE);

PeakClient.to(players).addElement("mygame:hud", "mygame:killfeed", entry);
```

`newestFirst(true)` puts new entries on top, `mainAxisAlign(END)` anchors the stack at the bottom. Entries are plain
elements. A view that replaces the whole document loses entries that the server never knew about.

## Sorted List

Orders its children by `sortKey` and animates every rank change. Use it for small leaderboards and standings.

```java
PeakSortedList board = PeakSortedList.create("mygame:board")
        .descending().maxVisible(5).moveTicks(10)
        .exitAnimation(PeakPopupAnimation.FADE);
board.add(PeakPanel.create("mygame:board/alex").sortKey(40).add(...));

PeakClient.to(players).updateElement("mygame:hud", "mygame:board/alex", PeakPatch.panel().sortKey(55));
```

`sortKey` can also be bound to a value, so a scoreboard driven by values needs no patches. A sorted list sends and sorts
every child, so it suits a few dozen entries at most.

## Leaderboard Window

For large rankings. The server keeps the whole ranking and sends only the top rows. Rows still glide when ranks change.

```java
PeakLeaderboardWindow<UUID> board = PeakLeaderboardWindow.<UUID>create("mygame:board", 10)
        .ownRow(uuid -> uuid)
        .scoreFormat(score -> (int) score + " pts");

PeakView view = audience.open(PeakDocument.create("mygame:hud").add(board.element()));
board.bind(view);

board.set(player.getUUID(), player.getName().getString(), 120);
board.add(player.getUUID(), 5);
board.remove(player.getUUID());
```

| Option | Purpose |
| --- | --- |
| `ascending()` | lower scores win, for times |
| `intervalTicks(n)` | at most one packet per interval |
| `ownRow(...)` | shows the viewer's own rank below the list |
| `neighbours(n)` | adds the ranks above and below the viewer |
| `row(...)`, `rowStyle(...)`, `columns(...)` | own row layout or look |

The window is the same for everyone. Only the own-rank block differs per player. Equal scores keep the order of whoever
reached the score first.
