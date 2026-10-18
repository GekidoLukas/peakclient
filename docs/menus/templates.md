# Templates

Templates are ready-made building blocks for common menus. They only combine existing [elements](/elements/overview) and
[actions](/menus/server-events), so you can change their look like anything else. Switching pages or tabs happens on the client without a round trip to the server.

## Confirm Dialog

A small menu that asks for a yes or no.

```java
PeakConfirmDialog.create("mygame:delete")
        .title("Delete the arena?").message("This cannot be undone.")
        .confirmText("Delete").cancelText("Keep")
        .arg("arena", "castle")
        .onConfirm(event -> deleteArena(event.arg("arena")))
        .show(PeakClient.to(player));
```

It opens over the current menu and goes back after the answer (`backAfterAnswer(false)` keeps it open). The dialog does not
know who asked, so put that into `arg(...)`.

## Paged List

Splits many items over pages with Previous and Next buttons.

```java
PeakPagedList list = PeakPagedList.create("mygame:players", 8).width(180);
for (String name : names) {
    list.add(PeakButton.text("mygame:player/" + name, name).onClick(PeakAction.send("pick").arg("name", name)));
}
menu.add(list.build());
```

All items are part of the menu. For a very long list, prefer a scrolling container, or remote pages.

### Remote pages

Keeps the items on the server and sends a page when the player first visits it.

```java
PeakPagedList list = PeakPagedList.remote("mygame:scores", 5).width(180).loadingSpinner();
PeakRemotePages pages = PeakRemotePages.attach("mygame:menu/scores", list, (player, page) ->
        scoresOf(player, page).stream().map(row -> PeakLabel.create(row.id(), row.text())).toList());

menus.send(menu);
pages.sendFirstPage(player);
```

- The loader runs once per player and page. Revisited pages come from the client.
- The page count is fixed per build. Send the menu again to change it.
- `refresh(player, page)` reloads a page now, `invalidate(...)` and `invalidateAll(...)` reload at the next visit.
- Sending the menu again forgets what was loaded. Call `sendFirstPage` again.
- The loader runs on the server thread, so keep it quick. It works with `menus().send`, not with persistent menu views.

## Tabs

Shows one content at a time, selected by tab buttons.

```java
menu.add(PeakTabs.create("mygame:settings")
        .tab("General", generalPanel).tab("Teams", teamsPanel).selected(0).build());
```

Every content stays alive, so input in a hidden tab is kept. `selectedBackground`, `normalBackground`, `selectedColor` and
`normalColor` set the look.

## Choice List

A column of buttons where one is highlighted. A click sends the action with the argument `value`.

```java
menu.add(PeakChoiceList.create("mygame:difficulty", "difficulty")
        .option("easy", "Easy").option("hard", "Hard").selected("easy").build());
PeakClient.menu("mygame:menu").on("difficulty", event -> setDifficulty(event.arg("value")));
```

The highlight is local. The server decides what a choice means and can patch the buttons.

## Check List

A column of checkboxes. Every change sends the action with the arguments `option` and `value` (`true` or `false`).

```java
PeakCheckList.create("mygame:perks", "perk").option("speed", "Speed", true).option("jump", "Jump", false);
```

## Collapsible

A section with a header that expands and collapses. It works entirely on the client.

```java
PeakCollapsible.create("mygame:advanced", "Advanced").open(false).add(PeakLabel.create("Hidden options"));
```

## Separator

A line, optionally with a label.

```java
PeakSeparator.create("mygame:sep").width(180).label("Section");
```

## Form Field

A label, an input, an optional hint and an error text, for forms.

```java
PeakFormField.create("mygame:name-field", "Name")
        .input(PeakTextBox.create("mygame:name")).required(true).hint("3 to 16 characters");
```

With `PeakAction.send("save").collectAll().validate()` the client checks required fields before sending. See [Server Events](/menus/server-events).

## Spinner

A ring of dots for loading states. It also works in HUD documents.

```java
PeakSpinner.create("mygame:loading").dots(8);
```

## Rich Text Toolbar

A row of formatting buttons (bold, italic, underline, strikethrough, color, clear) for a rich text box.

```java
PeakRichTextToolbar.create("mygame:toolbar", "mygame:notes").colorPicker();
```

`colorPicker()` adds a button that shows and hides a color picker. The buttons keep the focus in the text box and show the
active style at the caret.
