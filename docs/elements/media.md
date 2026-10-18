# Images, Items and Players

## Image

Shows a texture.

```java
PeakImage.create("mygame:logo", "mygame:textures/gui/logo.png").size(64, 32);
```

| Option | Purpose |
| --- | --- |
| `objectFit(...)` | `FILL` stretches, `CONTAIN` keeps the ratio and fits inside, `COVER` keeps the ratio and fills |
| `sourceSize(w, h)` | size of the texture file, needed for correct fitting |
| `aspectRatio(r)` | ratio to keep when only one side has a size |

The texture must exist on the client as a resource (mod assets or a resource pack). The server cannot send images.

## Item

Shows an item.

```java
PeakItemElement.fixed(new ItemStack(Items.DIAMOND_SWORD));
PeakItemElement.equipment(EquipmentSlot.MAINHAND);   // what the viewer holds
PeakItemElement.inventorySlot(0);
```

The source is a fixed stack, an equipment slot of the viewer, an inventory slot of the viewer, or the block the viewer is
looking at. The item is scaled to fit the smaller side of the element.

## Block

Shows a block as a small 3D model.

```java
PeakBlockElement.fixed("minecraft:gold_block").yaw(30);
PeakBlockElement.targetedBlock();
```

## Entity

Shows a 3D entity, a mob or a player.

```java
PeakEntityElement.fixed("minecraft:zombie").size(60, 90);
PeakEntityElement.player();            // the viewer
PeakEntityElement.targetedEntity();    // what the viewer looks at
```

| Option | Purpose |
| --- | --- |
| `yaw`, `pitch` | viewing angle |
| `zoom` | size within the element |
| `copyMode(...)` | for players: `FULL`, `TYPE_AND_SKIN`, or `TYPE_AND_SKIN_EQUIPMENT` (also shows worn armor and items) |
| `showNameTag(boolean)` | name above the entity (off by default) |

The entity is scaled to fit the element, so the same entity fills different boxes without distortion.

## Player Icon

A flat view of a player's skin: face, bust, upper body or full body.

```java
PeakPlayerIcon.clientPlayer().view(PeakPlayerIconView.FACE).size(16, 16);
PeakPlayerIcon.of(uuid).view(PeakPlayerIconView.BUST);
PeakPlayerIcon.ofTexture("mygame:textures/skins/npc.png");
```

The source is the viewer (`clientPlayer`), the player under the crosshair (`targetedPlayer`), a player by UUID, or a skin
texture. A player who is not online is looked up by UUID. Until the skin is loaded, nothing is drawn.

Skin textures must be 64×64 resources.

## Chibi Player

A small 3D figure of a player with a big head. Same sources as the player icon.

```java
PeakChibiPlayer.of(uuid).rotationY(PeakChibiRotation.THREE_QUARTER).size(60, 110);
```

`rotationY(...)` or `rotationYDegrees(...)` turns the figure. The figure is fitted into the element, so a larger element
makes a larger figure.
