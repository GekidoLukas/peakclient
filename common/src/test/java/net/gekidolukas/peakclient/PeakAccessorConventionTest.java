package net.gekidolukas.peakclient;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeakAccessorConventionTest {
    private static boolean isPlainInstanceMethod(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !method.isSynthetic() && !method.isBridge();
    }

    private static boolean isBuilderReturn(Class<?> owner, Method method) {
        Class<?> type = method.getReturnType();
        return type == void.class || type.isAssignableFrom(owner) || owner.isAssignableFrom(type)
                || method.getGenericReturnType() instanceof TypeVariable<?>;
    }

    @Test
    void aClassWithAFluentSetterReadsWithGetOrIsInsteadOfABareAccessor() throws Exception {
        List<Class<?>> classes = PeakUiClasses.all();
        assertTrue(classes.size() > 50, "the scan found only " + classes.size() + " classes");

        List<String> violations = new ArrayList<>();
        for (Class<?> type : classes) {
            if (type.isRecord() || type.isEnum() || type.isInterface() || type.isAnnotation()) continue;

            Set<String> oneArgument = new HashSet<>();
            for (Method method : type.getDeclaredMethods()) {
                if (isPlainInstanceMethod(method) && method.getParameterCount() == 1) oneArgument.add(method.getName());
            }
            for (Method method : type.getDeclaredMethods()) {
                if (!isPlainInstanceMethod(method) || method.getParameterCount() != 0) continue;
                if (isBuilderReturn(type, method)) continue;
                if (oneArgument.contains(method.getName())) violations.add(type.getSimpleName() + "." + method.getName() + "()");
            }
        }
        assertEquals(List.of(), violations,
                "read with getX()/isX() when a fluent x(value) setter of the same name exists");
    }
}
