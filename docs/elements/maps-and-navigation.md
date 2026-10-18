# Maps and Navigation

## Waypoints

A waypoint is a marked place in the world. Waypoints are not elements. You send them to the players
(see [Sending to Players](/concepts/sending)), and compasses, minimaps and world anchors can show them.

```java
PeakWaypoint flag = PeakWaypoint.of(120.5, 70, -40.5, "Red flag").color(PeakPaint.RED);
PeakClient.to(players).upsertWaypoint(flag);
PeakClient.to(players).removeWaypoint(flag.getId());
```

| Option | Purpose |
| --- | --- |
| `color(...)`, `icon(texture)`, `iconScale(f)` | look |
| `label(...)`, `labelOnMap(boolean)` | name next to the marker |
| `expireAfterTicks(n)` | removes the waypoint after some time (0 = never) |
| `visibleIn(elementIds...)` | only show it in these compasses or minimaps (default: everywhere) |

To follow a moving entity, use `PeakWaypointTracker.create(waypointId, entity, targets)`. It keeps the position current and
stops when the entity is gone. Send the waypoint to the same players first.

A waypoint can also show the name or face of an entity instead of a plain label:

```java
PeakWaypoint carrier = PeakWaypoint.trackingEntity(player.getUUID().toString(),
        PeakEntityTrackingMode.PLAYER_FACE, player.getX(), player.getY(), player.getZ());
```

Modes: `LIVING_NAME` (name of a mob), `PLAYER_NAME` and `PLAYER_FACE` (head of a player). The position is kept current by the
tracker above.

## Compass

A horizontal bar like a boss bar that shows directions and waypoints in front of the viewer.

```java
PeakCompass.create("mygame:compass")
        .size(200, 14)
        .fieldOfViewDegrees(120)
        .maxDistance(300)
        .overlapMode(PeakCompassOverlapMode.STACK);
```

| Option | Purpose |
| --- | --- |
| `fieldOfViewDegrees(d)` | how much of the horizon is visible |
| `maxDistance(blocks)` | waypoints farther away are hidden |
| `showCardinalLabels`, `showIntercardinalLabels`, `showTickMarks` | the scale |
| `overlapMode(...)` | what happens when waypoints overlap: `NONE` or `STACK` (stacked above or below) |
| `groupSameNameWaypoints(boolean)` | merge waypoints with the same name that are close together |
| `iconScaleRange(min, max)`, `iconScaleDistanceRange(near, far)` | icons grow when waypoints are near |
| `tickPosition(...)`, `tickIntervalDegrees(d)` | scale at the `TOP` or `BOTTOM` and the distance between ticks |
| `stackDirection(...)`, `stackSpacing(px)`, `maxStackedWaypoints(n)` | how stacked waypoints are placed (`UP` or `DOWN`) |
| `backgroundColor`, `tickColor` | colors |

## Minimap

A map image with the viewer's position, waypoints and optionally the world border.

```java
PeakMinimap.create("mygame:textures/map/arena.png")
        .imageSize(512, 512)
        .worldBounds(-256, -256, 256, 256)
        .viewRadiusBlocks(64)
        .size(100, 100);
```

The texture is a picture of your map, placed in the world by `worldBounds`. The minimap shows the part around the viewer,
`viewRadiusBlocks` wide.

| Option | Purpose |
| --- | --- |
| `playerIconMode(...)` | marker for the viewer: `NONE`, `TEXTURE` or `PLAYER_HEAD` |
| `worldBorderSource(...)` | draw the vanilla border (`VANILLA`), your own (`CUSTOM`) or none |
| `borderColor`, `borderTexture` | frame |
| `outOfImageColor`, `outOfBorderColor` | color outside the image or outside the border |
| `edgeIndicatorMaxDistance(blocks)` | waypoints outside the view are shown at the edge up to this distance |
| `edgeIconScaleRange(min, max)`, `waypointLabelScale(f)` | size of edge icons and waypoint labels |
| `worldBorderShape(...)`, `worldBorderColor`, `worldBorderThickness` | look of the border line: `SQUARE` or `CIRCLE` |
| `playerIconTexture(path)`, `playerIconSize(px)` | your own marker for the viewer |

The custom world border is set on the server with `setCustomWorldBorder(...)`, `setNextCustomWorldBorder(...)` for a
shrinking border, and `clearCustomWorldBorder()`.

## World Anchor

Hangs its children on a point in the world and draws them at the matching place on the screen. Use it for name tags,
objective markers, damage numbers or an arrow that points at a place.

```java
PeakWorldAnchor marker = PeakWorldAnchor.at("mygame:objective", 120.5, 70, -40.5)
        .pivot(PeakAnchor.BOTTOM_CENTER)
        .maxDistance(80).fadeDistance(15)
        .clampToEdge(12)
        .add(PeakLabel.create("Capture the flag"));
hud.add(marker);

PeakWorldAnchor name = PeakWorldAnchor.entity(player.getUUID()).add(PeakLabel.create("Alex"));
PeakWorldAnchor flagMarker = PeakWorldAnchor.waypoint(flag.getId());
```

| Option | Purpose |
| --- | --- |
| target | a point (`at`), an entity (`entity`, followed by the client) or a waypoint (`waypoint`) |
| `pivot(...)`, `screenOffset(x, y)` | which part of the content sits on the point, and a pixel shift |
| `distanceRange`, `maxDistance`, `fadeDistance` | show only within a range and fade out at the end |
| `scaleWithDistance(reference, min, max)` | smaller when far, larger when near |
| `clampToEdge(margin)` | when off screen, stay at the screen edge with an arrow |
| `noOcclusion()`, `occlusion(...)` | what hides the anchor |

By default blocks and entities cover the anchor pixel by pixel: where a wall is nearer than the point, that part of the
label is hidden. `noOcclusion()` makes it always visible. Move an anchor with
`PeakPatch.worldAnchor().moveTo(x, y, z)`.

### Target options

```java
PeakWorldAnchor.entity(uuid).target(t -> t.point(PeakAnchorTarget.EntityPoint.ABOVE_HEAD).above(0.5));
```

| Call | Purpose |
| --- | --- |
| `point(...)` | for entities: `ABOVE_HEAD`, `CENTER` or `FEET` |
| `above(blocks)` | lifts the point |
| `offset(x, y, z)` | shifts the point in the world |

An entity that is not loaded, a waypoint that does not exist and a different `dimension(...)` hide the anchor.

### Occlusion options

```java
PeakWorldAnchor.entity(uuid).occlusion(PeakOcclusion.blocksAndEntities().dimTo(0.35f));
```

| Option | Values |
| --- | --- |
| mode | `PARTIAL` (default, per pixel), `BLOCKS`, `BLOCKS_AND_ENTITIES` (whole label by line of sight), `NONE` |
| `hide()`, `dim()`, `dimTo(f)` | hide the covered part, or dim it |
| `blockShape(...)` | `VISUAL` (glass does not cover) or `COLLIDER` |
| `entities(...)` | which entities cover: `PLAYERS`, `LIVING`, `ALL` (line modes only) |
| `ignoreTarget(boolean)` | the target entity and its own block never cover the anchor |
| `depthBias(blocks)` | `PARTIAL` only: tests the label this far nearer to the camera |
| `fadeTicks(n)` | time to fade when the cover changes |

`PARTIAL` needs one frame to start and falls back to the line test on some setups, such as certain shader packs.

An anchor is always placed absolutely and does not change the layout of its parent. It is not supported in menus.
