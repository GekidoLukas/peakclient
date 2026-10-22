package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class PeakCollapsible {
    private final String id;
    private final Component title;
    private final List<PeakElement<?>> content = new ArrayList<>();
    private boolean open;
    private int width = -1;
    private int gap = 4;
    private int indent = 10;
    private Consumer<PeakButton> headerStyle;

    private PeakCollapsible(String id, Component title) {
        this.id = PeakId.require(id, "Collapsible");
        this.title = title == null ? Component.empty() : title;
    }

    public static PeakCollapsible create(String id, String title) {
        return new PeakCollapsible(id, Component.literal(title == null ? "" : title));
    }

    public static PeakCollapsible create(String id, Component title) {
        return new PeakCollapsible(id, title);
    }

    public static String headerId(String id) { return id + "/header"; }
    public static String contentId(String id) { return id + "/content"; }
    public static String openArrowId(String id) { return id + "/arrow-open"; }
    public static String closedArrowId(String id) { return id + "/arrow-closed"; }

    public PeakCollapsible add(PeakElement<?>... elements) {
        content.addAll(Arrays.asList(elements));
        return this;
    }

    public PeakCollapsible open(boolean open) {
        this.open = open;
        return this;
    }

    public PeakCollapsible width(int width) {
        this.width = width;
        return this;
    }

    public PeakCollapsible gap(int gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public PeakCollapsible indent(int indent) {
        this.indent = Math.max(0, indent);
        return this;
    }

    public PeakCollapsible headerStyle(Consumer<PeakButton> customizer) {
        this.headerStyle = customizer;
        return this;
    }

    public PeakPanel build() {
        PeakPanel root = PeakPanel.create(id).column().gap(gap).padding(0).background(0);
        if (width > 0) root.width(width);

        PeakLabel arrowClosed = PeakLabel.create(closedArrowId(id), ">").color(0xAAAAAA);
        PeakLabel arrowOpen = PeakLabel.create(openArrowId(id), "v").color(0xAAAAAA);
        arrowClosed.hidden(open);
        arrowOpen.hidden(!open);

        PeakButton header = PeakButton.create(headerId(id));
        header.row().gap(6);
        if (width > 0) header.width(width);
        header.add(arrowClosed, arrowOpen, PeakLabel.create(id + "/title", title));
        header.onClick(
                PeakAction.toggle(contentId(id)),
                PeakAction.toggle(openArrowId(id)),
                PeakAction.toggle(closedArrowId(id)));
        if (headerStyle != null) headerStyle.accept(header);

        PeakPanel body = PeakPanel.create(contentId(id)).column().gap(gap).padding(0, 0, 0, indent).background(0);
        for (PeakElement<?> element : content) body.add(element);
        body.hidden(!open);

        root.add(header, body);
        return root;
    }
}
