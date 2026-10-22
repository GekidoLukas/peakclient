package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.server.PeakMenuEvents;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakAnchor;
import net.gekidolukas.peakclient.ui.PeakButton;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakId;
import net.gekidolukas.peakclient.ui.PeakLabel;
import net.gekidolukas.peakclient.ui.PeakMenu;
import net.gekidolukas.peakclient.ui.PeakMenuEscape;
import net.gekidolukas.peakclient.ui.PeakPanel;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class PeakConfirmDialog {
    public static final String CONFIRM_ACTION = "confirm";
    public static final String CANCEL_ACTION = "cancel";
    public static final String TITLE_KEY = "peakclient.template.confirm.title";
    public static final String CONFIRM_KEY = "peakclient.template.confirm.confirm";
    public static final String CANCEL_KEY = "peakclient.template.confirm.cancel";

    private final String id;
    private Component title = Component.translatable(TITLE_KEY);
    private Component message = Component.empty();
    private Component confirmText = Component.translatable(CONFIRM_KEY);
    private Component cancelText = Component.translatable(CANCEL_KEY);
    private int width = 220;
    private boolean backAfterAnswer = true;
    private final Map<String, String> arguments = new LinkedHashMap<>();
    private final List<PeakAction> confirmActions = new ArrayList<>();
    private final List<PeakAction> cancelActions = new ArrayList<>();
    private Consumer<PeakPanel> panelCustomizer;
    private Consumer<PeakButton> confirmCustomizer;
    private Consumer<PeakButton> cancelCustomizer;

    private PeakConfirmDialog(String id) {
        this.id = PeakId.require(id, "Dialog");
    }

    public static PeakConfirmDialog create(String id) {
        return new PeakConfirmDialog(id);
    }

    public String id() { return id; }

    public PeakConfirmDialog title(String title) { return title(Component.literal(title)); }
    public PeakConfirmDialog title(Component title) { this.title = title; return this; }
    public PeakConfirmDialog message(String message) { return message(Component.literal(message)); }
    public PeakConfirmDialog message(Component message) { this.message = message; return this; }
    public PeakConfirmDialog confirmText(String text) { return confirmText(Component.literal(text)); }
    public PeakConfirmDialog confirmText(Component text) { this.confirmText = text; return this; }
    public PeakConfirmDialog cancelText(String text) { return cancelText(Component.literal(text)); }
    public PeakConfirmDialog cancelText(Component text) { this.cancelText = text; return this; }
    public PeakConfirmDialog width(int width) { this.width = Math.max(120, width); return this; }
    public PeakConfirmDialog backAfterAnswer(boolean back) { this.backAfterAnswer = back; return this; }
    public PeakConfirmDialog arg(String key, String value) { arguments.put(key, value); return this; }
    public PeakConfirmDialog onConfirmClient(PeakAction... actions) { confirmActions.addAll(List.of(actions)); return this; }
    public PeakConfirmDialog onCancelClient(PeakAction... actions) { cancelActions.addAll(List.of(actions)); return this; }

    public PeakConfirmDialog panel(Consumer<PeakPanel> customizer) { this.panelCustomizer = customizer; return this; }
    public PeakConfirmDialog confirmButton(Consumer<PeakButton> customizer) { this.confirmCustomizer = customizer; return this; }
    public PeakConfirmDialog cancelButton(Consumer<PeakButton> customizer) { this.cancelCustomizer = customizer; return this; }

    public PeakConfirmDialog onConfirm(Consumer<PeakMenuActionEvent> handler) {
        PeakMenuEvents.set(id, CONFIRM_ACTION, handler);
        return this;
    }

    public PeakConfirmDialog onCancel(Consumer<PeakMenuActionEvent> handler) {
        PeakMenuEvents.set(id, CANCEL_ACTION, handler);
        return this;
    }

    public PeakDocument build() {
        PeakDocument menu = PeakMenu.create(id, settings -> settings.title(title).escape(PeakMenuEscape.BACK));

        PeakPanel panel = PeakPanel.create(id + "/panel")
                .absolute(PeakAnchor.CENTER, 0, 0)
                .column().gap(8).padding(10).width(width).borderRadius(5)
                .background(0xFF202030).border(1, 0xFF888888);
        panel.add(PeakLabel.create(id + "/title", title).color(0xFFD700));
        if (!message.getString().isEmpty()) {
            panel.add(PeakLabel.create(id + "/message", message).color(0xDDDDDD).wrapWidth(width - 22).lineSpacing(2));
        }

        PeakPanel row = PeakPanel.create(id + "/buttons").row().gap(6).padding(0).background(0);
        PeakButton confirm = answerButton(id + "/confirm", confirmText, CONFIRM_ACTION, confirmActions, 0xFF2E7D32);
        PeakButton cancel = answerButton(id + "/cancel", cancelText, CANCEL_ACTION, cancelActions, PeakButton.DEFAULT_BACKGROUND);
        if (confirmCustomizer != null) confirmCustomizer.accept(confirm);
        if (cancelCustomizer != null) cancelCustomizer.accept(cancel);
        row.add(confirm, cancel);
        panel.add(row);

        if (panelCustomizer != null) panelCustomizer.accept(panel);
        menu.add(panel);
        return menu;
    }

    public PeakMenus show(PeakTargets targets) {
        return targets.menus().sendAndOpen(build());
    }

    private PeakButton answerButton(String buttonId, Component text, String actionId, List<PeakAction> extra, int background) {
        List<PeakAction> actions = new ArrayList<>();
        actions.add(PeakAction.send(actionId, arguments));
        actions.addAll(extra);
        if (backAfterAnswer) actions.add(PeakAction.back());
        PeakButton button = PeakButton.text(buttonId, text).onClick(actions.toArray(new PeakAction[0]));
        button.background(background);
        return button;
    }
}
