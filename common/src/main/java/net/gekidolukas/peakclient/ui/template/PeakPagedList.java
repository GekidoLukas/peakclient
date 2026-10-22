package net.gekidolukas.peakclient.ui.template;

import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.gekidolukas.peakclient.ui.PeakSpinner;
import net.gekidolukas.peakclient.ui.patch.PeakPatch;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PeakPagedList {
    public static final String LOAD_PAGE_ARG = "page";
    public static final String PREVIOUS_KEY = "peakclient.template.paged.previous";
    public static final String NEXT_KEY = "peakclient.template.paged.next";
    public static final String LABEL_KEY = "peakclient.template.paged.label";
    public static final String LOADING_KEY = "peakclient.template.paged.loading";

    private final String id;
    private final int itemsPerPage;
    private final List<PeakElement<?>> items = new ArrayList<>();
    private int gap = 2;
    private int width = -1;
    private int navGap = 6;
    private String labelKey = LABEL_KEY;
    private Component previousText = Component.translatable(PREVIOUS_KEY);
    private Component nextText = Component.translatable(NEXT_KEY);
    private Consumer<PeakButton> navButton;
    private Consumer<PeakLabel> navLabel;
    private boolean remote;
    private int remotePages = 1;
    private Component loadingText = Component.translatable(LOADING_KEY);
    private Consumer<PeakLabel> loadingLabel;
    private Function<String, PeakElement<?>> loadingFactory;

    private PeakPagedList(String id, int itemsPerPage) {
        this.id = PeakId.require(id, "Paged list");
        this.itemsPerPage = Math.max(1, itemsPerPage);
    }

    public static PeakPagedList create(String id, int itemsPerPage) {
        return new PeakPagedList(id, itemsPerPage);
    }

    public static PeakPagedList remote(String id, int pageCount) {
        PeakPagedList list = new PeakPagedList(id, 1);
        list.remote = true;
        list.remotePages = Math.max(1, pageCount);
        return list;
    }

    public PeakPagedList add(PeakElement<?>... elements) {
        requireLocal();
        items.addAll(Arrays.asList(elements));
        return this;
    }

    public PeakPagedList addAll(Collection<? extends PeakElement<?>> elements) {
        requireLocal();
        items.addAll(elements);
        return this;
    }

    public PeakPagedList gap(int gap) {
        this.gap = Math.max(0, gap);
        return this;
    }

    public PeakPagedList width(int width) {
        this.width = width;
        return this;
    }

    public PeakPagedList navGap(int gap) {
        this.navGap = Math.max(0, gap);
        return this;
    }

    public PeakPagedList labelKey(String key) {
        this.labelKey = key == null || key.isEmpty() ? LABEL_KEY : key;
        return this;
    }

    public PeakPagedList buttonTexts(String previous, String next) {
        return buttonTexts(Component.literal(previous), Component.literal(next));
    }

    public PeakPagedList buttonTexts(Component previous, Component next) {
        this.previousText = previous;
        this.nextText = next;
        return this;
    }

    public PeakPagedList navButton(Consumer<PeakButton> customizer) {
        this.navButton = customizer;
        return this;
    }

    public PeakPagedList navLabel(Consumer<PeakLabel> customizer) {
        this.navLabel = customizer;
        return this;
    }

    public PeakPagedList pageCount(int pages) {
        if (!remote) throw new IllegalStateException("The page count of a local paged list follows its items");
        this.remotePages = Math.max(1, pages);
        return this;
    }

    public PeakPagedList loadingText(String text) {
        return loadingText(Component.literal(text == null ? "" : text));
    }

    public PeakPagedList loadingText(Component text) {
        this.loadingText = text == null ? Component.empty() : text;
        return this;
    }

    public PeakPagedList loadingLabel(Consumer<PeakLabel> customizer) {
        this.loadingLabel = customizer;
        return this;
    }

    public static String pageId(String listId, int page) {
        return listId + "/page-" + page;
    }

    public static String contentId(String listId, int page) {
        return pageId(listId, page) + "/content";
    }

    public static String loadingId(String listId, int page) {
        return pageId(listId, page) + "/loading";
    }

    public static String loadActionId(String listId) {
        return listId + "/load";
    }

    public String id() {
        return id;
    }

    public boolean isRemote() {
        return remote;
    }

    public PeakElement<?> loadingElement(int page) {
        if (loadingFactory != null) return loadingFactory.apply(loadingId(id, page));
        return loadingLabel(page);
    }

    public PeakPagedList loadingElement(Function<String, PeakElement<?>> factory) {
        this.loadingFactory = factory;
        return this;
    }

    public PeakPagedList loadingSpinner() {
        return loadingElement(elementId -> PeakSpinner.create(elementId));
    }

    public PeakLabel loadingLabel(int page) {
        PeakLabel label = PeakLabel.create(loadingId(id, page), loadingText).color(0xAAAAAA);
        if (loadingLabel != null) loadingLabel.accept(label);
        return label;
    }

    private void requireLocal() {
        if (remote) throw new IllegalStateException("A remote paged list gets its items from the server, use PeakRemotePages");
    }

    public int pageCount() {
        if (remote) return remotePages;
        return Math.max(1, (items.size() + itemsPerPage - 1) / itemsPerPage);
    }

    public PeakPanel build() {
        int pages = pageCount();
        PeakPanel root = PeakPanel.create(id).column().gap(4).padding(0).background(0);
        if (width > 0) root.width(width);

        for (int page = 0; page < pages; page++) {
            PeakPanel panel = PeakPanel.create(pageId(id, page)).column().gap(gap).padding(0).background(0);
            if (width > 0) panel.width(width);
            if (remote) {
                PeakPanel content = PeakPanel.create(contentId(id, page)).column().gap(gap).padding(0).background(0);
                if (width > 0) content.width(width);
                content.add(loadingElement(page));
                panel.add(content);
            } else {
                int from = page * itemsPerPage;
                int to = Math.min(items.size(), from + itemsPerPage);
                for (int index = from; index < to; index++) panel.add(items.get(index));
            }
            if (pages > 1) panel.add(navigation(page, pages));
            if (page != 0) panel.hidden(true);
            root.add(panel);
        }
        return root;
    }

    private PeakAction[] switchActions(int from, int to) {
        PeakAction hide = PeakAction.patch(pageId(id, from), PeakPatch.panel().hidden(true));
        PeakAction show = PeakAction.patch(pageId(id, to), PeakPatch.panel().hidden(false));
        if (!remote) return new PeakAction[]{ hide, show };
        return new PeakAction[]{ hide, show,
                PeakAction.send(loadActionId(id)).arg(LOAD_PAGE_ARG, String.valueOf(to)) };
    }

    private PeakPanel navigation(int page, int pages) {
        PeakPanel row = PeakPanel.create(id + "/nav-" + page).row().gap(navGap).padding(0).background(0);
        PeakButton previous = PeakButton.text(id + "/prev-" + page, previousText);
        PeakButton next = PeakButton.text(id + "/next-" + page, nextText);
        if (page > 0) {
            previous.onClick(switchActions(page, page - 1));
        } else {
            previous.enabled(false);
        }
        if (page < pages - 1) {
            next.onClick(switchActions(page, page + 1));
        } else {
            next.enabled(false);
        }
        if (navButton != null) {
            navButton.accept(previous);
            navButton.accept(next);
        }
        PeakLabel label = PeakLabel.create(id + "/label-" + page,
                Component.translatable(labelKey, page + 1, pages)).color(0xAAAAAA);
        if (navLabel != null) navLabel.accept(label);
        row.add(previous, label, next);
        return row;
    }
}
