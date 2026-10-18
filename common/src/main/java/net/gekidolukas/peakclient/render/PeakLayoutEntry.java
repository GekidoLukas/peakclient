package net.gekidolukas.peakclient.render;

import net.gekidolukas.peakclient.ui.PeakElement;

public record PeakLayoutEntry(String documentId, String elementId, String elementType,
                              int width, int height, int absX, int absY, int depth,
                              PeakElement<?> element, int parentIndex,
                              int localWidth, int localHeight, float absScale,
                              int percentBaseWidth, int percentBaseHeight, boolean invisible,
                              int clipLeft, int clipTop, int clipRight, int clipBottom) {

    public static final int NO_CLIP_MIN = Integer.MIN_VALUE;
    public static final int NO_CLIP_MAX = Integer.MAX_VALUE;

    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= absX && mouseX <= absX + width
                && mouseY >= absY && mouseY <= absY + height;
    }

    public boolean isClippedAt(int mouseX, int mouseY) {
        return mouseX < clipLeft || mouseX >= clipRight || mouseY < clipTop || mouseY >= clipBottom;
    }

    public boolean containsVisible(int mouseX, int mouseY) {
        return !invisible && contains(mouseX, mouseY) && !isClippedAt(mouseX, mouseY);
    }

    public String key() {
        return documentId + "/" + elementId;
    }

    PeakLayoutEntry withParent(int parent) {
        return new PeakLayoutEntry(documentId, elementId, elementType, width, height, absX, absY, depth,
                element, parent, localWidth, localHeight, absScale, percentBaseWidth, percentBaseHeight, invisible,
                clipLeft, clipTop, clipRight, clipBottom);
    }
}
