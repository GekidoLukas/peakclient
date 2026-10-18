package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakDropdownOverlay;
import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakDropdown;
import net.gekidolukas.peakclient.ui.PeakDropdownLayout;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakOption;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.minecraft.client.gui.GuiGraphics;

import static com.mojang.blaze3d.platform.InputConstants.KEY_DOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_END;
import static com.mojang.blaze3d.platform.InputConstants.KEY_HOME;
import static com.mojang.blaze3d.platform.InputConstants.KEY_LEFT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_NUMPADENTER;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEDOWN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_PAGEUP;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RETURN;
import static com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT;
import static com.mojang.blaze3d.platform.InputConstants.KEY_SPACE;
import static com.mojang.blaze3d.platform.InputConstants.KEY_UP;

final class PeakDropdownController implements PeakInputController<PeakDropdown> {
    private final PeakMenuContext context;

    PeakDropdownController(PeakMenuContext context) {
        this.context = context;
    }

    @Override
    public void pressed(PeakDropdown dropdown, PeakLayoutEntry hit, double mouseX, double mouseY) {
        context.input().focus(dropdown.getId());
        openFor(dropdown);
    }

    @Override
    public boolean key(PeakDropdown dropdown, int keyCode) {
        if (keyCode == KEY_RETURN || keyCode == KEY_NUMPADENTER || keyCode == KEY_SPACE) {
            PeakLayoutEntry entry = context.entryOf(dropdown);
            if (entry != null) openFor(dropdown);
            return true;
        }
        int current = dropdown.selectedIndex();
        int target;
        if (keyCode == KEY_UP || keyCode == KEY_LEFT) {
            target = current < 0 ? dropdown.lastEnabledIndex() : dropdown.nextEnabledIndex(current, -1);
        } else if (keyCode == KEY_DOWN || keyCode == KEY_RIGHT) {
            target = current < 0 ? dropdown.firstEnabledIndex() : dropdown.nextEnabledIndex(current, 1);
        } else if (keyCode == KEY_HOME) {
            target = dropdown.firstEnabledIndex();
        } else if (keyCode == KEY_END) {
            target = dropdown.lastEnabledIndex();
        } else {
            return false;
        }
        if (target >= 0 && target != current) {
            PeakOption option = dropdown.getOptions().get(target);
            dropdown.selected(option.getValue());
            context.playClick(dropdown.getInteraction());
            context.fireChange(dropdown, PeakEvent.CHANGE, dropdown.inputValue());
        }
        return true;
    }

    PeakDropdown open() {
        PeakMenuInputState input = context.input();
        String id = input.openDropdownId();
        if (id == null) return null;
        PeakDocumentModel model = PeakClientMenus.model(context.menuId());
        PeakElement<?> element = model == null ? null : model.find(id);
        if (element instanceof PeakDropdown dropdown && PeakMenuContext.isEnabled(dropdown)
                && context.entryOf(dropdown) != null) {
            return dropdown;
        }
        input.closeDropdown();
        return null;
    }

    void drawOverlay(GuiGraphics graphics, PeakDropdown dropdown, int mouseX, int mouseY, PeakMenuSettings settings) {
        PeakMenuInputState input = context.input();
        PeakDropdownLayout layout = layout(dropdown, input.dropdownScroll());
        if (layout == null) return;
        input.dropdownScroll(layout.firstRow());
        int highlight = layout.contains(mouseX, mouseY) ? layout.rowAt(mouseX, mouseY) : input.dropdownCursor();
        PeakDropdownOverlay.draw(graphics, context.minecraft().font, dropdown, layout, highlight,
                PeakTheme.focusColor(settings, dropdown.getInteraction()));
    }

