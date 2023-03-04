package com.free2one.accessor.codeInsight.navigation;

import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class AccessorGotoDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null || sourceElement.getParent() == null) {
            return null;
        }

        Collection<PsiElement> psiTargets = new ArrayList<>();
        PsiElement elementParent = sourceElement.getParent();
        classDeclaration(elementParent, sourceElement, psiTargets);
        methodDeclaration(elementParent, sourceElement, psiTargets);

        return psiTargets.toArray(new PsiElement[psiTargets.size()]);
    }

    @Override
    public @Nullable @Nls(capitalization = Nls.Capitalization.Title) String getActionText(@NotNull DataContext context) {
        return GotoDeclarationHandler.super.getActionText(context);
    }

    private void methodDeclaration(PsiElement elementParent, PsiElement sourceElement, Collection<PsiElement> psiTargets) {
        if (!(elementParent instanceof MethodReference sourceMethodReference) ||
                sourceMethodReference.getNavigationElement() == null ||
                !(sourceMethodReference.getNavigationElement() instanceof MethodReference) ||
                ((MethodReference) sourceMethodReference.getNavigationElement()).getClassReference() == null) {
            return;
        }

        PhpType sourceElementType = sourceMethodReference.getType();
        PhpType pendingType = sourceElementType.isComplete() ? sourceElementType : sourceMethodReference.getClassReference().getGlobalType();
        if (pendingType.getTypes().isEmpty()) {
            pendingType = sourceMethodReference.getGlobalType();
        }

        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(sourceElement.getProject());
        AccessorSettings settings = sourceElement.getProject().getService(AccessorSettings.class);
        for (String classname : pendingType.getTypes()) {
            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(sourceElement.getProject()).getClassesByFQN(classname);
            for (PhpClass phpClass : phpNamedElements) {
                if (settings.containSettingDirectories(phpClass.getContainingFile().getVirtualFile().getPath())) {
                    continue;
                }

                Method method = phpClass.findMethodByName(sourceElement.getText());
                if (method != null) {
                    psiTargets.add(method);
                    continue;
                }

                ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(phpClass.getFQN());
                if (classMetadata == null) {
                    continue;
                }

                String fieldName = classMetadata.findFieldNameFromMethodName(sourceElement.getText());
                phpClass.getFields().stream().filter(field -> field.getName().equals(fieldName)).findAny().ifPresent(psiTargets::add);
            }
        }
    }

    private void classDeclaration(PsiElement elementParent, PsiElement sourceElement, Collection<PsiElement> psiTargets) {
        if (!(elementParent instanceof ClassReference clazz) ||
                clazz.getNavigationElement() == null ||
                !(clazz.getNavigationElement() instanceof ClassReference)) {
            return;
        }

        PhpType sourceElementType = clazz.getType();
        PhpType pendingType = sourceElementType.isComplete() ? sourceElementType : clazz.getGlobalType();
        AccessorSettings settings = sourceElement.getProject().getService(AccessorSettings.class);
        for (String classname : pendingType.getTypes()) {
            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(sourceElement.getProject()).getClassesByFQN(classname)
                    .stream()
                    .filter(c -> !settings.containSettingDirectories(c.getContainingFile().getVirtualFile().getPath()))
                    .collect(Collectors.toCollection(ArrayList::new));
            psiTargets.addAll(phpNamedElements);
        }
    }

}
