package com.free2one.accessor.codeInspection;

import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorsGenerator;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class UnusedPrivateFieldInspectionSuppressor implements InspectionSuppressor {

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (!toolId.equals("PhpUnusedPrivateFieldInspection")) {
            return false;
        }

        PsiElement parentElement = element.getParent();
        if (!(parentElement instanceof Field field) ||
                field.getContainingClass() == null) {
            return false;
        }

        PhpClass phpClass = field.getContainingClass();
        if (phpClass.getProject().getService(AccessorSettings.class)
                .containProxyDirectory(phpClass.getContainingFile().getVirtualFile().getPath())) {
            return false;
        }

        return ReadAction.compute(() -> {
            MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(phpClass.getProject());
            ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(phpClass.getFQN());
            if (classMetadata == null) {
                return false;
            }

            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(phpClass.getProject()).getTraitsByName(classMetadata.getAccessorClassname());
            for (PhpClass clazz : phpNamedElements) {
                PhpAccessorsGenerator accessorsGenerator = new PhpAccessorsGenerator(clazz, field);
                if (accessorsGenerator.findSetters().length > 0 ||
                        accessorsGenerator.findGetters().length > 0) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];
    }
}