package com.free2one.accessor.codeInspection;

import com.free2one.accessor.AccessorFinderService;
import com.free2one.accessor.meta.ClassMetadata;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultipleClassDeclarationsInspectionSuppressor implements InspectionSuppressor {

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!toolId.equals("PhpMultipleClassDeclarationsInspection")) {
            return false;
        }

        PsiElement parentElement = element.getParent();
        Class<?>[] classes = new Class<?>[]{ClassReference.class, PhpClass.class, PhpDocType.class};
        for (Class<?> clazz : classes) {
            if (!clazz.isInstance(parentElement)) {
                continue;
            }

            if (parentElement instanceof PhpReference phpReference) {
                // interim solution: PhpDocType has a problem getting FQM, so get type directly here
                return metadataExisted(parentElement.getProject(), phpReference.getType().toString());
            }

            if (parentElement instanceof PhpNamedElement phpNamedElement) {
                return metadataExisted(parentElement.getProject(), phpNamedElement.getFQN());
            }
        }

        return false;
    }

    private boolean metadataExisted(Project project, String classFQN) {
        return ReadAction.compute(() -> {
            if (classFQN == null) {
                return false;
            }

            ClassMetadata metadata = project.getService(AccessorFinderService.class).getAccessorMetadata(classFQN);

            return metadata != null;
        });
    }

    @Override
    public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}
