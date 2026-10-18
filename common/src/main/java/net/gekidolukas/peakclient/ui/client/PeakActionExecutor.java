package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.network.PeakMenuEventPayload;
import net.gekidolukas.peakclient.ui.PeakAction;
import net.gekidolukas.peakclient.ui.PeakActionType;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;

public final class PeakActionExecutor {
    private PeakActionExecutor() {}

    public static void run(String menuId, String elementId, List<PeakAction> actions) {
        run(PeakActionScope.menu(menuId), elementId, actions, Map.of());
    }

    public static void run(String menuId, String elementId, List<PeakAction> actions, Map<String, String> extraArguments) {
        run(PeakActionScope.menu(menuId), elementId, actions, extraArguments);
    }

    public static void run(PeakActionScope scope, String elementId, List<PeakAction> actions, Map<String, String> extraArguments) {
        for (PeakAction action : List.copyOf(actions)) {
            try {
                if (scope.menu() && action.type() == PeakActionType.SEND && action.validates()
                        && !PeakClientMenus.validateForm(scope.id(), action)) {
                    return;
                }
                execute(scope, elementId, action, extraArguments);
            } catch (Exception e) {
                PeakLog.error("Failed to run an action of " + scope.describe(), e);
            }
        }
    }

    private static void execute(PeakActionScope scope, String elementId, PeakAction action, Map<String, String> extraArguments) {
        switch (action.type()) {
            case CLOSE_MENU -> PeakClientMenus.closeAll("action");
            case BACK -> PeakClientMenus.back("action");
            case OPEN_MENU -> PeakClientMenus.open(action.target(), action.replace(), false, "action");
            case SEND -> send(scope, elementId, action, extraArguments);
            case PATCH -> {
                if (action.patch() == null) return;
                scope.apply(new PeakUiOperation.Update(action.target(), action.patch().deepCopy()));
            }
            case COPY_TEXT -> Minecraft.getInstance().keyboardHandler.setClipboard(action.target());
            case FORMAT -> {
                if (menuOnly(scope, action)) PeakClientMenus.applyFormat(scope.id(), action);
            }
            case SET_COLOR -> {
                if (menuOnly(scope, action)) PeakColorPickerLinks.setColor(scope.id(), action);
            }
            case TOGGLE -> scope.toggleHidden(action.target());
            case SOUND -> PeakSounds.play(action.soundSpec());
        }
    }

    private static void send(PeakActionScope scope, String elementId, PeakAction action, Map<String, String> extraArguments) {
        if (scope.menu()) {
            PeakClientMenus.sendAction(scope.id(), elementId, action,
                    PeakInputValues.arguments(PeakClientMenus.model(scope.id()), action, extraArguments));
            return;
        }
        PeakClientMenus.sendAction(PeakMenuEventPayload.Kind.HUD_ACTION, scope.id(), elementId, action,
                PeakInputValues.arguments(scope.model(), action, extraArguments));
    }

    private static boolean menuOnly(PeakActionScope scope, PeakAction action) {
        if (scope.menu()) return true;
        scope.warnOnce("menu-only", action.type().name(), "Action " + action.type() + " only works in menus, but ran in "
                + scope.describe() + " and was ignored");
        return false;
    }
}
