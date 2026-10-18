package net.gekidolukas.peakclient.ui.client;

import com.google.gson.JsonObject;
import net.gekidolukas.peakclient.PeakClientNetworking;
import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.network.PeakMenuEventPayload;
import net.gekidolukas.peakclient.network.PeakMenuPayload;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakFormat;
import net.gekidolukas.peakclient.ui.PeakMenuEscape;
import net.gekidolukas.peakclient.ui.PeakMenuSettings;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.serialize.PeakGzip;
import net.gekidolukas.peakclient.ui.serialize.PeakUiSerializer;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PeakClientMenus {
    private static final long PENDING_OPEN_TIMEOUT_TICKS = 200;

    private static final PeakClientMenuState STATE = new PeakClientMenuState();
    private static final PeakMenuNavigator NAVIGATOR = new PeakMenuNavigator();
    private static final Map<String, PeakMenuScrollState> SCROLLS = new HashMap<>();
    private static final Map<String, PeakMenuInputState> INPUTS = new HashMap<>();
    private static boolean pendingOpen;
    private static long pendingSince;

    private PeakClientMenus() {}

    public static void receive(PeakMenuPayload payload) {
        String menuId = payload.menuId();
        switch (payload.kind()) {
            case SEND -> {
                String json = PeakGzip.decompress(payload.gzippedJson());
                PeakDocument menu = PeakUiSerializer.documentFromJson(json);
                if (!menu.isMenu()) menu.menu(PeakMenuSettings.create());
                STATE.put(menu, NAVIGATOR.history());
            }
            case UPDATE -> {
                String json = PeakGzip.decompress(payload.gzippedJson());
                List<PeakUiOperation> operations = PeakUiSerializer.operationsFromJson(json);
                STATE.applyOperations(menuId, operations);
            }
            case OPEN -> open(menuId,
                    (payload.flags() & PeakMenuPayload.FLAG_REPLACE) != 0,
                    (payload.flags() & PeakMenuPayload.FLAG_FORCE) != 0, "server");
            case CLOSE -> closeAll("server");
            case BACK -> back("server");
            case REMOVE -> removeMenu(menuId);
            case CLEAR -> {
                STATE.clear();
                SCROLLS.clear();
                INPUTS.clear();
                closeAll("server");
            }
        }
    }

    public static PeakMenuScrollState scrollState(String menuId) {
        return SCROLLS.computeIfAbsent(menuId, id -> new PeakMenuScrollState());
    }

    public static PeakDocument currentMenu() {
        String current = NAVIGATOR.current();
        return current == null ? null : STATE.get(current);
    }

    public static String currentId() {
        return NAVIGATOR.current();
    }

    public static boolean isOpen() {
        return NAVIGATOR.isOpen();
    }

    public static List<String> history() {
        return NAVIGATOR.history();
    }

    public static void open(String menuId, boolean replace, boolean force, String reason) {
        if (!STATE.contains(menuId)) {
            PeakLog.warn("Cannot open menu '{}': it was never sent to this client", menuId);
            return;
        }
        String before = NAVIGATOR.current();
        List<String> historyBefore = NAVIGATOR.history();
        NAVIGATOR.open(menuId, replace);
        afterNavigation(before, historyBefore, force, reason);
    }

    public static void back(String reason) {
        String before = NAVIGATOR.current();
        List<String> historyBefore = NAVIGATOR.history();
        NAVIGATOR.back();
        afterNavigation(before, historyBefore, false, reason);
    }

    public static void closeAll(String reason) {
        if (!NAVIGATOR.isOpen()) {
            pendingOpen = false;
            return;
        }
        String before = NAVIGATOR.current();
        List<String> historyBefore = NAVIGATOR.history();
        NAVIGATOR.closeAll();
        afterNavigation(before, historyBefore, false, reason);
    }

    public static void removeMenu(String menuId) {
        STATE.remove(menuId);
        SCROLLS.remove(menuId);
        INPUTS.remove(menuId);
        if (!NAVIGATOR.contains(menuId)) return;
        String before = NAVIGATOR.current();
        List<String> historyBefore = NAVIGATOR.history();
        NAVIGATOR.remove(menuId);
        afterNavigation(before, historyBefore, false, "remove");
    }

    public static void onEscape() {
        PeakDocument menu = currentMenu();
        PeakMenuEscape escape = menu != null && menu.getMenu() != null ? menu.getMenu().getEscape() : PeakMenuEscape.CLOSE_ALL;
        switch (escape) {
            case CLOSE_ALL -> closeAll("escape");
            case BACK -> back("escape");
            case NONE -> { }
        }
    }

    public static void runActions(String menuId, String elementId, List<PeakAction> actions) {
        PeakActionExecutor.run(menuId, elementId, actions);
    }

    public static void runActions(String menuId, String elementId, List<PeakAction> actions, Map<String, String> extraArguments) {
        PeakActionExecutor.run(menuId, elementId, actions, extraArguments);
    }

    public static PeakMenuInputState inputState(String menuId) {
        return INPUTS.computeIfAbsent(menuId, id -> new PeakMenuInputState());
    }

    public static PeakDocumentModel model(String menuId) {
        return STATE.model(menuId);
    }

    private static final Set<String> WARNED_FORMAT_TARGETS = new HashSet<>();

    static void applyFormat(String menuId, PeakAction action) {
        PeakDocumentModel model = model(menuId);
        PeakElement<?> target = model == null ? null : model.find(action.target());
        if (!(target instanceof PeakRichTextBox box)) {
            if (WARNED_FORMAT_TARGETS.add(menuId + "/" + action.target())) {
                PeakLog.warn("Format action in menu '{}' targets '{}', which is not a rich text box", menuId, action.target());
            }
            return;
        }
        PeakFormat kind = action.formatKind();
        if (kind == null || !box.isFormatting()) return;

        PeakMenuInputState input = inputState(menuId);
        PeakRichTextEditor editor = input.richEditor(box);
        boolean changed = switch (kind) {
            case BOLD -> editor.toggleFlag(PeakRichText.BOLD);
            case ITALIC -> editor.toggleFlag(PeakRichText.ITALIC);
            case UNDERLINE -> editor.toggleFlag(PeakRichText.UNDERLINE);
            case STRIKE -> editor.toggleFlag(PeakRichText.STRIKE);
            case COLOR_NEXT -> editor.cycleColor(box.getColors(), false);
            case COLOR_PREVIOUS -> editor.cycleColor(box.getColors(), true);
            case COLOR -> editor.applyColor(action.formatColorValue());
            case CLEAR -> editor.clearFormat();
        };
        if (changed) input.commit(box, editor);
    }

    static boolean validateForm(String menuId, PeakAction action) {
        PeakDocumentModel model = model(menuId);
        if (model == null) return true;
        PeakFormValidation.Result result = PeakFormValidation.validate(model, action);
        for (String id : result.passed()) setInvalid(menuId, model, id, false);
        for (String id : result.failed()) setInvalid(menuId, model, id, true);
        return result.failed().isEmpty();
    }

    private static void setInvalid(String menuId, PeakDocumentModel model, String elementId,
                                   boolean invalid) {
        PeakElement<?> element = model.find(elementId);
        if (element == null) return;
        JsonObject patch = new JsonObject();
        patch.addProperty("interaction.invalid", invalid);
        applyLocalOperation(menuId, new PeakUiOperation.Update(elementId, patch));

        String errorId = element.getInteraction() == null ? "" : element.getInteraction().getErrorElement();
        if (errorId.isEmpty() || model.find(errorId) == null) return;
        JsonObject label = new JsonObject();
        label.addProperty("hidden", !invalid);
        applyLocalOperation(menuId, new PeakUiOperation.Update(errorId, label));
    }

    private static final Set<String> WARNED_TOGGLE_TARGETS = new HashSet<>();

    static void toggleHidden(String menuId, String elementId) {
        PeakDocumentModel model = model(menuId);
        PeakElement<?> target = model == null ? null : model.find(elementId);
        if (target == null) {
            if (WARNED_TOGGLE_TARGETS.add(menuId + "/" + elementId)) {
                PeakLog.warn("Toggle action in menu '{}' targets '{}', which does not exist", menuId, elementId);
            }
            return;
        }
        JsonObject patch = new JsonObject();
        patch.addProperty("hidden", !target.isHidden());
        applyLocalOperation(menuId, new PeakUiOperation.Update(elementId, patch));
    }

    static boolean applyLocalOperation(String menuId, PeakUiOperation operation) {
        return STATE.applyOperations(menuId, List.of(operation));
    }

    public static void onScreenRemoved() {
        if (!NAVIGATOR.isOpen()) return;
        closeAll("screen");
    }

    public static void tick() {
        if (!pendingOpen) return;
        if (!NAVIGATOR.isOpen()) {
            pendingOpen = false;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null || mc.screen instanceof PeakMenuScreen) {
            pendingOpen = false;
            showScreen(mc);
            return;
        }
        if (PeakClientTickCounter.current() - pendingSince > PENDING_OPEN_TIMEOUT_TICKS) {
            pendingOpen = false;
            PeakLog.warn("Menu '{}' could not be opened because another screen stayed open", NAVIGATOR.current());
            closeAll("timeout");
        }
    }

    public static void clearAll() {
        STATE.clear();
        SCROLLS.clear();
        INPUTS.clear();
        NAVIGATOR.closeAll();
        PeakPanoramas.clear();
        PeakColorPickerLinks.clearWarnings();
        pendingOpen = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.screen instanceof PeakMenuScreen) mc.setScreen(null);
    }

    private static void afterNavigation(String before, List<String> historyBefore, boolean force, String reason) {
        String current = NAVIGATOR.current();
        if (current == null) {
            pendingOpen = false;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof PeakMenuScreen) mc.setScreen(null);
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof PeakMenuScreen) {
                pendingOpen = false;
            } else if (mc.screen == null || force) {
                pendingOpen = false;
                showScreen(mc);
            } else {
                pendingOpen = true;
                pendingSince = PeakClientTickCounter.current();
            }
        }
        List<String> historyNow = NAVIGATOR.history();
        boolean changed = current == null ? before != null : !current.equals(before) || !historyNow.equals(historyBefore);
        if (changed) sendNavigation(current, historyNow, reason);
    }

    private static void showScreen(Minecraft mc) {
        if (!(mc.screen instanceof PeakMenuScreen)) mc.setScreen(new PeakMenuScreen());
    }

    private static void sendNavigation(String current, List<String> history, String reason) {
        if (!PeakClientNetworking.canSendToServer(PeakMenuEventPayload.TYPE)) return;
        String json = PeakUiSerializer.GSON.toJson(history);
        PeakClientNetworking.sendToServer(new PeakMenuEventPayload(PeakMenuEventPayload.Kind.NAVIGATED,
                current == null ? "" : current, "", reason, json));
    }

    static void sendAction(String menuId, String elementId, PeakAction action, Map<String, String> arguments) {
        sendAction(PeakMenuEventPayload.Kind.ACTION, menuId, elementId, action, arguments);
    }

    static void sendAction(PeakMenuEventPayload.Kind kind, String menuId, String elementId, PeakAction action,
                           Map<String, String> arguments) {
        if (!PeakClientNetworking.canSendToServer(PeakMenuEventPayload.TYPE)) return;
        if (arguments.size() > PeakAction.MAX_ARGUMENTS) {
            PeakLog.warn("Action '{}' of menu '{}' was not sent: it has {} arguments, the limit is {}",
                    action.target(), menuId, arguments.size(), PeakAction.MAX_ARGUMENTS);
            return;
        }
        String json = PeakUiSerializer.GSON.toJson(arguments);
        if (json.length() > PeakMenuEventPayload.MAX_ARGS_LENGTH) {
            PeakLog.warn("Action '{}' of menu '{}' was not sent: its arguments exceed {} characters",
                    action.target(), menuId, PeakMenuEventPayload.MAX_ARGS_LENGTH);
            return;
        }
        PeakClientNetworking.sendToServer(new PeakMenuEventPayload(kind,
                menuId, elementId, action.target(), json));
    }
}
