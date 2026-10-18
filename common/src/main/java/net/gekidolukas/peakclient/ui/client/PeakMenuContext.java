package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.render.PeakLayoutEntry;
import net.gekidolukas.peakclient.render.PeakLayoutProbe;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakInteraction;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakTheme;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;

import java.util.Map;

final class PeakMenuContext {
    private final Screen screen;
    private final PeakClickCounter clicks = new PeakClickCounter();
    private PeakLayoutProbe probe = new PeakLayoutProbe();
    private String shownMenuId = "";
    private PeakMenuDrag activeDrag;
    private PeakElement<?> pressed;
    private boolean doubleClickPressed;

    PeakMenuContext(Screen screen) {
        this.screen = screen;
    }

    Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    int width() {
        return screen.width;
    }

    int height() {
        return screen.height;
    }

    PeakLayoutProbe probe() {
        return probe;
    }

    void probe(PeakLayoutProbe next) {
        this.probe = next;
    }

    String shownMenuId() {
        return shownMenuId;
    }

    void shownMenuId(String menuId) {
        this.shownMenuId = menuId;
    }

    PeakMenuDrag activeDrag() {
        return activeDrag;
    }

    void beginDrag(PeakMenuDrag drag) {
        this.activeDrag = drag;
    }

    PeakMenuDrag takeDrag() {
        PeakMenuDrag drag = activeDrag;
        activeDrag = null;
        return drag;
    }

    void clearDrag() {
        if (activeDrag == null) return;
        activeDrag.cancel();
        activeDrag = null;
    }

    PeakElement<?> pressed() {
        return pressed;
    }

    void pressed(PeakElement<?> element) {
        this.pressed = element;
    }

    PeakElement<?> takePressed() {
        PeakElement<?> element = pressed;
        pressed = null;
        return element;
    }

    boolean doubleClickPressed() {
        return doubleClickPressed;
    }

    void doubleClickPressed(boolean value) {
        this.doubleClickPressed = value;
    }

    String menuId() {
        String id = PeakClientMenus.currentId();
        return id == null ? shownMenuId : id;
    }

    PeakMenuInputState input() {
        return PeakClientMenus.inputState(menuId());
    }

    PeakDocumentModel model() {
        return PeakClientMenus.model(menuId());
    }

    PeakMenuSettings settings() {
        PeakDocument menu = PeakClientMenus.currentMenu();
        return menu == null ? null : menu.getMenu();
    }

    PeakElement<?> focusedElement() {
        String focusedId = input().focusedId();
        if (focusedId == null) return null;
        PeakDocumentModel model = model();
        return model == null ? null : model.find(focusedId);
    }

    PeakLayoutEntry entryOf(PeakElement<?> element) {
        for (PeakLayoutEntry entry : probe.entries()) {
            if (entry.element() == element) return entry;
        }
        return null;
    }

    static boolean isEnabled(PeakElement<?> element) {
        PeakInteraction interaction = element.getInteraction();
        return interaction == null || interaction.isEnabled();
    }

    static float localX(PeakLayoutEntry entry, double mouseX) {
        float ratio = entry.localWidth() <= 0 ? 1f : entry.width() / (float) entry.localWidth();
        if (ratio <= 0f) ratio = 1f;
        return (float) ((mouseX - entry.absX()) / ratio);
    }

    static float localY(PeakLayoutEntry entry, double mouseY) {
        float ratio = entry.localHeight() <= 0 ? 1f : entry.height() / (float) entry.localHeight();
        if (ratio <= 0f) ratio = 1f;
        return (float) ((mouseY - entry.absY()) / ratio);
    }

    void playClick(PeakInteraction interaction) {
        PeakSounds.play(PeakTheme.clickSound(settings(), interaction));
    }

    void fireChange(PeakElement<?> element, PeakEvent event, String value) {
        fire(element, event, Map.of("value", value));
    }

    void fire(PeakElement<?> element, PeakEvent event, Map<String, String> extra) {
        PeakEventDispatch.fire(PeakActionScope.menu(menuId()), element, event, extra);
    }

    int registerClick(PeakElement<?> element, double mouseX, double mouseY) {
        return clicks.register(element.getId(), mouseX, mouseY, Util.getMillis());
    }

    int caretIndexAt(String value, float x) {
        Font font = minecraft().font;
        int previous = 0;
        for (int i = 1; i <= value.length(); i++) {
            int current = font.width(value.substring(0, i));
            if (x < current) return (x - previous < current - x) ? i - 1 : i;
            previous = current;
        }
        return value.length();
    }

    int charIndexAt(String value, float x) {
        if (x < 0) return -1;
        Font font = minecraft().font;
        for (int i = 0; i < value.length(); i++) {
            if (x < font.width(value.substring(0, i + 1))) return i;
        }
        return -1;
    }
}
