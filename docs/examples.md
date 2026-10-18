# Examples

Three complete examples that cover most uses. Replace `mygame` with your own namespace.

## A static HUD

A box in the corner that you send once. Use it for information that does not change.

```java
PeakDocument hud = PeakDocument.create("mygame:hud");
hud.add(PeakPanel.create("mygame:panel")
        .absolute(PeakAnchor.TOP_RIGHT, -10, 10)
        .padding(6).background(PeakPaint.BLACK.withAlpha(0.6f)).border(1, PeakPaint.GRAY)
        .add(PeakLabel.create("mygame:title", "§6Capture the Flag"),
             PeakLabel.create("mygame:rules", "First to 3 captures wins")));

PeakClient.to(players).sendUi(hud);
// later
PeakClient.to(players).removeUi("mygame:hud");
```

Not for: anything that changes often or has to survive a reconnect. Use the next example.

## A live scoreboard

A HUD for a running game. The score text is updated by a patch. The kills of each player come as a value, so the document
does not change.

```java
static final PeakValueKey<Integer> KILLS = PeakValueKey.ofInt("mygame:kills");

PeakDocument hud = PeakDocument.create("mygame:hud");
hud.add(PeakPanel.create("mygame:panel")
        .absolute(PeakAnchor.TOP_RIGHT, -10, 10)
        .padding(6).background(PeakPaint.BLACK.withAlpha(0.6f))
        .column().gap(2)
        .add(PeakLabel.create("mygame:score", "Red 0 : 0 Blue"),
             PeakLabel.create("mygame:kills-label", "Kills: " + KILLS.placeholder("0"))));

PeakAudience audience = PeakClient.audience(players);
PeakView view = audience.open(hud).persistent(true);
audience.values().bindPerPlayer(KILLS, uuid -> stats.kills(uuid), 10);

view.update("mygame:score", PeakPatch.label().text("Red " + red + " : " + blue + " Blue"));

audience.close();
```

- Joins, leaves and reconnects are handled by the audience. `persistent(true)` sends the document again to returning
  players.
- Changes within one tick are sent as one packet.
- Close the audience when the game ends. This removes the HUD and stops the value updates.

See [Sending to Players](/concepts/sending), [Updating Elements](/concepts/updates) and
[Values and Placeholders](/concepts/values-and-placeholders).

## A notification and a menu

A short message to everyone, and a menu where a click reaches your server.

```java
PeakClient.to(players).notify("Round starts in 10 seconds");

PeakDocument shop = PeakMenu.create("mygame:shop", settings -> settings.title("Shop"));
shop.add(PeakPanel.create().absolute(PeakAnchor.CENTER, 0, 0).column().gap(6).padding(10).width(200)
        .add(PeakLabel.create("mygame:shop/title", "Shop"),
             PeakButton.text("mygame:shop/buy", "Buy sword")
                     .onClick(PeakAction.send("buy").arg("item", "sword")),
             PeakButton.text("mygame:shop/close", "Close").onClick(PeakAction.close())));

PeakClient.menu(SHOP).on("buy", event -> {
    if(event.player() instanceof ServerPlayer serverPlayer) {
        serverPlayer.addItem(Items.DIAMOND_SWORD.getDefaultInstance());
    }
    PeakClient.to(event.player()).menus().back();
});

PeakClient.to(player).menus().sendAndOpen(shop);
```

- Register the handler once, when your mod starts.
- The server learns about a click only through `PeakAction.send(...)`. Everything else stays on the client.
- Treat arguments as untrusted input.

See [Menus](/menus/overview) and [Server Events](/menus/server-events).
