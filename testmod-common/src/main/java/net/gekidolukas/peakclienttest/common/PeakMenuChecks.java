package net.gekidolukas.peakclienttest.common;

import net.gekidolukas.peakclient.server.PeakMenuActionEvent;
import net.gekidolukas.peakclient.server.PeakMenuNavigationEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public final class PeakMenuChecks {
    private PeakMenuChecks() {}

    private static PeakMenuCheck.Outcome yesIf(boolean condition) {
        return condition ? PeakMenuCheck.Outcome.YES : PeakMenuCheck.Outcome.NO;
    }

    public static PeakMenuCheck action(String instruction, String actionId) {
        return action(instruction, actionId, event -> true);
    }

    public static PeakMenuCheck action(String instruction, String actionId, String elementId) {
        return action(instruction, actionId, event -> event.elementId().equals(elementId));
    }

    public static PeakMenuCheck action(String instruction, String actionId, Predicate<PeakMenuActionEvent> condition) {
        return new PeakMenuCheck(instruction, false, (context, event) ->
                yesIf(event instanceof PeakMenuActionEvent action
                        && action.actionId().equals(actionId) && condition.test(action)));
    }

    public static PeakMenuCheck actionCount(String instruction, String actionId, int times) {
        return new PeakMenuCheck(instruction, false, (context, event) -> {
            if (!(event instanceof PeakMenuActionEvent action) || !action.actionId().equals(actionId)) {
                return PeakMenuCheck.Outcome.NO;
            }
            return yesIf(context.increment("count:" + actionId) >= times);
        });
    }

    public static PeakMenuCheck navigated(String instruction, String menuId, String... history) {
        List<String> expectedHistory = Arrays.asList(history);
        return new PeakMenuCheck(instruction, false, (context, event) -> {
            if (!(event instanceof PeakMenuNavigationEvent navigation)) return PeakMenuCheck.Outcome.NO;
            if (menuId == null ? navigation.menuId() != null : !menuId.equals(navigation.menuId())) return PeakMenuCheck.Outcome.NO;
            return yesIf(expectedHistory.isEmpty() || expectedHistory.equals(navigation.history()));
        });
    }

    public static PeakMenuCheck navigatedBy(String instruction, String menuId, String reason) {
        return new PeakMenuCheck(instruction, false, (context, event) ->
                yesIf(event instanceof PeakMenuNavigationEvent navigation
                        && (menuId == null ? navigation.menuId() == null : menuId.equals(navigation.menuId()))
                        && reason.equals(navigation.reason())));
    }

    public static PeakMenuCheck closed(String instruction) {
        return navigated(instruction, null);
    }

    public static PeakMenuCheck manual(String instruction) {
        return new PeakMenuCheck(instruction, true, (context, event) -> PeakMenuCheck.Outcome.NO);
    }

    public static PeakMenuCheck poll(String instruction, Predicate<PeakMenuTestContext> condition) {
        return new PeakMenuCheck(instruction, false, (context, event) -> yesIf(event == null && condition.test(context)));
    }

    public static PeakMenuCheck noNavigationFor(String instruction, int ticks) {
        return new PeakMenuCheck(instruction, false, (context, event) -> {
            if (event instanceof PeakMenuNavigationEvent) return PeakMenuCheck.Outcome.FAIL;
            if (event != null) return PeakMenuCheck.Outcome.NO;
            return yesIf(context.increment("quiet:" + instruction) >= ticks);
        });
    }

    public static PeakMenuCheck waitTicks(String instruction, int ticks) {
        return new PeakMenuCheck(instruction, false, (context, event) -> {
            if (event != null) return PeakMenuCheck.Outcome.NO;
            return yesIf(context.increment("wait:" + instruction) >= ticks);
        });
    }
}
