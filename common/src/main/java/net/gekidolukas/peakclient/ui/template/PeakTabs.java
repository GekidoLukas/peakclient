package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PeakTabs {
    public static final int SELECTED_BACKGROUND = 0xFF3C78C8;

    private record Tab(Component title, PeakElement<?> content) {}

    private final String id;
    private final List<Tab> tabs = new ArrayList<>();
    private int selected;
    private PeakPaint selectedBackground = PeakPaint.solid(SELECTED_BACKGROUND);
    private PeakPaint normalBackground = PeakPaint.solid(PeakButton.DEFAULT_BACKGROUND);
    private PeakPaint selectedColor = PeakPaint.NONE;
    private PeakPaint normalColor = PeakPaint.NONE;
    private int headerGap = 2;
    private int contentGap = 6;
    private Consumer<PeakButton> tabButton;

    private PeakTabs(String id) {
        this.id = PeakId.require(id, "Tabs");
    }

    public static PeakTabs create(String id) {
        return new PeakTabs(id);
    }

    public PeakTabs tab(String title, PeakElement<?> content) {
        return tab(Component.literal(title == null ? "" : title), content);
    }

    public PeakTabs tab(Component title, PeakElement<?> content) {
        tabs.add(new Tab(title == null ? Component.empty() : title, content));
        return this;
    }

    public PeakTabs selected(int index) {
        this.selected = index;
        return this;
    }

    public PeakTabs selectedBackground(PeakPaint paint) {
        this.selectedBackground = paint;
        return this;
    }

    public PeakTabs selectedBackground(int argb) {
        return selectedBackground(PeakPaint.solid(argb));
    }

    public PeakTabs normalBackground(PeakPaint paint) {
        this.normalBackground = paint;
        return this;
    }

    public PeakTabs normalBackground(int argb) {
        return normalBackground(PeakPaint.solid(argb));
    }

    public PeakTabs selectedColor(PeakPaint paint) {
        this.selectedColor = paint;
        return this;
    }

    public PeakTabs selectedColor(int argb) {
        return selectedColor(PeakPaint.solid(argb));
    }

    public PeakTabs normalColor(PeakPaint paint) {
        this.normalColor = paint;
        return this;
    }

    public PeakTabs normalColor(int argb) {
        return normalColor(PeakPaint.solid(argb));
    }

    public PeakTabs headerGap(int gap) {
        this.headerGap = Math.max(0, gap);
        return this;
    }

    public PeakTabs contentGap(int gap) {
        this.contentGap = Math.max(0, gap);
        return this;
    }

    public PeakTabs tabButton(Consumer<PeakButton> customizer) {
        this.tabButton = customizer;
        return this;
    }

    public static String tabId(String tabsId, int index) {
        return tabsId + "/tab-" + index;
    }

    public static String contentId(String tabsId, int index) {
        return tabsId + "/content-" + index;
    }

    public PeakPanel build() {
        PeakPanel root = PeakPanel.create(id).column().gap(contentGap).padding(0).background(0);
        if (tabs.isEmpty()) return root;
        int active = Math.max(0, Math.min(selected, tabs.size() - 1));

        PeakPanel header = PeakPanel.create(id + "/header").row().gap(headerGap).padding(0).background(0);
        for (int i = 0; i < tabs.size(); i++) {
            PeakButton button = PeakButton.text(tabId(id, i), tabs.get(i).title()).onClick(selectActions(i));
            if (tabButton != null) tabButton.accept(button);
            button.background(i == active ? selectedBackground : normalBackground);
            PeakPaint labelColor = i == active ? selectedColor : normalColor;
            if (!labelColor.isNone()) {
                for (PeakElement<?> child : button.getChildren()) {
                    if (child instanceof PeakLabel label && label.getId().equals(tabId(id, i) + "/label")) label.color(labelColor);
                }
            }
            header.add(button);
        }
        root.add(header);

        for (int i = 0; i < tabs.size(); i++) {
            PeakPanel content = PeakPanel.create(contentId(id, i)).column().padding(0).background(0);
            content.add(tabs.get(i).content());
            if (i != active) content.hidden(true);
            root.add(content);
        }
        return root;
    }

    private PeakAction[] selectActions(int target) {
        List<PeakAction> actions = new ArrayList<>();
        for (int i = 0; i < tabs.size(); i++) {
            boolean isTarget = i == target;
            actions.add(PeakAction.patch(contentId(id, i), PeakPatch.panel().hidden(!isTarget)));
            actions.add(PeakAction.patch(tabId(id, i),
                    PeakPatch.button().background(isTarget ? selectedBackground : normalBackground)));
            PeakPaint labelColor = isTarget ? selectedColor : normalColor;
            if (!labelColor.isNone()) {
                actions.add(PeakAction.patch(tabId(id, i) + "/label", PeakPatch.label().color(labelColor)));
            }
        }
        return actions.toArray(new PeakAction[0]);
    }
}
