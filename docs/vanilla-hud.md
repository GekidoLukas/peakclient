# Vanilla HUD

PeakClient can hide or move parts of the vanilla HUD, for example to replace the health bar with your own or to clear
the screen for a cutscene.

```java
PeakClient.to(player).vanillaHud().hide(PeakVanillaElement.HOTBAR, PeakVanillaElement.EXPERIENCE_BAR);
PeakClient.to(player).vanillaHud().hide(PeakVanillaElement.STATUS_BARS);
PeakClient.to(player).vanillaHud().hideAllExcept(PeakVanillaElement.CHAT);
PeakClient.to(player).vanillaHud().offset(PeakVanillaElement.HOTBAR, 0, -20);
PeakClient.to(player).vanillaHud().show(PeakVanillaElement.HOTBAR);
PeakClient.to(player).vanillaHud().reset();
```

| Call | Purpose |
| --- | --- |
| `hide(...)`, `show(...)` | hide or show elements |
| `hideAll()`, `hideAllExcept(...)` | hide everything, or everything but some |
| `offset(element, x, y)` | move an element in pixels |
| `reset()`, `reset(...)` | back to the vanilla state |

Every call sends one packet and changes only what it names. `hide` keeps an earlier `offset`.

## Elements

| Group | Elements |
| --- | --- |
| Bars | `HEALTH_BAR`, `ARMOR_BAR`, `HUNGER_BAR`, `AIR_BAR`, `VEHICLE_HEALTH`, `EXPERIENCE_BAR`, `JUMP_BAR` |
| Items | `HOTBAR`, `OFFHAND`, `HELD_ITEM_NAME` |
| Overlays | `CROSSHAIR`, `STATUS_EFFECTS`, `BOSS_BAR`, `SCOREBOARD`, `ACTION_BAR`, `TITLE`, `SOUND_SUBTITLES` |
| Screens | `CHAT`, `TAB_LIST`, and four toasts |

Groups you can pass in one call: `PeakVanillaElement.STATUS_BARS`, `TOASTS` and `ALL`.

These cannot be moved, only hidden: `AIR_BAR`, `OFFHAND`, `CHAT`, `TAB_LIST` and the toasts. A move on one of them is ignored
and logged.

## Who owns a change

Choose the owner that matches how long the change should last.

| Owner | How | Ends |
| --- | --- | --- |
| Targets | `PeakClient.to(...).vanillaHud()` | when you reset it. It is replayed after a reconnect |
| Audience | `audience.vanillaHud()` | members who leave, and everyone on `audience.close()`, get the vanilla HUD back |

See [Sending to Players](/concepts/sending) for targets and audiences.
| Document | `document.hideVanilla(...)` | when the document is no longer shown |

A document that hides an element always wins over `show(...)`. Document settings are not part of a view's `replace`. Change
what is hidden through `audience.vanillaHud()` instead.

## Platform note

If the client's loader cannot control an element, the request for it is logged once and ignored.
