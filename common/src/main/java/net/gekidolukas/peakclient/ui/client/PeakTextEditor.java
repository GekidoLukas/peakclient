package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.ui.PeakTextFilter;

public final class PeakTextEditor {
    private String value = "";
    private int caret;
    private int anchor;
    private int maxLength = 64;
    private PeakTextFilter filter = PeakTextFilter.ANY;
    private int scrollPx;
    private String syncedFieldValue;

    public boolean needsSync(String fieldValue) { return !fieldValue.equals(syncedFieldValue); }

    public void synced(String fieldValue) { syncedFieldValue = fieldValue; }

    public String value() { return value; }

    public int caret() { return caret; }

    public int scrollPx() { return scrollPx; }

    public void scrollPx(int scrollPx) { this.scrollPx = Math.max(0, scrollPx); }

    public boolean hasSelection() { return caret != anchor; }

    public int selectionStart() { return Math.min(caret, anchor); }

    public int selectionEnd() { return Math.max(caret, anchor); }

    public String selectedText() { return value.substring(selectionStart(), selectionEnd()); }

    public void configure(int maxLength, PeakTextFilter filter) {
        this.maxLength = Math.max(1, maxLength);
        this.filter = filter == null ? PeakTextFilter.ANY : filter;
    }

    public void setValue(String newValue) {
        StringBuilder cleaned = new StringBuilder();
        for (char character : newValue.toCharArray()) {
            if (isAllowedCharacter(character) && cleaned.length() < maxLength) cleaned.append(character);
        }
        value = cleaned.toString();
        caret = value.length();
        anchor = caret;
    }

    public boolean insert(String text) {
        boolean changed = deleteSelection();
        for (char character : text.toCharArray()) {
            if (!isAllowedCharacter(character) || value.length() >= maxLength) continue;
            String candidate = value.substring(0, caret) + character + value.substring(caret);
            if (!filter.isValid(candidate)) continue;
            value = candidate;
            caret++;
            changed = true;
        }
        anchor = caret;
        return changed;
    }

    public boolean deleteSelection() {
        if (!hasSelection()) return false;
        int start = selectionStart();
        value = value.substring(0, start) + value.substring(selectionEnd());
        caret = start;
        anchor = start;
        return true;
    }

    public boolean backspace(boolean word) {
        if (deleteSelection()) return true;
        if (caret == 0) return false;
        int start = word ? wordStart(caret) : caret - 1;
        value = value.substring(0, start) + value.substring(caret);
        caret = start;
        anchor = start;
        return true;
    }

    public boolean delete(boolean word) {
        if (deleteSelection()) return true;
        if (caret >= value.length()) return false;
        int end = word ? wordEnd(caret) : caret + 1;
        value = value.substring(0, caret) + value.substring(end);
        anchor = caret;
        return true;
    }

    public void moveLeft(boolean word, boolean extend) {
        if (!extend && hasSelection()) {
            caret = selectionStart();
            anchor = caret;
            return;
        }
        caret = word ? wordStart(caret) : Math.max(0, caret - 1);
        if (!extend) anchor = caret;
    }

    public void moveRight(boolean word, boolean extend) {
        if (!extend && hasSelection()) {
            caret = selectionEnd();
            anchor = caret;
            return;
        }
        caret = word ? wordEnd(caret) : Math.min(value.length(), caret + 1);
        if (!extend) anchor = caret;
    }

    public void home(boolean extend) {
        caret = 0;
        if (!extend) anchor = caret;
    }

    public void end(boolean extend) {
        caret = value.length();
        if (!extend) anchor = caret;
    }

    public void selectAll() {
        anchor = 0;
        caret = value.length();
    }

    public void selectRange(int start, int end) {
        anchor = Math.max(0, Math.min(start, value.length()));
        caret = Math.max(0, Math.min(end, value.length()));
    }

    public void selectWordAt(int index) {
        if (value.isEmpty()) return;
        int position = Math.max(0, Math.min(index, value.length() - 1));
        boolean space = value.charAt(position) == ' ';
        int start = position;
        while (start > 0 && (value.charAt(start - 1) == ' ') == space) start--;
        int end = position + 1;
        while (end < value.length() && (value.charAt(end) == ' ') == space) end++;
        selectRange(start, end);
    }

    public boolean deleteRange(int start, int end) {
        int from = Math.max(0, Math.min(start, value.length()));
        int to = Math.max(from, Math.min(end, value.length()));
        if (to == from) return false;
        value = value.substring(0, from) + value.substring(to);
        caret = from;
        anchor = from;
        return true;
    }

    public void setCaret(int index, boolean extend) {
        caret = Math.max(0, Math.min(index, value.length()));
        if (!extend) anchor = caret;
    }

    private int wordStart(int from) {
        int i = from;
        while (i > 0 && value.charAt(i - 1) == ' ') i--;
        while (i > 0 && value.charAt(i - 1) != ' ') i--;
        return i;
    }

    private int wordEnd(int from) {
        int i = from;
        while (i < value.length() && value.charAt(i) == ' ') i++;
        while (i < value.length() && value.charAt(i) != ' ') i++;
        return i;
    }

    private static boolean isAllowedCharacter(char character) {
        return character >= 32 && character != 127 && character != '§';
    }
}
