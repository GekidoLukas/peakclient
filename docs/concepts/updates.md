# Updating Elements

Do not resend a whole document to change a number. Send a small update instead.

## Patches

A patch names only the fields you want to change. Everything else keeps its value.

```java
PeakPatch.label().text("Red 2 : 0 Blue");
PeakPatch.panel().background(PeakPaint.RED.withAlpha(0.5f));
```

There is one patch type per element type, created through `PeakPatch`.

::: tip
`style(PeakStyle)` replaces the whole style. To change one thing, use the single field methods such as `background`,
`padding`, `border` or `borderRadius`.
:::

## Operations on a view

```java
view.update("mygame:score", PeakPatch.label().text("Red 3 : 0 Blue"));
view.updateByClass("score-row", PeakPatch.panel().background(0x3366CC));
view.add("mygame:panel", PeakLabel.create("mygame:row-1", "Alice: 3 kills"));
view.delete("mygame:row-1");
view.replace(buildHud(state));   // sends only the difference
```

All changes made within one server tick are sent together as one packet.

`replace` is the easiest way to keep a view in sync with your game state: rebuild the document and let PeakClient work
out what changed.

## Without a view

The same operations exist on targets. They take the document id.

```java
PeakClient.to(players).updateElement("mygame:hud", "mygame:score", PeakPatch.label().text("..."));
PeakClient.to(players).addElement("mygame:hud", "mygame:feed", entry);
PeakClient.to(players).deleteElement("mygame:hud", "mygame:old");
```

## Typed references

A typed reference ties an id to an element type, so the compiler gives you the matching patch.

```java
static final PeakRef<PeakLabel, PeakLabelPatch> SCORE = PeakRef.label("mygame:score");

hud.add(SCORE.create(id -> PeakLabel.create(id, "0 : 0")));
view.update(SCORE, patch -> patch.text("1 : 0"));
```

A reference to an element of another type is ignored with a warning.

## Good to know

- An update for an unknown id or field is ignored and logged once.
- A field that is bound to a value (see [Values and Placeholders](/concepts/values-and-placeholders)) is overwritten by
  the binding. Call `unbind` first if you patch it.
