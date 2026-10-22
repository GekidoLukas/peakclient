package net.gekidolukas.peakclient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.ui.PeakBarChart;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import net.gekidolukas.peakclient.ui.PeakTimer;
import net.gekidolukas.peakclient.ui.PeakTooltipLabel;
import net.gekidolukas.peakclient.ui.PeakWaypoint;
import net.gekidolukas.peakclient.ui.serialize.PeakComponentAdapter;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeakComponentAdapterTest {

    private static Component roundTrip(Component component) {
        return PeakComponentAdapter.fromTree(PeakComponentAdapter.toTree(component));
    }

    @Test
    void aPlainLiteralIsAPlainJsonString() {
        JsonElement tree = PeakComponentAdapter.toTree(Component.literal("§6Hello {mc:player_name}"));
        assertTrue(tree.isJsonPrimitive());
        assertEquals("§6Hello {mc:player_name}", tree.getAsString());
        assertEquals("§6Hello {mc:player_name}", roundTrip(Component.literal("§6Hello {mc:player_name}")).getString());
    }

    @Test
    void stylesAndSiblingsSurviveARoundTrip() {
        Component original = Component.literal("red bold").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                .append(Component.literal(" tail").withStyle(ChatFormatting.GREEN));

        Component back = roundTrip(original);

        assertEquals("red bold tail", back.getString());
        assertEquals(ChatFormatting.RED.getColor(), back.getStyle().getColor().getValue());
        assertTrue(back.getStyle().isBold());
        assertEquals(1, back.getSiblings().size());
        assertEquals(ChatFormatting.GREEN.getColor(), back.getSiblings().get(0).getStyle().getColor().getValue());
        assertFalse(back.getSiblings().get(0).getStyle().isBold());
    }

    @Test
    void aTranslatableKeepsKeyFallbackAndArguments() {
        Component original = Component.translatableWithFallback("my.key", "Fallback %s", "text", 7,
                Component.literal("nested").withStyle(ChatFormatting.YELLOW));

        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, roundTrip(original).getContents());

        assertEquals("my.key", contents.getKey());
        assertEquals("Fallback %s", contents.getFallback());
        assertEquals(3, contents.getArgs().length);
        assertEquals("text", contents.getArgs()[0]);
        assertEquals(7, ((Number) contents.getArgs()[1]).intValue());
        Component nested = assertInstanceOf(Component.class, contents.getArgs()[2]);
        assertEquals("nested", nested.getString());
        assertEquals(ChatFormatting.YELLOW.getColor(), nested.getStyle().getColor().getValue());
    }

    @Test
    void theFontSurvivesARoundTrip() {
        ResourceLocation font = ResourceLocation.parse("minecraft:alt");
        Component back = roundTrip(Component.literal("x").withStyle(Style.EMPTY.withFont(font)));

        assertEquals(font, back.getStyle().getFont());
    }

    @Test
    void anExplicitFalseSurvivesARoundTrip() {
        Component original = Component.literal("a").withStyle(ChatFormatting.BOLD)
                .append(Component.literal("b").withStyle(Style.EMPTY.withBold(false)));

        Component back = roundTrip(original);

        assertTrue(back.getStyle().isBold());
        Style child = back.getSiblings().get(0).getStyle();
        assertFalse(child.isBold());
        assertFalse(child.applyTo(Style.EMPTY.withBold(true)).isBold());
    }

    @Test
    void clickEventsAndInsertionsAreNeverWritten() {
        Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://example.org"))
                .withInsertion("ins");

        JsonElement tree = PeakComponentAdapter.toTree(Component.literal("x").withStyle(style));

        assertTrue(tree.isJsonPrimitive());
        JsonObject object = new JsonObject();
        object.addProperty("text", "y");
        object.addProperty("insertion", "ins");
        object.addProperty("clickEvent", "ignored");
        Component read = PeakComponentAdapter.fromTree(object);
        assertNull(read.getStyle().getClickEvent());
        assertNull(read.getStyle().getInsertion());
    }

    @Test
    void aKeybindIsKept() {
        KeybindContents contents = assertInstanceOf(KeybindContents.class, roundTrip(Component.keybind("key.jump")).getContents());
        assertEquals("key.jump", contents.getName());
    }

    @Test
    void anArrayIsReadAsConcatenation() {
        JsonArray array = new JsonArray();
        array.add("a");
        array.add("b");
        assertEquals("ab", PeakComponentAdapter.fromTree(array).getString());
    }

    @Test
    void anObjectWithHexColorIsRead() {
        JsonObject object = new JsonObject();
        object.addProperty("text", "x");
        object.addProperty("color", "#336699");
        assertEquals(0x336699, PeakComponentAdapter.fromTree(object).getStyle().getColor().getValue());
    }

    @Test
    void nullIsReadAsEmpty() {
        assertEquals("", PeakComponentAdapter.fromTree(null).getString());
    }

    @Test
    void labelsAreSerializedWithThePlainStringForm() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakLabel.create("test:label", "Hello"));

        String json = PeakUiSerializer.toJson(document);
        assertTrue(json.contains("\"text\":\"Hello\""));

        PeakLabel read = (PeakLabel) PeakUiSerializer.documentFromJson(json).findById("test:label");
        assertEquals("Hello", read.getText().getString());
        assertInstanceOf(PlainTextContents.class, read.getText().getContents());
    }

    @Test
    void aTranslatableLabelSurvivesTheWireFormat() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakLabel.create("test:label", Component.translatable("my.key", "1", "3")));
        document.add(PeakTooltipLabel.create("test:tip", Component.translatable("my.tip")));

        PeakDocument read = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));

        TranslatableContents label = assertInstanceOf(TranslatableContents.class,
                ((PeakLabel) read.findById("test:label")).getText().getContents());
        assertEquals("my.key", label.getKey());
        assertArrayEquals(new Object[]{"1", "3"}, label.getArgs());
        TranslatableContents tip = assertInstanceOf(TranslatableContents.class,
                ((PeakTooltipLabel) read.findById("test:tip")).getText().getContents());
        assertEquals("my.tip", tip.getKey());
    }

    @Test
    void theOtherTextFieldsAreComponentsToo() {
        PeakDocument document = PeakDocument.create("test:doc");
        document.add(PeakTimer.countDownFrom("test:timer", 100).prefix(Component.translatable("my.prefix")).suffix(" left"));
        document.add(PeakBarChart.create().addEntry(Component.translatable("my.entry"), 3, 0xFF0000));
        document.add(PeakTextBox.create("test:box").placeholder(Component.translatable("my.placeholder")));
        document.add(PeakCheckbox.create("test:check", Component.translatable("my.check")));

        PeakDocument read = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(document));

        PeakTimer timer = (PeakTimer) read.findById("test:timer");
        assertEquals("my.prefix", assertInstanceOf(TranslatableContents.class, timer.getPrefix().getContents()).getKey());
        assertEquals(" left", timer.getSuffix().getString());
        PeakBarChart chart = (PeakBarChart) read.getChildren().get(1);
        assertEquals("my.entry", assertInstanceOf(TranslatableContents.class, chart.getEntries().get(0).label().getContents()).getKey());
        PeakTextBox box = (PeakTextBox) read.findById("test:box");
        assertEquals("my.placeholder", assertInstanceOf(TranslatableContents.class, box.getPlaceholder().getContents()).getKey());
        PeakCheckbox check = (PeakCheckbox) read.findById("test:check");
        assertEquals("my.check", assertInstanceOf(TranslatableContents.class, check.getText().getContents()).getKey());
    }

    @Test
    void aWaypointLabelIsAComponent() {
        PeakWaypoint waypoint = PeakWaypoint.of(1, 2, 3, Component.translatable("my.waypoint"));

        PeakWaypoint read = PeakUiSerializer.GSON.fromJson(PeakUiSerializer.GSON.toJson(waypoint), PeakWaypoint.class);

        assertEquals("my.waypoint", assertInstanceOf(TranslatableContents.class, read.getLabel().getContents()).getKey());
        assertEquals("plain", PeakUiSerializer.GSON.fromJson(PeakUiSerializer.GSON.toJson(PeakWaypoint.of(0, 0, 0, "plain")),
                PeakWaypoint.class).getLabel().getString());
    }

    @Test
    void menuTitlesAndTooltipsAreComponents() {
        PeakDocument menu = PeakMenu.create("test:menu", settings -> settings.title(Component.translatable("my.title")));
        PeakLabel label = PeakLabel.create("test:label", "x");
        label.tooltip(Component.translatable("my.tooltip"));
        menu.add(label);

        PeakDocument read = PeakUiSerializer.documentFromJson(PeakUiSerializer.toJson(menu));

        assertInstanceOf(TranslatableContents.class, read.getMenu().getTitle().getContents());
        PeakLabel readLabel = (PeakLabel) read.findById("test:label");
        assertInstanceOf(TranslatableContents.class, readLabel.getInteraction().getTooltip().getContents());
    }
}
