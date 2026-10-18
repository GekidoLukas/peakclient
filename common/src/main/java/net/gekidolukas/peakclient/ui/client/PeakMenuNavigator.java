package net.gekidolukas.peakclient.ui.client;

import java.util.ArrayList;
import java.util.List;

public final class PeakMenuNavigator {
    public static final int MAX_DEPTH = 16;

    private final List<String> stack = new ArrayList<>();

    public String current() {
        return stack.isEmpty() ? null : stack.get(stack.size() - 1);
    }

    public boolean isOpen() {
        return !stack.isEmpty();
    }

    public List<String> history() {
        return List.copyOf(stack);
    }

    public boolean contains(String menuId) {
        return stack.contains(menuId);
    }

    public void open(String menuId, boolean replace) {
        int existing = stack.indexOf(menuId);
        if (existing >= 0) {
            while (stack.size() > existing + 1) stack.remove(stack.size() - 1);
            return;
        }
        if (replace && !stack.isEmpty()) {
            stack.set(stack.size() - 1, menuId);
            return;
        }
        stack.add(menuId);
        while (stack.size() > MAX_DEPTH) stack.remove(0);
    }

    public void back() {
        if (stack.size() <= 1) {
            stack.clear();
            return;
        }
        stack.remove(stack.size() - 1);
    }

    public void closeAll() {
        stack.clear();
    }

    public void remove(String menuId) {
        stack.removeIf(menuId::equals);
    }
}
