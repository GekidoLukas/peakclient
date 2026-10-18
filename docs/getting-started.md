# Getting Started

PeakClient lets a server mod describe a user interface and have it drawn on the player's screen. Your server builds a
document of labels, panels, images, timers and more. The PeakClient mod on the player's client renders it as a HUD or
as a menu. You never write client code and never touch the network layer.

It is built for events and minigames: scoreboards, killfeeds, voting, team scores, countdowns and waypoints.

## Requirements

- Minecraft 1.21.1
- Fabric or NeoForge
- Java 21
- The PeakClient mod on the server and on every client that should see the interface

## Add the dependency

Add PeakClient to your server mod.

::: warning Not published yet
PeakClient has no public repository yet. The coordinates below are placeholders and will change once it is published.
:::

::: code-group

```groovy [Fabric]
dependencies {
    modImplementation "net.gekidolukas.peakclient:peakclient-fabric:1.0.0"
}
```

```groovy [NeoForge]
dependencies {
    implementation "net.gekidolukas.peakclient:peakclient-neoforge:1.0.0"
}
```

:::

Then declare it as required in your mod metadata. The mod id is `peakclient`.

::: code-group

```json [fabric.mod.json]
{
  "depends": {
    "peakclient": "*"
  }
}
```

```toml [neoforge.mods.toml]
[[dependencies.yourmod]]
modId = "peakclient"
type = "required"
versionRange = "[1.0.0,)"
ordering = "NONE"
side = "BOTH"
```

:::

## Check that it is ready

Everything starts at the static class `PeakClient`. Use it after the server has started.

```java
PeakClient.isAvailable();                   // PeakClient is initialized
PeakClient.hasClient(uuid);                 // this player has the mod
PeakClient.onClientReady(event -> {         // a player with the mod finished joining
    ServerPlayer player = event.player();
});
```

Players without the mod are skipped silently, so you do not have to filter them yourself. Calls from other threads are
queued onto the server thread and take effect at the next tick.

## Your first HUD

```java
PeakDocument hud = PeakDocument.create("mygame:hud");
hud.add(PeakPanel.create("mygame:panel")
        .absolute(PeakAnchor.TOP_RIGHT, -10, 10)
        .padding(6).background(PeakPaint.BLACK.withAlpha(0.6f))
        .add(PeakLabel.create("mygame:title", "Hello from PeakClient")));

PeakClient.to(player).sendUi(hud);
```

`PeakClient.to(...)` accepts one player, a collection of players or an audience. The element ids use the form
`namespace:path/part`, so use your own namespace to avoid clashes with other mods.

## Next steps

- [Documents and Elements](/concepts/documents-and-elements): how an interface is built.
- [Sending to Players](/concepts/sending): targets, audiences and views.
- [Elements Overview](/elements/overview): the building blocks.
- [Menus](/menus/overview): interfaces the player can click and type into.
- [Pitfalls](/reference/pitfalls): common mistakes to avoid.
