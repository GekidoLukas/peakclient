package net.gekidolukas.peakclient.ui.client;

import java.util.List;

final class PeakMenuScrollbars {
    private final PeakMenuContext context;
    private List<PeakScrollbarRegion> regions = List.of();

    PeakMenuScrollbars(PeakMenuContext context) {
        this.context = context;
    }

    void update(List<PeakScrollbarRegion> next) {
        regions = next;
    }

    void clear() {
        regions = List.of();
    }

    PeakScrollbarRegion at(int x, int y) {
        for (int i = regions.size() - 1; i >= 0; i--) {
            PeakScrollbarRegion region = regions.get(i);
            if (region.hitsTrack(x, y) && region.visibleAt(x, y)) return region;
        }
        return null;
    }

    PeakScrollbarRegion of(String ownerId) {
        for (int i = regions.size() - 1; i >= 0; i--) {
            if (regions.get(i).ownerId().equals(ownerId)) return regions.get(i);
        }
        return null;
    }

    PeakMenuDrag begin(PeakScrollbarRegion region, double mouseX, double mouseY) {
        int axisPosition = region.axis((int) mouseX, (int) mouseY);
        ScrollbarDrag drag;
        if (region.thumbContainsAxis(axisPosition)) {
            drag = new ScrollbarDrag(region.ownerId(), axisPosition - region.thumbStart());
        } else {
            drag = new ScrollbarDrag(region.ownerId(), region.thumbLength() / 2);
            drag.apply(region, mouseX, mouseY);
        }
        return drag;
    }

    private final class ScrollbarDrag implements PeakMenuDrag {
        private final String ownerId;
        private final int grab;

        ScrollbarDrag(String ownerId, int grab) {
            this.ownerId = ownerId;
            this.grab = grab;
        }

        void apply(PeakScrollbarRegion region, double mouseX, double mouseY) {
            int axisPosition = region.axis((int) mouseX, (int) mouseY);
            int offset = region.offsetForThumbStart(axisPosition - grab);
            PeakClientMenus.scrollState(context.menuId()).set(region.ownerId(), offset);
        }

        @Override
        public void dragged(double mouseX, double mouseY) {
            PeakScrollbarRegion region = of(ownerId);
            if (region != null) apply(region, mouseX, mouseY);
        }

        @Override
        public void released(double mouseX, double mouseY) {}

        @Override
        public String scrollbarOwner() {
            return ownerId;
        }

        @Override
        public boolean silencesHoverSound() {
            return true;
        }
    }
}
