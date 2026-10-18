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

PeakClient is published on [Modrinth](https://modrinth.com), which also serves every version as a Maven artifact. No
account or token is needed to download it.

Add the Modrinth repository to your `build.gradle`:

```groovy
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}
```

Then add the dependency for your loader:

::: code-group

```groovy [Fabric]
dependencies {
    modImplementation "maven.modrinth:peakclient:1.0.0-fabric"
}
```

```groovy [NeoForge]
dependencies {
    implementation "maven.modrinth:peakclient:1.0.0-neoforge"
}
```

:::

Versions are named `<version>-<loader>`. Replace `1.0.0` with the version you want to build against.

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

The ready event also carries `protocolVersion()`, the version of the client's mod. Players without the mod are skipped
silently, so you do not have to filter them yourself. Calls from other threads are queued onto the server thread and take
effect at the next tick.

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

- [Examples](/examples): three complete examples to start from.
- [Compatibility](/reference/compatibility): versions, loaders and who needs the mod.
- [Documents and Elements](/concepts/documents-and-elements): how an interface is built.
- [Sending to Players](/concepts/sending): targets, audiences and views.
- [Elements Overview](/elements/overview): the building blocks.
- [Menus](/menus/overview): interfaces the player can click and type into.
- [Pitfalls](/reference/pitfalls): common mistakes to avoid.
- [Troubleshooting](/reference/troubleshooting): what to check when a player sees nothing.