    void overlayClick(PeakDropdown dropdown, double mouseX, double mouseY, int button) {
        PeakDropdownLayout layout = layout(dropdown, context.input().dropdownScroll());
        if (button == 0 && layout != null && layout.contains((int) mouseX, (int) mouseY)) {
            int index = layout.rowAt((int) mouseX, (int) mouseY);
            if (index >= 0 && dropdown.getOptions().get(index).isEnabled()) choose(dropdown, index);
            return;
        }
        context.input().closeDropdown();
    }

    boolean overlayKey(PeakDropdown dropdown, int keyCode) {
        PeakMenuInputState input = context.input();
        PeakDropdownLayout layout = layout(dropdown, input.dropdownScroll());
        if (layout == null) return false;
        int cursor = input.dropdownCursor();
        int target = cursor;
        if (keyCode == KEY_UP) {
            target = cursor < 0 ? dropdown.lastEnabledIndex() : dropdown.nextEnabledIndex(cursor, -1);
        } else if (keyCode == KEY_DOWN) {
            target = cursor < 0 ? dropdown.firstEnabledIndex() : dropdown.nextEnabledIndex(cursor, 1);
        } else if (keyCode == KEY_HOME) {
            target = dropdown.firstEnabledIndex();
        } else if (keyCode == KEY_END) {
            target = dropdown.lastEnabledIndex();
        } else if (keyCode == KEY_PAGEUP || keyCode == KEY_PAGEDOWN) {
            int jump = Math.max(1, layout.visibleRows() - 1) * (keyCode == KEY_PAGEUP ? -1 : 1);
            target = Math.max(0, Math.min(dropdown.getOptions().size() - 1, Math.max(0, cursor) + jump));
        } else if (keyCode == KEY_RETURN || keyCode == KEY_NUMPADENTER || keyCode == KEY_SPACE) {
            choose(dropdown, cursor);
            return true;
        } else {
            return false;
        }
        if (target >= 0) {
            input.dropdownCursor(target);
            input.dropdownScroll(layout.scrollToShow(target));
        }
        return true;
    }

    void overlayWheel(PeakDropdown dropdown, double scrollY) {
        PeakMenuInputState input = context.input();
        PeakDropdownLayout layout = layout(dropdown, input.dropdownScroll());
        if (layout != null && scrollY != 0) {
            int next = layout.firstRow() + (scrollY > 0 ? -2 : 2);
            input.dropdownScroll(Math.max(0, Math.min(next, layout.maxFirstRow())));
        }
    }

    private PeakDropdownLayout layout(PeakDropdown dropdown, int scrollRow) {
        PeakLayoutEntry entry = context.entryOf(dropdown);
        if (entry == null) return null;
        var font = context.minecraft().font;
        return PeakDropdownLayout.of(entry.absX(), entry.absY(), entry.width(), entry.height(),
                context.width(), context.height(),
                dropdown.getOptions().size(), dropdown.getMaxVisible(), PeakDropdownOverlay.rowHeight(font),
                PeakDropdownOverlay.widestText(font, dropdown), scrollRow);
    }

    private void openFor(PeakDropdown dropdown) {
        if (dropdown.getOptions().isEmpty()) return;
        int cursor = dropdown.selectedIndex() >= 0 ? dropdown.selectedIndex() : dropdown.firstEnabledIndex();
        PeakDropdownLayout layout = layout(dropdown, 0);
        int scroll = layout == null ? 0 : layout.scrollToShow(cursor);
        context.input().openDropdown(dropdown.getId(), scroll, cursor);
        context.playClick(dropdown.getInteraction());
    }

    private void choose(PeakDropdown dropdown, int index) {
        context.input().closeDropdown();
        if (index < 0 || index >= dropdown.getOptions().size()) return;
        PeakOption option = dropdown.getOptions().get(index);
        if (!option.isEnabled()) return;
        context.playClick(dropdown.getInteraction());
        if (option.getValue().equals(dropdown.getSelected())) return;
        dropdown.selected(option.getValue());
        context.fireChange(dropdown, PeakEvent.CHANGE, dropdown.inputValue());
    }
}
