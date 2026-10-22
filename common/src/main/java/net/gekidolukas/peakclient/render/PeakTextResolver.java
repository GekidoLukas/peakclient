package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.ui.PeakStyledText;
import net.gekidolukas.peakclient.ui.client.PeakComponentText;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public final class PeakTextResolver {
    private static final Map<Object, Map<String, Entry>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Object, Map<String, StyledEntry>> STYLED_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private static final Set<String> WARNED_KEYS = new HashSet<>();
    private static Language warnedLanguage;

    private record Entry(Component component, Language language, String legacy) {}

    private record StyledEntry(Component component, Language language, PeakStyledText text) {}

    private PeakTextResolver() {}

    public static PeakStyledText styled(Object owner, Component component) {
        return styled(owner, "", component);
    }

    public static PeakStyledText styled(Object owner, String slot, Component component) {
        if (component == null) return PeakStyledText.EMPTY;
        if (owner == null) return PeakStyledText.of(component);
        Language language = Language.getInstance();
        Map<String, StyledEntry> slots = STYLED_CACHE.computeIfAbsent(owner, key -> new HashMap<>());
        StyledEntry entry = slots.get(slot);
        if (entry != null && entry.component() == component && entry.language() == language) return entry.text();
        warnMissing(component);
        PeakStyledText text = PeakStyledText.of(component);
        if (isStatic(component)) slots.put(slot, new StyledEntry(component, language, text));
        else slots.remove(slot);
        return text;
    }

    public static PeakStyledText resolved(Object owner, Component component) {
        return PeakPlaceholderResolver.resolve(styled(owner, "", component));
    }

    public static PeakStyledText resolved(Object owner, String slot, Component component) {
        return PeakPlaceholderResolver.resolve(styled(owner, slot, component));
    }

    public static String legacy(Object owner, Component component) {
        return legacy(owner, "", component);
    }

    public static String legacy(Object owner, String slot, Component component) {
        if (component == null) return "";
        if (owner == null) return PeakComponentText.toLegacy(component);
        Language language = Language.getInstance();
        Map<String, Entry> slots = CACHE.computeIfAbsent(owner, key -> new HashMap<>());
        Entry entry = slots.get(slot);
        if (entry != null && entry.component() == component && entry.language() == language) return entry.legacy();
        String legacy = PeakComponentText.toLegacy(component);
        if (isStatic(component)) slots.put(slot, new Entry(component, language, legacy));
        else slots.remove(slot);
        return legacy;
    }

    public static String resolve(Object owner, Component component) {
        return PeakPlaceholderResolver.resolve(legacy(owner, "", component));
    }

    public static String resolve(Object owner, String slot, Component component) {
        return PeakPlaceholderResolver.resolve(legacy(owner, slot, component));
    }

    public static List<String> missingKeys(Component component, Predicate<String> has) {
        List<String> missing = new ArrayList<>();
        collectMissing(component, has, missing);
        return missing;
    }

    private static void collectMissing(Component component, Predicate<String> has, List<String> missing) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            if (translatable.getFallback() == null && !has.test(translatable.getKey())) missing.add(translatable.getKey());
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component argument) collectMissing(argument, has, missing);
            }
        }
        for (Component sibling : component.getSiblings()) collectMissing(sibling, has, missing);
    }

    private static void warnMissing(Component component) {
        Language language = Language.getInstance();
        synchronized (WARNED_KEYS) {
            if (language != warnedLanguage) {
                WARNED_KEYS.clear();
                warnedLanguage = language;
            }
            for (String key : missingKeys(component, language::has)) {
                if (WARNED_KEYS.add(key)) {
                    PeakLog.warn("Translation key {} is missing in the current language, the key is shown as text", key);
                }
            }
        }
    }

    static boolean isStatic(Component component) {
        ComponentContents contents = component.getContents();
        if (contents instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component argument && !isStatic(argument)) return false;
            }
        } else if (!(contents instanceof PlainTextContents)) {
            return false;
        }
        for (Component sibling : component.getSiblings()) {
            if (!isStatic(sibling)) return false;
        }
        return true;
    }
}
