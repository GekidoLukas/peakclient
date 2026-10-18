package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakPaint;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PeakChoiceList {
    public static final int SELECTED_BACKGROUND = 0xFF3C78C8;

    private record Option(String value, String text) {}

    private final String id;
    private final String actionId;
    private final List<Option> options = new ArrayList<>();
    private String selected;
    private int width = 160;
    private int gap = 2;
    private PeakPaint selectedBackground = PeakPaint.solid(SELECTED_BACKGROUND);
    private PeakPaint normalBackground = PeakPaint.solid(PeakButton.DEFAULT_BACKGROUND);
    private PeakPaint selectedColor = PeakPaint.NONE;
    private PeakPaint normalColor = PeakPaint.NONE;
    private Consumer<PeakButton> optionButton;

    private PeakChoiceList(String id, String actionId) {
        this.id = PeakId.require(id, "Choice list");
        this.actionId = actionId;
    }

    public static PeakChoiceList create(String id, String actionId) {
        return new PeakChoiceList(id, actionId);
    }

    public PeakChoiceList option(String value, String text) {
        options.add(new Option(value, text));
        return this;
    }

    public PeakChoiceList selected(String value) {
        this.selected = value;
        return this;
    }

    public PeakChoiceList width(int width) {
        this.width = width;
        return this;
    }

    public PeakChoiceList gap(int gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public PeakChoiceList selectedBackground(PeakPaint paint) {
        this.selectedBackground = paint;
        return this;
    }

    public PeakChoiceList selectedBackground(int argb) {
        return selectedBackground(PeakPaint.solid(argb));
    }

    public PeakChoiceList normalBackground(PeakPaint paint) {
        this.normalBackground = paint;
        return this;
    }

    public PeakChoiceList normalBackground(int argb) {
        return normalBackground(PeakPaint.solid(argb));
    }

    public PeakChoiceList selectedColor(PeakPaint paint) {
        this.selectedColor = paint;
        return this;
    }

    public PeakChoiceList selectedColor(int argb) {
        return selectedColor(PeakPaint.solid(argb));
    }

    public PeakChoiceList normalColor(PeakPaint paint) {
        this.normalColor = paint;
        return this;
    }

    public PeakChoiceList normalColor(int argb) {
        return normalColor(PeakPaint.solid(argb));
    }

    public PeakChoiceList optionButton(Consumer<PeakButton> customizer) {
        this.optionButton = customizer;
        return this;
    }

    public static String optionId(String listId, int index) {
        return listId + "/option-" + index;
    }

    public PeakPanel build() {
        PeakPanel root = PeakPanel.create(id).column().gap(gap).padding(0).background(0);
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            PeakButton button = PeakButton.text(optionId(id, i), option.text()).width(width).onClick(selectActions(i));
            if (optionButton != null) optionButton.accept(button);
            boolean isSelected = option.value().equals(selected);
            button.background(isSelected ? selectedBackground : normalBackground);
            PeakPaint labelColor = isSelected ? selectedColor : normalColor;
            if (!labelColor.isNone()) {
                for (PeakElement<?> child : button.getChildren()) {
                    if (child instanceof PeakLabel label && label.getId().equals(optionId(id, i) + "/label")) label.color(labelColor);
                }
            }
            root.add(button);
        }
        return root;
    }

    private PeakAction[] selectActions(int target) {
        List<PeakAction> actions = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            boolean isTarget = i == target;
            actions.add(PeakAction.patch(optionId(id, i),
                    PeakPatch.button().background(isTarget ? selectedBackground : normalBackground)));
            PeakPaint labelColor = isTarget ? selectedColor : normalColor;
            if (!labelColor.isNone()) {
                actions.add(PeakAction.patch(optionId(id, i) + "/label", PeakPatch.label().color(labelColor)));
            }
        }
        actions.add(PeakAction.send(actionId).arg("value", options.get(target).value()));
        return actions.toArray(new PeakAction[0]);
    }
}
