package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakCheckbox;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class PeakCheckList {
    public static final String OPTION_ARG = "option";

    private record Option(String value, String text, boolean checked) {}

    private final String id;
    private final String actionId;
    private final List<Option> options = new ArrayList<>();
    private int gap = 3;
    private int width = -1;
    private Consumer<PeakCheckbox> checkboxStyle;

    private PeakCheckList(String id, String actionId) {
        this.id = PeakId.require(id, "Check list");
        this.actionId = actionId == null ? "" : actionId;
    }

    public static PeakCheckList create(String id, String actionId) {
        return new PeakCheckList(id, actionId);
    }

    public static PeakCheckList create(String id) {
        return new PeakCheckList(id, "");
    }

    public static String optionId(String listId, int index) {
        return listId + "/option-" + index;
    }

    public PeakCheckList option(String value, String text) {
        return option(value, text, false);
    }

    public PeakCheckList option(String value, String text, boolean checked) {
        options.add(new Option(value, text, checked));
        return this;
    }

    public PeakCheckList gap(int gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public PeakCheckList width(int width) {
        this.width = width;
        return this;
    }

    public PeakCheckList checkbox(Consumer<PeakCheckbox> customizer) {
        this.checkboxStyle = customizer;
        return this;
    }

    public PeakPanel build() {
        PeakPanel root = PeakPanel.create(id).column().gap(gap).padding(0).background(0);
        if (width > 0) root.width(width);
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            PeakCheckbox box = PeakCheckbox.create(optionId(id, i), option.text()).checked(option.checked());
            if (!actionId.isEmpty()) {
                box.onChange(PeakAction.send(actionId).arg(OPTION_ARG, option.value()));
            }
            if (checkboxStyle != null) checkboxStyle.accept(box);
            root.add(box);
        }
        return root;
    }
}
