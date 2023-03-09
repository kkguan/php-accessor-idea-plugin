package com.free2one.accessor.util;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AnnotationSearchUtil {

    public static boolean isAnnotatedWith(PhpClass phpClass, @NotNull String annotationFQN) {
        return !phpClass.getAttributes(annotationFQN).isEmpty();
    }

    public static boolean isAnnotatedWith(Collection<PhpClass> phpClasses, @NotNull String annotationFQN) {
        return phpClasses.stream().anyMatch(phpClass -> !phpClass.getAttributes(annotationFQN).isEmpty());
    }
}
