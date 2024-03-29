package com.free2one.accessor.codeInspection;

import com.free2one.accessor.AccessorFinderService;
import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Suppress unused field inspection when field has accessor
 */
public class UnusedFieldInspectionSuppressor {

    private static boolean fieldHasAccessor(PsiElement element) {
        if (!(element instanceof Field field) ||
                field.getContainingClass() == null) {
            return false;
        }

        PhpClass phpClass = field.getContainingClass();
        if (phpClass == null ||
                phpClass.getProject().getService(AccessorSettings.class)
                        .containProxyDirectory(phpClass.getContainingFile().getVirtualFile().getPath())) {
            return false;
        }

        return ReadAction.compute(() -> {
            ClassMetadata classMetadata = element.getProject().getService(AccessorFinderService.class).getAccessorMetadata(phpClass.getFQN());
            if (classMetadata == null) {
                return false;
            }

            String fieldName = field.getName();
            Collection<String> methodNames = classMetadata.findMethodNamesFromFieldName(fieldName);
            if (methodNames.isEmpty()) {
                return false;
            }

            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(phpClass.getProject()).getTraitsByName(classMetadata.getAccessorClassname());
            for (PhpClass clazz : phpNamedElements) {
                Collection<Method> elements = clazz.getMethods().stream()
                        .filter(method -> methodNames.stream().anyMatch(name -> name.equals(method.getName())))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (!elements.isEmpty()) {
                    return true;
                }
//                PhpAccessorsGenerator accessorsGenerator = new PhpAccessorsGenerator(clazz, field);
//                if (accessorsGenerator.findSetters().length > 0 ||
//                        accessorsGenerator.findGetters().length > 0) {
//                    return true;
//                }
            }
            return false;
        });
    }

    static class PropertyOnlyWrittenInspectionSuppressor implements InspectionSuppressor {
        @Override
        public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
            if (!toolId.equals("PhpPropertyOnlyWrittenInspection")) {
                return false;
            }

            return UnusedFieldInspectionSuppressor.fieldHasAccessor(element);
        }

        @Override
        public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
            return new SuppressQuickFix[0];
        }
    }

    static class UnusedPrivateFieldInspectionSuppressor implements InspectionSuppressor {
        @Override
        public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
            if (!toolId.equals("PhpUnusedPrivateFieldInspection")) {
                return false;
            }

            return UnusedFieldInspectionSuppressor.fieldHasAccessor(element.getParent());
        }

        @Override
        public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
            return new SuppressQuickFix[0];
        }
    }

}
