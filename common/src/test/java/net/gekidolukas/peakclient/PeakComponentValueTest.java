package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.render.PeakPlaceholderResolver;
import net.gekidolukas.peakclient.server.PeakValueKey;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.client.PeakClientCustomValues;
import net.gekidolukas.peakclient.ui.serialize.PeakComponentValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakComponentValueTest {

    @AfterEach
    void clearValues() {
        PeakClientCustomValues.clearAll();
    }

    @Test
    void aComponentSurvivesTheEncodingWithItsStyle() {
        String encoded = PeakComponentValue.encode(Component.literal("Bob").withStyle(ChatFormatting.RED));

        assertTrue(PeakComponentValue.isEncoded(encoded));
        Component back = PeakComponentValue.decode(encoded);
        assertEquals("Bob", back.getString());
        assertEquals(ChatFormatting.RED.getColor(), back.getStyle().getColor().getValue());
    }

    @Test
    void plainTextIsNotTouched() {
        assertFalse(PeakComponentValue.isEncoded("Bob"));
        assertEquals("Bob", PeakComponentValue.plain("Bob"));
        assertEquals("", PeakComponentValue.plain(""));
    }

    @Test
    void anEncodedValueHasAPlainForm() {
        assertEquals("Bob", PeakComponentValue.plain(PeakComponentValue.encode(Component.literal("Bob").withStyle(ChatFormatting.RED))));
        assertEquals("some.key", PeakComponentValue.plain(PeakComponentValue.encode(Component.translatable("some.key"))));
    }

    @Test
    void aBrokenValueDecodesToNothing() {
        assertEquals("", PeakComponentValue.decode("\u0001{not json").getString());
    }

    @Test
    void theValueKeyFormatsComponents() {
        PeakValueKey<Component> key = PeakValueKey.ofComponent("test:name");

        assertEquals("{test:name#-}", key.placeholder("-"));
        assertTrue(PeakComponentValue.isEncoded(key.format(Component.literal("x"))));
    }

    @Test
    void aStyledValueKeepsItsOwnStyleAndOtherwiseTakesTheStyleOfThePlaceholder() {
        PeakClientCustomValues.set("test", "red", PeakComponentValue.encode(Component.literal("Bob").withStyle(ChatFormatting.RED)));
        PeakClientCustomValues.set("test", "plain", PeakComponentValue.encode(Component.literal("Amy")));

        PeakStyledText red = PeakPlaceholderResolver.resolve(PeakStyledText.ofLegacy("Hi §a{test:red}!"));
        PeakStyledText plain = PeakPlaceholderResolver.resolve(PeakStyledText.ofLegacy("Hi §a{test:plain}!"));

        assertEquals("Hi Bob!", red.plain());
        assertEquals(ChatFormatting.RED.getColor(), red.styleAt(3).getColor().getValue());
        assertEquals(ChatFormatting.GREEN.getColor(), red.styleAt(6).getColor().getValue());
        assertEquals("Hi Amy!", plain.plain());
        assertEquals(ChatFormatting.GREEN.getColor(), plain.styleAt(3).getColor().getValue());
        assertNull(red.styleAt(0).getColor());
    }

    @Test
    void stringsAndBindingsNeverSeeTheMarker() {
        PeakClientCustomValues.set("test", "name", PeakComponentValue.encode(Component.literal("Bob").withStyle(ChatFormatting.RED)));

        assertEquals("x Bob", PeakPlaceholderResolver.resolve("x {test:name}"));
        PeakPlaceholderResolver.Result strict = PeakPlaceholderResolver.resolveStrict("{test:name}");
        assertEquals("Bob", strict.value());
        assertTrue(strict.complete());
    }
}
