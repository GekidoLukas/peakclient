package net.gekidolukas.peakclient.ui.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PeakRichTextEditor {
    private PeakRichText text = PeakRichText.parse("");
    private int caret;
    private int anchor;
    private int maxLength = 512;
    private int maxLines;
    private PeakRichText.Style pending;
    private int preferredX = -1;
    private int revision;
    private int observedStamp = Integer.MIN_VALUE;
    private String syncedFieldValue;
    private final Map<Integer, PeakTextLayout> layouts = new HashMap<>();
    private int layoutRevision = -1;
    private int scrollX;

    public void configure(int maxLength, int maxLines) {
        this.maxLength = Math.max(1, maxLength);
        this.maxLines = Math.max(0, maxLines);
    }

    public boolean needsSync(String fieldValue) { return !fieldValue.equals(syncedFieldValue); }

    public void synced(String fieldValue) { syncedFieldValue = fieldValue; }

    public PeakRichText text() { return text; }

    public String value() { return text.serialize(); }

    public int caret() { return caret; }

    public int anchor() { return anchor; }

    public boolean hasSelection() { return caret != anchor; }

    public int selectionStart() { return Math.min(caret, anchor); }

    public int selectionEnd() { return Math.max(caret, anchor); }

    public String selectedPlain() { return text.plain(selectionStart(), selectionEnd()); }

    public void setValue(String legacy) {
        text = PeakRichText.parse(legacy);
        while (text.length() > 0 && text.serialize().length() > maxLength) {
            text.delete(text.length() - 1, text.length());
        }
        caret = text.length();
        anchor = caret;
        pending = null;
        preferredX = -1;
        revision++;
    }

    public boolean takeCaretMoved() {
        int stamp = revision * 31 + caret * 7 + anchor;
        if (stamp == observedStamp) return false;
        observedStamp = stamp;
        return true;
    }

    public PeakTextLayout layout(int wrapWidth, PeakTextLayout.Measure measure) {
        if (layoutRevision != revision) {
            layouts.clear();
            layoutRevision = revision;
        }
        PeakTextLayout built = layouts.get(wrapWidth);
        if (built == null) {
            if (layouts.size() >= 4) layouts.clear();
            built = PeakTextLayout.build(text, wrapWidth, measure);
            layouts.put(wrapWidth, built);
        }
        return built;
    }

    public int scrollX() { return scrollX; }

    public void scrollX(int value) { this.scrollX = Math.max(0, value); }

    private void changed() {
        revision++;
    }

    private PeakRichText.Style insertStyle() {
        if (pending != null) return pending;
        return caret > 0 ? text.styleAt(caret - 1) : PeakRichText.Style.NONE;
    }

    private int newlineCount() {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') count++;
        }
        return count;
    }

    private boolean insertCharacter(char character, PeakRichText.Style style) {
        text.insert(caret, String.valueOf(character), style);
        if (text.serialize().length() > maxLength) {
            text.delete(caret, caret + 1);
            return false;
        }
        caret++;
        return true;
    }

    public boolean insertText(String input, boolean allowNewlines) {
        boolean modified = deleteSelection();
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            if (character == '\r') continue;
            if (character == '\n') {
                if (!allowNewlines || (maxLines > 0 && newlineCount() + 1 >= maxLines)) continue;
            } else if (character < 32 || character == 127 || character == '§') {
                continue;
            }
            if (insertCharacter(character, insertStyle())) {
                modified = true;
                pending = null;
            }
        }
        anchor = caret;
        preferredX = -1;
        if (modified) changed();
        return modified;
    }

    public boolean insertTyped(char character) {
        return insertText(String.valueOf(character), false);
    }

    public boolean insertNewline() {
        return insertText("\n", true);
    }

    public boolean deleteSelection() {
        if (!hasSelection()) return false;
        int start = selectionStart();
        text.delete(start, selectionEnd());
        caret = start;
        anchor = start;
        pending = null;
        changed();
        return true;
    }

    public boolean backspace(boolean word) {
        if (deleteSelection()) return true;
        if (caret == 0) return false;
        int start = word ? wordStart(caret) : caret - 1;
        text.delete(start, caret);
        caret = start;
        anchor = start;
        pending = null;
        preferredX = -1;
        changed();
        return true;
    }

    public boolean delete(boolean word) {
        if (deleteSelection()) return true;
        if (caret >= text.length()) return false;
        int end = word ? wordEnd(caret) : caret + 1;
        text.delete(caret, end);
        anchor = caret;
        pending = null;
        preferredX = -1;
        changed();
        return true;
    }

    public void moveLeft(boolean word, boolean extend) {
        preferredX = -1;
        pending = null;
        if (!extend && hasSelection()) {
            caret = selectionStart();
            anchor = caret;
            return;
        }
        caret = word ? wordStart(caret) : Math.max(0, caret - 1);
        if (!extend) anchor = caret;
    }

    public void moveRight(boolean word, boolean extend) {
        preferredX = -1;
        pending = null;
        if (!extend && hasSelection()) {
            caret = selectionEnd();
            anchor = caret;
            return;
        }
        caret = word ? wordEnd(caret) : Math.min(text.length(), caret + 1);
        if (!extend) anchor = caret;
    }

    public void moveVertical(int direction, boolean extend, PeakTextLayout layout, PeakTextLayout.Measure measure) {
        pending = null;
        int line = layout.lineOf(caret);
        if (preferredX < 0) preferredX = layout.xOf(caret, measure);
        int target = line + direction;
        if (target < 0) {
            caret = 0;
        } else if (target >= layout.lineCount()) {
            caret = text.length();
        } else {
            caret = layout.indexAt(target, preferredX, measure);
        }
        if (!extend) anchor = caret;
    }

    public void lineStart(PeakTextLayout layout, boolean extend) {
        preferredX = -1;
        pending = null;
        caret = layout.line(layout.lineOf(caret)).start();
        if (!extend) anchor = caret;
    }

    public void lineEnd(PeakTextLayout layout, boolean extend) {
        preferredX = -1;
        pending = null;
        PeakTextLayout.Line line = layout.line(layout.lineOf(caret));
        int end = line.end();
        boolean soft = end < text.length() && text.charAt(end) != '\n';
        if (soft && end > line.start()) end--;
        caret = end;
        if (!extend) anchor = caret;
    }

    public void documentStart(boolean extend) {
        preferredX = -1;
        pending = null;
        caret = 0;
        if (!extend) anchor = caret;
    }

    public void documentEnd(boolean extend) {
        preferredX = -1;
        pending = null;
        caret = text.length();
        if (!extend) anchor = caret;
    }

    public void selectAll() {
        preferredX = -1;
        anchor = 0;
        caret = text.length();
    }

    public void selectRange(int start, int end) {
        preferredX = -1;
        pending = null;
        anchor = Math.max(0, Math.min(start, text.length()));
        caret = Math.max(0, Math.min(end, text.length()));
    }

    public void selectWordAt(int index) {
        if (text.length() == 0) return;
        int position = Math.max(0, Math.min(index, text.length() - 1));
        boolean separator = isSeparator(text.charAt(position));
        int start = position;
        while (start > 0 && isSeparator(text.charAt(start - 1)) == separator) start--;
        int end = position + 1;
        while (end < text.length() && isSeparator(text.charAt(end)) == separator) end++;
        selectRange(start, end);
    }

    public void selectParagraphAt(int index) {
        int position = Math.max(0, Math.min(index, text.length()));
        int start = position;
        while (start > 0 && text.charAt(start - 1) != '\n') start--;
        int end = position;
        while (end < text.length() && text.charAt(end) != '\n') end++;
        selectRange(start, end);
    }

    public boolean deleteRange(int start, int end) {
        int from = Math.max(0, Math.min(start, text.length()));
        int to = Math.max(from, Math.min(end, text.length()));
        if (to == from) return false;
        text.delete(from, to);
        caret = from;
        anchor = from;
        pending = null;
        preferredX = -1;
        changed();
        return true;
    }

    public PeakRichText selectedFragment() {
        return text.slice(selectionStart(), selectionEnd());
    }

    public boolean insertRich(PeakRichText fragment) {
        boolean modified = deleteSelection();
        for (int i = 0; i < fragment.length(); i++) {
            char character = fragment.charAt(i);
            if (character == '\n') {
                if (maxLines > 0 && newlineCount() + 1 >= maxLines) continue;
            } else if (character < 32 || character == 127 || character == '§') {
                continue;
            }
            if (insertCharacter(character, fragment.styleAt(i))) modified = true;
        }
        anchor = caret;
        pending = null;
        preferredX = -1;
        if (modified) changed();
        return modified;
    }

    public void setCaret(int index, boolean extend) {
        preferredX = -1;
        pending = null;
        caret = Math.max(0, Math.min(index, text.length()));
        if (!extend) anchor = caret;
    }

    public boolean toggleFlag(int flag) {
        if (hasSelection()) {
            boolean all = true;
            for (int i = selectionStart(); i < selectionEnd(); i++) {
                if (!text.styleAt(i).has(flag)) {
                    all = false;
                    break;
                }
            }
            boolean clear = all;
            text.map(selectionStart(), selectionEnd(), style -> clear ? style.without(flag) : style.with(flag));
            changed();
            return true;
        }
        pending = insertStyle().toggled(flag);
        return false;
    }

    public boolean cycleColor(List<Integer> palette, boolean backwards) {
        if (palette.isEmpty()) return false;
        PeakRichText.Style reference = hasSelection() ? text.styleAt(selectionStart()) : insertStyle();
        int currentRgb = reference.color() == 0 ? -1 : reference.rgb();
        int index = -1;
        for (int i = 0; i < palette.size(); i++) {
            if ((palette.get(i) & 0xFFFFFF) == currentRgb) {
                index = i;
                break;
            }
        }
        int next;
        if (index < 0) next = backwards ? palette.size() - 1 : 0;
        else next = Math.floorMod(index + (backwards ? -1 : 1), palette.size());
        int rgb = palette.get(next) & 0xFFFFFF;
        return applyColor(rgb);
    }

    public boolean applyColor(int rgb) {
        if (hasSelection()) {
            text.map(selectionStart(), selectionEnd(), style -> style.withColor(rgb));
            changed();
            return true;
        }
        pending = insertStyle().withColor(rgb);
        return false;
    }

    public boolean clearFormat() {
        if (hasSelection()) {
            text.map(selectionStart(), selectionEnd(), style -> PeakRichText.Style.NONE);
            changed();
            return true;
        }
        pending = PeakRichText.Style.NONE;
        return false;
    }

    public PeakRichText.Style styleAtCaret() {
        return hasSelection() ? text.styleAt(selectionStart()) : insertStyle();
    }

    private int wordStart(int from) {
        int i = from;
        while (i > 0 && isSeparator(text.charAt(i - 1))) i--;
        while (i > 0 && !isSeparator(text.charAt(i - 1))) i--;
        return i;
    }

    private int wordEnd(int from) {
        int i = from;
        while (i < text.length() && isSeparator(text.charAt(i))) i++;
        while (i < text.length() && !isSeparator(text.charAt(i))) i++;
        return i;
    }

    private static boolean isSeparator(char character) {
        return character == ' ' || character == '\n';
    }
}
