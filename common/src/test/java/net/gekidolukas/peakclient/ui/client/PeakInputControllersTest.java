package net.gekidolukas.peakclient.ui.client;

import net.gekidolukas.peakclient.PeakUiClasses;
import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.PeakInput;
import net.gekidolukas.peakclient.ui.PeakNumberField;
import net.gekidolukas.peakclient.ui.PeakSlider;
import net.gekidolukas.peakclient.ui.PeakTextBox;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakInputControllersTest {
    private static PeakInputControllers controllers() {
        PeakMenuContext context = new PeakMenuContext(null);
        return new PeakInputControllers(context, new PeakMenuChanges(), new PeakTextHits(context));
    }

    @Test
    void everyInputElementClassHasItsOwnController() throws Exception {
        PeakInputControllers controllers = controllers();
        List<String> missing = new ArrayList<>();
        int inputs = 0;
        for (Class<?> type : PeakUiClasses.all()) {
            if (!PeakInput.class.isAssignableFrom(type) || !PeakElement.class.isAssignableFrom(type)) continue;
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) continue;
            inputs++;
            if (!controllers.handles(type)) missing.add(type.getSimpleName());
        }
        assertTrue(inputs >= 8, "the scan found only " + inputs + " input element classes");
        assertEquals(List.of(), missing, "input elements without a controller in PeakInputControllers");
    }

    @Test
    void anElementWithoutASpecialControllerUsesTheActivationFallback() {
        PeakInputControllers controllers = controllers();
        PeakTextBox box = PeakTextBox.create("ns:box", "");
        PeakSlider slider = PeakSlider.create("ns:slider", 0, 10, 5);
        PeakNumberField number = PeakNumberField.create("ns:number", 0, 10, 5);

        assertNotSame(controllers.of(box), controllers.of(slider));
        assertSame(controllers.of(box), controllers.of(box));
        assertTrue(controllers.of(box).isTyping());
        assertTrue(controllers.of(number).isTyping());
        assertEquals(false, controllers.of(slider).isTyping());
    }
}
