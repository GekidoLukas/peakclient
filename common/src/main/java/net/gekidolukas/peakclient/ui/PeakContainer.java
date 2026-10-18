package net.gekidolukas.peakclient.ui;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class PeakContainer<SELF extends PeakContainer<SELF>> extends PeakElement<SELF> {
    protected final List<PeakElement<?>> children = new ArrayList<>();
    protected PeakLayoutDirection direction = PeakLayoutDirection.COLUMN;
    protected PeakAlign alignChildren = PeakAlign.START;
    protected PeakOverflow overflow = PeakOverflow.VISIBLE;
    protected PeakMainAxisAlign mainAxisAlign = PeakMainAxisAlign.START;
    protected int gap = 0;
    protected PeakScrollbarStyle scrollbar = new PeakScrollbarStyle();

    protected PeakContainer(String id) {
        super(id);
    }

    public List<PeakElement<?>> getChildren() { return children; }

    public SELF add(PeakElement<?> child) {
        children.add(child);
        return self();
    }

    public SELF add(PeakElement<?> first, PeakElement<?> second, PeakElement<?>... more) {
        add(first);
        add(second);
        for (PeakElement<?> child : more) add(child);
        return self();
    }

    public SELF row() { return direction(PeakLayoutDirection.ROW); }
    public SELF column() { return direction(PeakLayoutDirection.COLUMN); }
    public SELF direction(PeakLayoutDirection direction) { this.direction = direction; return self(); }
    public PeakLayoutDirection getDirection() { return direction; }

    public SELF alignChildren(PeakAlign align) { this.alignChildren = align; return self(); }
    public PeakAlign getAlignChildren() { return alignChildren; }

    public SELF overflow(PeakOverflow overflow) { this.overflow = overflow; return self(); }
    public PeakOverflow getOverflow() { return overflow; }

    public PeakMainAxisAlign getMainAxisAlign() { return mainAxisAlign; }
    public SELF mainAxisAlign(PeakMainAxisAlign align) { this.mainAxisAlign = align; return self(); }

    public int getGap() { return gap; }
    public SELF gap(int gap) { this.gap = Math.max(0, gap); return self(); }

    public PeakScrollbarStyle getScrollbar() {
        if (scrollbar == null) scrollbar = new PeakScrollbarStyle();
        return scrollbar;
    }
    public SELF scrollbar(PeakScrollbarStyle style) { this.scrollbar = style == null ? new PeakScrollbarStyle() : style; return self(); }
    public SELF scrollbar(Consumer<PeakScrollbarStyle> customizer) { customizer.accept(getScrollbar()); return self(); }
    public SELF hideScrollbar() { getScrollbar().visible(false); return self(); }

    public boolean scrollsVertically() { return direction == PeakLayoutDirection.COLUMN; }
}
