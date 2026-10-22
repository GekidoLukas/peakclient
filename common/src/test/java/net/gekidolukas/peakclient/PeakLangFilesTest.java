package net.gekidolukas.peakclient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gekidolukas.peakclient.render.PeakTextResolver;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PeakLangFilesTest {
    private static final Pattern ARGUMENT = Pattern.compile("%(\\d+\\$)?[sd]");

    private static JsonObject load(String language) throws IOException {
        try (InputStream stream = PeakLangFilesTest.class.getResourceAsStream("/assets/peakclient/lang/" + language + ".json")) {
            assertNotNull(stream, language + ".json is missing");
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static int arguments(String text) {
        Matcher matcher = ARGUMENT.matcher(text);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    @Test
    void everyEnglishKeyIsTranslatedToGerman() throws IOException {
        JsonObject english = load("en_us");
        JsonObject german = load("de_de");

        assertEquals(new TreeSet<>(english.keySet()), new TreeSet<>(german.keySet()));
        for (Map.Entry<String, JsonElement> entry : english.entrySet()) {
            String translated = german.get(entry.getKey()).getAsString();
            assertFalse(translated.isEmpty(), entry.getKey());
            assertEquals(arguments(entry.getValue().getAsString()), arguments(translated), entry.getKey());
        }
    }

    @Test
    void missingKeysAreFoundInArgumentsAndSiblings() {
        Component component = Component.translatable("known", Component.translatable("missing.argument"))
                .append(Component.translatable("missing.sibling"))
                .append(Component.translatableWithFallback("missing.fallback", "ok"))
                .append(Component.literal("plain"));

        List<String> missing = PeakTextResolver.missingKeys(component, key -> key.equals("known"));

        assertEquals(List.of("missing.argument", "missing.sibling"), missing);
    }
}
