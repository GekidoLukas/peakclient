package net.gekidolukas.peakclient;

import net.gekidolukas.peakclient.ui.PeakElement;
import net.gekidolukas.peakclient.ui.serialize.PeakElementTypeAdapterFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakElementRegistryTest {
    private static List<Class<?>> concreteElementClasses() throws Exception {
        List<Class<?>> result = new ArrayList<>();
        for (Class<?> type : PeakUiClasses.all()) {
            if (type == PeakElement.class || !PeakElement.class.isAssignableFrom(type)) continue;
            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) continue;
            result.add(type);
        }
        return result;
    }

    @Test
    void everyConcreteElementClassIsRegisteredWithAPrototype() throws Exception {
        List<Class<?>> concrete = concreteElementClasses();
        assertTrue(concrete.size() > 20, "the scan found only " + concrete.size() + " element classes");

        List<String> missing = new ArrayList<>();
        for (Class<?> type : concrete) {
            if (!PeakElementTypeAdapterFactory.registeredClasses().contains(type)) missing.add(type.getSimpleName());
        }
        assertEquals(List.of(), missing, "element classes missing in PeakElementTypeAdapterFactory");
    }

    @Test
    void everyRegisteredClassIsAConcreteElement() {
        for (Class<?> type : PeakElementTypeAdapterFactory.registeredClasses()) {
            assertTrue(PeakElement.class.isAssignableFrom(type), type.getSimpleName() + " is not a PeakElement");
            assertTrue(!Modifier.isAbstract(type.getModifiers()), type.getSimpleName() + " is abstract");
        }
    }
}
