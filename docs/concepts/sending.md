# Sending to Players

Everything starts at the static class `PeakClient`. There are two ways to reach players.

## Targets: send and forget

`PeakClient.to(...)` addresses players directly.

```java
PeakClient.to(player)            // one player
PeakClient.to(uuid)              // one player by id
PeakClient.to(players)           // a collection of players or ids
PeakClient.to(audience)          // the current members of an audience
```

It returns a `PeakTargets` object that offers every operation once: send and remove documents, update elements, set
values, waypoints, the [vanilla HUD](/vanilla-hud), notifications and [menus](/menus/overview).

Use it for one-off messages. A document sent this way is not sent again when the player reconnects.

## Audiences and views: anything that should stay

An audience is a persistent group of players. A view is one document shown to that group.

```java
PeakAudience audience = PeakClient.audience(players);
PeakView view = audience.open(hud).persistent(true);

view.update("mygame:score", PeakPatch.label().text("Red 1 : 0 Blue"));

audience.close();   // at the end of the game
```

An audience handles joins, leaves and reconnects for you. New members get the current state of the view. With
`persistent(true)` a returning member gets the document again after a reconnect.

Other ways to create an audience:

| Call | Members |
| --- | --- |
| `PeakClient.audience(players)` | the players you pass |
| `PeakClient.allPlayers()` | everyone online |
| `PeakClient.team("red")` | players of a scoreboard team |
| `PeakClient.dimension(level)` | players in a dimension |
| `PeakClient.dynamic(supplier)` | whoever your supplier returns |

Only players who are online and have the PeakClient mod count as members.

## Which one?

| You want to | Use |
| --- | --- |
| Show a notification or a short message | Targets |
| Send a static HUD once | Targets |
| Run a game with a live scoreboard | Audience and view |
| Keep values, timers or waypoints in sync with a group | Audience |

Close the audience when the game ends. This also removes its views and stops its tasks. For timing and reconnect details,
see [Threads and Timing](/concepts/threads-and-timing). To change a view, see [Updating Elements](/concepts/updates).
