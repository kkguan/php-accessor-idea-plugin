package com.free2one.accessor.codeInspection;

import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultipleClassDeclarationsInspectionSuppressor implements InspectionSuppressor {

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!toolId.equals("PhpMultipleClassDeclarationsInspection")) {
            return false;
        }

        PsiElement parentElement = element.getParent();
        if (parentElement instanceof ClassReference classReference) {
            return metadataExisted(classReference.getProject(), classReference.getFQN());
        }

        if (parentElement instanceof PhpClass phpClass) {
            return metadataExisted(phpClass.getProject(), phpClass.getFQN());
        }
        
        return false;
    }

    private boolean metadataExisted(Project project, String classFQN) {
        return ReadAction.compute(() -> {
            if (classFQN == null) {
                return false;
            }

            MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
            ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(classFQN);

            return classMetadata != null;
        });
    }

    @Override
    public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}
