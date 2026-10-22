package net.gekidolukas.peakclient.api;

import net.gekidolukas.peakclient.network.PeakMenuPayload;
import net.gekidolukas.peakclient.ui.PeakDocument;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakUiOperation;
import net.gekidolukas.peakclient.ui.patch.PeakBasePatch;
import net.gekidolukas.peakclient.ui.template.PeakFormField;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class PeakMenus {
    private final Supplier<Collection<UUID>> source;

    public PeakMenus(Supplier<Collection<UUID>> source) {
        this.source = source;
    }

    private PeakClientApi api() {
        return PeakClientApi.get();
    }

    private Collection<UUID> ids() {
        return source.get();
    }

    public PeakMenus send(PeakDocument menu) {
        if (!menu.isMenu()) {
            throw new IllegalArgumentException("Document '" + menu.getId() + "' is not a menu, create it with PeakMenu.create(...)");
        }
        Collection<UUID> targets = ids();
        api().sendMenuToClient(menu, targets);
        PeakRemotePages.onMenuSent(menu.getId(), targets);
        return this;
    }

    public PeakMenus sendAndOpen(PeakDocument menu) {
        send(menu);
        return open(menu.getId());
    }

    public PeakMenus update(String menuId, List<PeakUiOperation> operations) {
        api().sendMenuUpdateToClient(menuId, operations, ids());
        return this;
    }

    public PeakMenus update(String menuId, String elementId, PeakBasePatch<?> patch) {
        return update(menuId, List.of(new PeakUiOperation.Update(elementId, patch.build().deepCopy())));
    }

    public PeakMenus updateByClass(String menuId, String className, PeakBasePatch<?> patch) {
        return update(menuId, List.of(new PeakUiOperation.UpdateByClass(className, patch.build().deepCopy())));
    }

    public PeakMenus add(String menuId, String parentId, PeakElement<?> element) {
        return update(menuId, List.of(new PeakUiOperation.Add(parentId, element)));
    }

    public PeakMenus delete(String menuId, String elementId) {
        return update(menuId, List.of(new PeakUiOperation.Delete(elementId)));
    }

    public PeakMenus formError(String menuId, String fieldId, String inputId, String message) {
        return update(menuId, PeakFormField.errorOperations(fieldId, inputId, message));
    }

    public PeakMenus formError(String menuId, String fieldId, String inputId, Component message) {
        return update(menuId, PeakFormField.errorOperations(fieldId, inputId, message));
    }

    public PeakMenus clearFormError(String menuId, String fieldId, String inputId) {
        return formError(menuId, fieldId, inputId, "");
    }

    public PeakMenus open(String menuId) {
        return open(menuId, false, false);
    }

    public PeakMenus open(String menuId, boolean replaceCurrent, boolean force) {
        int flags = (replaceCurrent ? PeakMenuPayload.FLAG_REPLACE : 0) | (force ? PeakMenuPayload.FLAG_FORCE : 0);
        api().menuCommand(PeakMenuCommand.OPEN, menuId, flags, ids());
        return this;
    }

    public PeakMenus close() {
        api().menuCommand(PeakMenuCommand.CLOSE, "", 0, ids());
        return this;
    }

    public PeakMenus back() {
        api().menuCommand(PeakMenuCommand.BACK, "", 0, ids());
        return this;
    }

    public PeakMenus remove(String menuId) {
        api().menuCommand(PeakMenuCommand.REMOVE, menuId, 0, ids());
        return this;
    }

    public PeakMenus clear() {
        api().menuCommand(PeakMenuCommand.CLEAR, "", 0, ids());
        return this;
    }
}
