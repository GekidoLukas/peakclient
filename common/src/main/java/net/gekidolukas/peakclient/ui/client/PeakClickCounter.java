package net.gekidolukas.peakclient.ui.client;

final class PeakClickCounter {
    static final long WINDOW_MILLIS = 300;
    static final int TOLERANCE_PIXELS = 4;
    static final int MAX_COUNT = 3;

    private String lastElementId = "";
    private long lastMillis;
    private int lastX;
    private int lastY;
    private int count;

    int register(String elementId, double mouseX, double mouseY, long nowMillis) {
        boolean repeat = elementId.equals(lastElementId) && nowMillis - lastMillis <= WINDOW_MILLIS
                && Math.abs(mouseX - lastX) <= TOLERANCE_PIXELS && Math.abs(mouseY - lastY) <= TOLERANCE_PIXELS;
        count = repeat ? Math.min(MAX_COUNT, count + 1) : 1;
        lastElementId = elementId;
        lastMillis = nowMillis;
        lastX = (int) mouseX;
        lastY = (int) mouseY;
        return count;
    }
}
