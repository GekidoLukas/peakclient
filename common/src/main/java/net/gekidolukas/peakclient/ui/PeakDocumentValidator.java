package net.gekidolukas.peakclient.ui;

import net.gekidolukas.peakclient.PeakLog;
import net.gekidolukas.peakclient.network.PeakProtocol;
import net.gekidolukas.peakclient.ui.serialize.PeakPaintAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class PeakDocumentValidator {
    private static final Pattern BINDING_PLACEHOLDER = Pattern.compile("\\{([\\w.]+):([\\w./-]+)(?:=([^#}]+))?(?:#([^}]+))?}");

    private static final double WARN_FRACTION = 0.8;

    private PeakDocumentValidator() {}

    public static boolean exceedsPayloadLimit(int compressedBytes) {
        return compressedBytes > PeakProtocol.MAX_PAYLOAD_BYTES;
    }

    public static ValidationResult validate(PeakDocument document, int serializedJsonLength, int compressedBytes) {
        List<String> issues = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        checkDuplicateIds(document, seenIds, issues);
        checkReferences(document, issues);
        checkWorldAnchors(document, document, false, issues);

        if (document.isMenu() && !document.getHiddenVanilla().isEmpty()) {
            issues.add("Menu '" + document.getId() + "' sets hideVanilla(...), which has no effect on menus. "
                    + "Use the vanilla HUD API (PeakClient.to(player).vanillaHud()) while the menu is open instead.");
        }
        if (document.isMenu() && document.getDocumentTimeToLiveTicks() > 0) {
            issues.add("Menu '" + document.getId() + "' sets documentTimeToLiveTicks, which has no effect on menus. "
                    + "Menus stay cached until they are removed or the player disconnects.");
        }

        if (compressedBytes > PeakProtocol.MAX_PAYLOAD_BYTES * WARN_FRACTION) {
            issues.add("Document '" + document.getId() + "' is very large (" + compressedBytes + " bytes compressed, "
                    + serializedJsonLength + " chars uncompressed) - approaching the payload size limit of "
                    + PeakProtocol.MAX_PAYLOAD_BYTES + " bytes. Consider splitting it into multiple documents"
                    + " or using update operations instead of full resends.");
        }

        return new ValidationResult(issues);
    }

    public static ValidationResult validateOperations(String documentId, List<PeakUiOperation> operations,
                                                      PeakDocument knownDocument) {
        List<String> issues = new ArrayList<>();
        Set<String> knownIds = knownDocument != null ? collectIds(knownDocument) : null;

        for (PeakUiOperation operation : operations) {
            if (operation instanceof PeakUiOperation.Update update) {
                if (knownIds != null && !knownIds.contains(update.elementId())) {
                    issues.add("Update operation targets element '" + update.elementId()
                            + "' which does not exist in document '" + documentId + "' (as last known on the server).");
                } else if (knownDocument != null) {
                    PeakElement<?> target = findElement(knownDocument, update.elementId());
                    if (target != null) {
                        for (String key : update.patch().keySet()) {
                            if (!target.isPatchKeyKnown(key)) {
                                issues.add("Update for element '" + update.elementId() + "' uses key '" + key
                                        + "' which does not exist on " + target.getClass().getSimpleName() + ".");
                            }
                        }
                    }
                }
            } else if (operation instanceof PeakUiOperation.Delete delete) {
                if (knownIds != null && !knownIds.contains(delete.elementId())) {
                    issues.add("Delete operation targets element '" + delete.elementId()
                            + "' which does not exist in document '" + documentId + "' (as last known on the server).");
                }
            } else if (operation instanceof PeakUiOperation.Add add) {
                if (knownIds != null && !knownIds.contains(add.parentId())) {
                    issues.add("Add operation references unknown parent '" + add.parentId()
                            + "' in document '" + documentId + "'.");
                }
                if (knownIds != null && knownIds.contains(add.element().getId())) {
                    issues.add("Add operation introduces element id '" + add.element().getId()
                            + "' which already exists in document '" + documentId + "'.");
                }
            }
        }

        return new ValidationResult(issues);
    }

    public static void validateAndLog(PeakDocument document, int serializedJsonLength, int compressedBytes) {
        ValidationResult result = validate(document, serializedJsonLength, compressedBytes);
        for (String issue : result.issues()) {
            PeakLog.warn("Document validation: {}", issue);
        }
    }

    public static void validateOperationsAndLog(String documentId, List<PeakUiOperation> operations, PeakDocument knownDocument) {
        ValidationResult result = validateOperations(documentId, operations, knownDocument);
        for (String issue : result.issues()) {
            PeakLog.warn("Operation validation: {}", issue);
        }
    }

    private static String paintProblem(String expression) {
        try {
            PeakPaintAdapter.parse(BINDING_PLACEHOLDER.matcher(expression).replaceAll("#FFFFFF"));
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private static void checkWorldAnchors(PeakElement<?> element, PeakDocument document, boolean unreliableParent, List<String> issues) {
        if (element instanceof PeakWorldAnchor anchor) {
            String where = "World anchor '" + anchor.getId() + "'";
            if (document.isMenu()) {
                issues.add(where + " is in a menu; menus are drawn without the world and never show anchors.");
            }
            if (unreliableParent) {
                issues.add(where + " sits inside a rotated, scrolling or animated popup container; its screen position will be wrong. "
                        + "Put it directly under the document.");
            }
            PeakAnchorTarget target = anchor.getTarget();
            if (target.getKind() == PeakAnchorTarget.Kind.ENTITY && target.parsedEntityUuid() == null) {
                issues.add(where + " follows an entity but its uuid '" + target.getEntityUuid() + "' is not valid.");
            }
            if (target.getKind() == PeakAnchorTarget.Kind.WAYPOINT && target.getWaypointId().isEmpty()) {
                issues.add(where + " follows a waypoint but has no waypoint id.");
            }
            if (anchor.getMaxDistance() > 0f && anchor.getMinDistance() > anchor.getMaxDistance()) {
                issues.add(where + " has a minimum distance greater than its maximum distance and will never show.");
            }
        }
        if (element instanceof PeakContainer<?> container) {
            boolean unreliable = unreliableParent || (element != document && (element.getRotationDegrees() != 0f
                    || container.getOverflow() == PeakOverflow.SCROLL || element instanceof PeakPopUpContainer));
            for (PeakElement<?> child : container.getChildren()) {
                checkWorldAnchors(child, document, unreliable, issues);
            }
        }
    }

    private static void checkDuplicateIds(PeakElement<?> element, Set<String> seen, List<String> issues) {
        if (!seen.add(element.getId())) {
            issues.add("Duplicate element id '" + element.getId() + "' found in document.");
        }
        element.getBindings().forEach((field, expression) -> {
            if (field == null || field.isBlank()) {
                issues.add("Element '" + element.getId() + "' has a binding with an empty field name.");
            } else if (expression == null || !BINDING_PLACEHOLDER.matcher(expression).find()) {
                issues.add("Binding '" + field + "' on element '" + element.getId() + "' contains no placeholder and has no effect.");
            } else if (!element.isPatchKeyKnown(field.split("\\.")[0])) {
                issues.add("Binding '" + field + "' on element '" + element.getId() + "' refers to a field that does not exist on "
                        + element.getClass().getSimpleName() + ".");
            } else if (PeakFieldTable.isPaintPath(element.getClass(), field) && paintProblem(expression) != null) {
                issues.add("Binding '" + field + "' on element '" + element.getId() + "' is not a valid paint: "
                        + paintProblem(expression));
            }
        });
        if (element instanceof PeakCarousel carousel) {
            if (carousel.getSpeed() <= 0f) {
                issues.add("Carousel '" + carousel.getId() + "' has a speed of " + carousel.getSpeed() + " and will not move.");
            }
            PeakSize.PeakDimension axis = carousel.getMovement().isHorizontal()
                    ? carousel.getSize().getWidth() : carousel.getSize().getHeight();
            if (axis.mode() == PeakSize.PeakDimension.Mode.AUTO) {
                issues.add("Carousel '" + carousel.getId() + "' has no fixed "
                        + (carousel.getMovement().isHorizontal() ? "width" : "height") + " and shows its whole content.");
            }
        }
        if (element instanceof PeakTableLayoutPanel table) {
            int columns = table.getColumnCount();
            for (PeakElement<?> child : table.getChildren()) {
                if (child.getColumnSpan() > columns) {
                    issues.add("Cell '" + child.getId() + "' in table '" + table.getId() + "' spans "
                            + child.getColumnSpan() + " columns but the table has " + columns + "; the span is clamped.");
                }
            }
        }
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) {
                checkDuplicateIds(child, seen, issues);
            }
        }
    }

    private static void checkReferences(PeakDocument document, List<String> issues) {
        Map<String, PeakElement<?>> byId = new HashMap<>();
        collectElements(document, byId);

        for (PeakElement<?> element : byId.values()) {
            checkEvents(element, document, issues);
            if (element instanceof PeakColorPicker picker) {
                if (!picker.getTarget().isEmpty() && !(byId.get(picker.getTarget()) instanceof PeakRichTextBox)) {
                    issues.add("Color picker '" + picker.getId() + "' has the target '" + picker.getTarget()
                            + "', which is not a rich text box in menu '" + document.getId() + "'.");
                }
                if (!picker.getHexField().isEmpty() && !(byId.get(picker.getHexField()) instanceof PeakTextBox)) {
                    issues.add("Color picker '" + picker.getId() + "' has the hex field '" + picker.getHexField()
                            + "', which is not a text box in menu '" + document.getId() + "'.");
                }
            }
            for (PeakAction action : actionsOf(element)) {
                checkAction(element.getId(), action, byId, document, issues);
            }
        }
        if (document.getMenu() != null) {
            for (PeakShortcut shortcut : document.getMenu().getShortcuts()) {
                for (PeakAction action : shortcut.getActions()) {
                    checkAction("shortcut:" + shortcut.spec(), action, byId, document, issues);
                }
            }
        }
    }

    private static void checkAction(String ownerId, PeakAction action, Map<String, PeakElement<?>> byId,
                                    PeakDocument document, List<String> issues) {
        PeakActionType type = action.type();
        if (!document.isMenu() && (type == PeakActionType.FORMAT || type == PeakActionType.SET_COLOR)) {
            issues.add("Action " + type + " of '" + ownerId + "' only works in menus, but " + describe(document) + " is a HUD document.");
            return;
        }
        if (type != PeakActionType.PATCH && type != PeakActionType.TOGGLE
                && type != PeakActionType.FORMAT && type != PeakActionType.SET_COLOR) return;

        PeakElement<?> target = byId.get(action.target());
        String where = "Action " + type + " of '" + ownerId + "' targets '" + action.target() + "'";
        if (target == null) {
            issues.add(where + ", which does not exist in " + describe(document) + ".");
        } else if (type == PeakActionType.FORMAT && !(target instanceof PeakRichTextBox)) {
            issues.add(where + ", which is not a rich text box.");
        } else if (type == PeakActionType.SET_COLOR && !(target instanceof PeakColorPicker)) {
            issues.add(where + ", which is not a color picker.");
        } else if (type == PeakActionType.PATCH && action.patch() != null && action.patch().has("trigger")
                && !(target instanceof PeakAnimated)) {
            issues.add(where + ", which is not an animated container and cannot run a trigger.");
        }
    }

    private static String describe(PeakDocument document) {
        return (document.isMenu() ? "menu '" : "document '") + document.getId() + "'";
    }

    private static void checkEvents(PeakElement<?> element, PeakDocument document, List<String> issues) {
        for (PeakReaction reaction : element.getEvents()) {
            PeakEvent event = reaction.getOn();
            String where = "Event " + event + " of '" + element.getId() + "'";
            if (reaction.getActions().isEmpty()) {
                issues.add(where + " has no actions.");
            }
            if (event.needsMenu() && !document.isMenu()) {
                issues.add(where + " only fires in menus, but " + describe(document) + " is a HUD document.");
            }
            if (event.isData()) {
                if (reaction.getWatch().isEmpty() && !PeakWatchable.hasDefaultValue(element)) {
                    issues.add(where + " has no watch expression and " + element.getClass().getSimpleName()
                            + " has no default value to observe.");
                } else if (!reaction.getWatch().isEmpty() && !BINDING_PLACEHOLDER.matcher(reaction.getWatch()).find()) {
                    issues.add(where + " watches '" + reaction.getWatch() + "', which contains no placeholder and never changes.");
                }
            }
            if (event.isTime() && reaction.getTicks() <= 0) {
                issues.add(where + " needs a tick count greater than zero.");
            }
        }
    }

    private static List<PeakAction> actionsOf(PeakElement<?> element) {
        List<PeakAction> actions = new ArrayList<>();
        for (PeakReaction reaction : element.getEvents()) actions.addAll(reaction.getActions());
        return actions;
    }

    private static void collectElements(PeakElement<?> element, Map<String, PeakElement<?>> into) {
        into.putIfAbsent(element.getId(), element);
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) collectElements(child, into);
        }
    }

    private static PeakElement<?> findElement(PeakElement<?> root, String id) {
        if (root.getId().equals(id)) return root;
        if (root instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) {
                PeakElement<?> found = findElement(child, id);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static Set<String> collectIds(PeakElement<?> root) {
        Set<String> ids = new HashSet<>();
        collectIdsRecursive(root, ids);
        return ids;
    }

    private static void collectIdsRecursive(PeakElement<?> element, Set<String> ids) {
        ids.add(element.getId());
        if (element instanceof PeakContainer<?> container) {
            for (PeakElement<?> child : container.getChildren()) collectIdsRecursive(child, ids);
        }
    }

    public record ValidationResult(List<String> issues) {
        public boolean isValid() { return issues.isEmpty(); }
    }
}
