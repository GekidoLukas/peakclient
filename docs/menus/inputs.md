# Input Elements

Input elements let the player enter or choose something. Their values live in the stored menu, so typed text stays when the
menu is closed and reopened. A server patch replaces what the player entered.

To read the values on the server, collect them in an action. See [Server Events](/menus/server-events).

## Text Box

A single-line text field.

```java
PeakTextBox.create("mygame:name").placeholder("Your name").maxLength(16).filter(PeakTextFilter.ALPHANUMERIC);
```

`maxLength` is at most 256. Filters: `ANY`, `INTEGER`, `DECIMAL`, `ALPHANUMERIC`. `onSubmit` fires on Enter, `onChange` on
every edit (throttled, see below). Double click selects a word, triple click the whole value.

## Rich Text Box

A multi-line text field with formatting.

```java
PeakRichTextBox.create("mygame:notes").size(160, 64).maxLines(5);
```

- Enter adds a line. Ctrl+Enter fires `onSubmit`.
- Ctrl+B, Ctrl+I, Ctrl+U and Ctrl+D toggle bold, italic, underline and strikethrough. Ctrl+K cycles colors and Ctrl+0
  clears the formatting.
- The value is a normal string with format codes (`§l`, `§o`, `§6` and so on), so a label can show it directly.
- Free colors outside the 16 Minecraft colors are stored as `§x§R§R§G§G§B§B`. The box draws them, a label does not.
- A selection can be dragged to another box. Ctrl copies instead of moving.

`maxLength` defaults to 512 (at most 2000) and counts the codes too. `formatting(false)` turns the shortcuts off.

## Checkbox

```java
PeakCheckbox.create("mygame:friendly-fire").text("Friendly fire").onChange(PeakAction.send("rule"));
checkbox.asSwitch();    // toggle switch look
```

## Radio Group

A single choice from a few options.

```java
PeakRadioGroup.create("mygame:size")
        .option("s", "Small").option("m", "Medium").option("l", "Large", false)
        .selected("m").onChange(PeakAction.send("size"));
```

An option can be disabled by passing `false`. With the focus, the arrow keys move and select.

## Dropdown

A single choice from a longer list that opens as an overlay.

```java
PeakDropdown.create("mygame:country").placeholder("Choose")
        .option("de", "Germany").option("fr", "France")
        .maxVisible(6).onChange(PeakAction.send("country"));
```

The list opens below the field or above it when there is no room. It scrolls if there are more than `maxVisible` rows.

## Number Field

A number with minus and plus buttons.

```java
PeakNumberField.create("mygame:amount", 1, 64, 16).step(1).decimals(0);
```

Arguments are minimum, maximum and start value. A typed value is applied with Enter or when the focus leaves. It is
clamped to the range and to the step. The mouse wheel steps the value while the pointer is over the field, Shift makes
it ten times larger. `showButtons(false)` hides the buttons.

## Slider

```java
PeakSlider.create("mygame:volume", 0, 100, 50).step(5).showValue(true).onChange(PeakAction.send("volume"));
```

Arguments are minimum, maximum and start value. `onChange` fires when the slider is released or changed with the keyboard.

## Color Picker

Choose a color from a square, a hue bar and an optional alpha bar.

```java
PeakColorPicker.create("mygame:color", 0xFF8800).alpha(false).target("mygame:notes").hexField("mygame:hex");
```

- `target` colors the selection of a rich text box live.
- `hexField` keeps a text box in sync with the color.
- Swatches are normal elements: `button.onClick(PeakAction.setColor("mygame:color", 0x55FF55))`.

The value is `#RRGGBB` (`#AARRGGBB` with alpha).

## Item Slot

An inventory-style slot for shops and kit menus.

```java
PeakItemSlot.fixed("mygame:slot", new ItemStack(Items.DIAMOND_SWORD))
        .onClick(PeakAction.send("pick")).onRightClick(PeakAction.send("info"));
```

It shows the item with its count and durability bar, the vanilla item tooltip on hover, and a gold border while
`selected(true)`. Patch `selected` from the server to build a selection. The source is a fixed stack, an equipment slot or
an inventory slot of the player. Fixed stacks keep their name, damage, enchantments and lore.

## Invalid fields and change events

- `invalid(true)` draws the border of an input in the invalid color (red by default). `required(true)` marks a field as
  mandatory in a form.
- `onChange` on text boxes is throttled by `changeDelayTicks` (default 5). Enter, losing the focus and closing the menu
  send the latest value at once.
