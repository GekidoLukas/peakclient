# Threads and Timing

## Server thread

Use the API from the server thread when you can. Calls from other threads, such as database callbacks or async tasks, are
queued onto the server thread in order and take effect at the next tick.

- Patches and operations are copied when you call, so changing them afterwards has no effect.
- Reading a document or its elements from another thread is not safe.
- Use the API only after the server has started. `PeakClient.isAvailable()` tells you if it is ready.

## One packet per tick

Changes you make to a view during one tick are collected and sent as a single packet. You do not need to batch updates
yourself.

## Players without the mod

Players who are offline or do not have the PeakClient mod are skipped silently. A player without the mod is reported once
in the log. To see who will actually receive something:

```java
PeakClient.to(players).reachable();   // online players with the mod
PeakClient.to(players).count();
```

## Reconnecting

After a disconnect the server keeps the state of a player for 30 minutes and replays it when the player comes back:
values, waypoints, the vanilla HUD settings and the custom world border.

Documents are not replayed by themselves. Use `persistent(true)` on a view to send its document again. Call
`PeakClient.forgetPlayer(uuid)` to drop a player's state at once.

## Document size

A document is compressed and checked against 1 MiB. A warning appears at 80 percent. Above the limit it is not sent.
Prefer views, patches and values over resending large documents.

## Tasks

Helpers such as the waypoint tracker, the timer sync and the value watchers run as tasks. They start immediately, stop
with `stop()`, and end with the server. A task that fails three times in a row is stopped and logged. Tasks started
through an audience follow its members and end when the audience closes.
