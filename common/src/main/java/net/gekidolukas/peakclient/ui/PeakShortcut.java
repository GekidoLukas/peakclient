package net.gekidolukas.peakclient.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class PeakShortcut {
    private String key = "";
    private boolean ctrl;
    private boolean shift;
    private boolean alt;
    private List<PeakAction> actions = new ArrayList<>();

    public static PeakShortcut of(String spec, PeakAction... actions) {
        PeakShortcut shortcut = new PeakShortcut();
        String[] parts = spec.trim().split("\\+");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim().toUpperCase(Locale.ROOT);
            if (part.isEmpty()) throw new IllegalArgumentException("Empty part in shortcut '" + spec + "'");
            if (i < parts.length - 1) {
                switch (part) {
                    case "CTRL", "CONTROL" -> shortcut.ctrl = true;
                    case "SHIFT" -> shortcut.shift = true;
                    case "ALT" -> shortcut.alt = true;
                    default -> throw new IllegalArgumentException("Unknown modifier '" + part + "' in shortcut '" + spec + "'");
                }
            } else {
                if (keyCode(part) < 0) throw new IllegalArgumentException("Unknown key '" + part + "' in shortcut '" + spec + "'");
                shortcut.key = part;
            }
        }
        shortcut.actions.addAll(Arrays.asList(actions));
        return shortcut;
    }

    public String getKey() { return key == null ? "" : key; }
    public boolean isCtrl() { return ctrl; }
    public boolean isShift() { return shift; }
    public boolean isAlt() { return alt; }
    public List<PeakAction> getActions() { return actions == null ? List.of() : actions; }

    public String spec() {
        StringBuilder text = new StringBuilder();
        if (ctrl) text.append("ctrl+");
        if (shift) text.append("shift+");
        if (alt) text.append("alt+");
        return text.append(getKey()).toString();
    }

    public int keyCode() {
        return keyCode(getKey());
    }

    public boolean matches(int glfwKey, boolean ctrlDown, boolean shiftDown, boolean altDown) {
        return glfwKey == keyCode() && ctrl == ctrlDown && shift == shiftDown && alt == altDown;
    }

    public static int keyCode(String name) {
        if (name == null || name.isEmpty()) return -1;
        if (name.length() == 1) {
            char character = name.charAt(0);
            if (character >= 'A' && character <= 'Z') return character;
            if (character >= '0' && character <= '9') return character;
        }
        if (name.startsWith("F") && name.length() <= 3) {
            try {
                int number = Integer.parseInt(name.substring(1));
                if (number >= 1 && number <= 12) return 289 + number;
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return switch (name) {
            case "SPACE" -> 32;
            case "ENTER", "RETURN" -> 257;
            case "TAB" -> 258;
            case "BACKSPACE" -> 259;
            case "INSERT" -> 260;
            case "DELETE" -> 261;
            case "RIGHT" -> 262;
            case "LEFT" -> 263;
            case "DOWN" -> 264;
            case "UP" -> 265;
            case "PAGE_UP" -> 266;
            case "PAGE_DOWN" -> 267;
            case "HOME" -> 268;
            case "END" -> 269;
            default -> -1;
        };
    }
}
