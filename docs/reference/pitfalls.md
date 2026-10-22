# Pitfalls

Common mistakes and how to avoid them. If something does not show up at all, see [Troubleshooting](/reference/troubleshooting).

## Ids and references

Give elements and documents your own ids with a namespace. Without an id a random one is generated, and you cannot
address the element from a later update. Keep the element object or a typed reference. See
[Naming and IDs](/reference/naming-and-ids) and [Updating Elements](/concepts/updates).

## Colors

Every color field takes an `int` or a `PeakPaint`. For `int`, `0xRRGGBB` is opaque and `0` is transparent. A color with an
alpha byte keeps its alpha. See [Colors and Gradients](/styling/colors-and-gradients).

## Percent sizes

`widthPercent(50)` is 50 percent of the content box of the parent, without padding. For absolutely positioned children it
is the whole box. In a container without a fixed size, a percent size counts as automatic.

## Borders

A border takes no space in the layout. Use a padding at least as large as the border. The background lies under the border,
so a half transparent border over a dark panel looks darker. See [Box Style](/styling/box-style).

## Style updates

`PeakPatch.x().style(PeakStyle)` replaces the whole style. Use the single field methods (`background`, `padding`,
`border`, `borderRadius`, `boxShadow`, `width`, `height`) to change one thing.

## Rotation

`rotationDegrees` only rotates the drawing. Layout and clipping still use the unrotated box.

## Bound fields

Patching a field that is bound to a value releases the binding, so the value no longer follows the placeholder. If the
binding should stay, send it again in the same patch (`bind`).

## Large rankings

More than a few dozen entries belong in `PeakLeaderboardWindow`, not in a `PeakSortedList`. See
[Animation and Displays](/elements/animation-and-displays).

## Reconnects

A document sent with `PeakClient.to(...)` is not sent again after a reconnect. Use an audience with a persistent view. See
[Sending to Players](/concepts/sending) and [Threads and Timing](/concepts/threads-and-timing).
Values, waypoints and the vanilla HUD settings are replayed, for up to 30 minutes.

## Tasks

`PeakWaypointTracker.create`, `PeakTimerSync.create` and `PeakCustomValueWatcher.watch` return tasks that are already
running. Stop them with `stop()`. A task started with `PeakClient.to(...)` ends when its last target leaves and does not
return after a reconnect. Tasks started through an audience follow its members and end when the audience closes.

## Document size

Documents are compressed and checked against 1 MiB. A warning appears at 80 percent. Above the limit the document is not
sent. Use views, patches and values instead of resending large documents.

## Textures and resources

The server cannot send images. Textures, skins and panoramas must exist on the client as mod assets or in a resource pack.
Skin textures must be 64×64.

## Thread

Use the API from the server thread when you can. Calls from other threads are queued. Reading a document from another thread
is not safe.
