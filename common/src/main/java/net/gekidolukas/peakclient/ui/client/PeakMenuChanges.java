package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakClientTickCounter;
import net.gekidolukas.peakclient.ui.PeakDocumentModel;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakEvent;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakRichTextBox;
import net.gekidolukas.peakclient.ui.PeakTextBox;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class PeakMenuChanges {
    static final int NUMBER_DELAY_TICKS = 3;

    private final PeakChangeDebouncer debouncer = new PeakChangeDebouncer();
    private String lastFocused;

    void noteText(PeakTextBox field) {
        if (!field.hasEvent(PeakEvent.CHANGE)) return;
        debouncer.change(field.getId(), field.getValue(), field.getChangeDelayTicks(), PeakClientTickCounter.current());
    }

    void noteRich(PeakRichTextBox box) {
        if (!box.hasEvent(PeakEvent.CHANGE)) return;
        debouncer.change(box.getId(), box.getValue(), box.getChangeDelayTicks(), PeakClientTickCounter.current());
    }

    void noteNumber(PeakNumberField number) {
        if (!number.hasEvent(PeakEvent.CHANGE)) return;
        debouncer.change(number.getId(), number.inputValue(), NUMBER_DELAY_TICKS, PeakClientTickCounter.current());
    }

    void flush(String menuId, String elementId) {
        PeakChangeDebouncer.Fire fire = debouncer.flush(elementId);
        if (fire != null) fire(menuId, List.of(fire));
    }

    void flushAll(String menuId) {
        fire(menuId, debouncer.flushAll());
    }

    void tick(String menuId, String focusedId, Consumer<String> blurred, BiConsumer<String, String> focusChanged) {
        if (!Objects.equals(lastFocused, focusedId)) {
            if (lastFocused != null) {
                blurred.accept(lastFocused);
                flush(menuId, lastFocused);
            }
            focusChanged.accept(lastFocused, focusedId);
        }
        lastFocused = focusedId;
        fire(menuId, debouncer.due(PeakClientTickCounter.current()));
    }

    private void fire(String menuId, List<PeakChangeDebouncer.Fire> fires) {
        if (fires.isEmpty() || menuId == null) return;
        PeakDocumentModel model = PeakClientMenus.model(menuId);
        if (model == null) return;
        for (PeakChangeDebouncer.Fire fire : fires) {
            PeakElement<?> element = model.find(fire.elementId());
            if (element == null) continue;
            PeakEventDispatch.fire(PeakActionScope.menu(menuId), element, PeakEvent.CHANGE, Map.of("value", fire.value()));
        }
    }
}
