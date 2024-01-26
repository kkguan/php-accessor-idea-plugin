package com.free2one.accessor.codeInsight.navigation;

import com.free2one.accessor.AccessorFinderService;
import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AccessorGotoDeclarationHandler implements GotoDeclarationHandler {

    private final Map<Class<? extends DeclarationHandler>, DeclarationHandler> declarationHandlers;

    {
        declarationHandlers = new HashMap<>() {
            {
                put(ClassReferenceHandler.class, new ClassReferenceHandler());
                put(PhpDocTypeHandler.class, new PhpDocTypeHandler());
                put(MethodReferenceHandler.class, new MethodReferenceHandler());
            }
        };
    }

    private static <T extends PhpTypedElement> void addClassDeclaration(PsiElement sourceElement, Collection<PsiElement> psiTargets, T clazz) {
        PhpType sourceElementType = clazz.getType();
        PhpType pendingType = sourceElementType.isComplete() ? sourceElementType : clazz.getGlobalType();
        AccessorSettings settings = sourceElement.getProject().getService(AccessorSettings.class);
        for (String className : pendingType.getTypes()) {
            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(sourceElement.getProject()).getClassesByFQN(className)
                    .stream()
                    .filter(c -> !settings.containSettingDirectories(c.getContainingFile().getVirtualFile().getPath()))
                    .collect(Collectors.toCollection(ArrayList::new));
            psiTargets.addAll(phpNamedElements);
        }
    }

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        if (sourceElement == null || sourceElement.getParent() == null) {
            return null;
        }

        Collection<PsiElement> psiTargets = new ArrayList<>();
        PsiElement elementParent = sourceElement.getParent();
        declarationHandlers.forEach((aClass, declarationHandler) -> declarationHandler.handle(elementParent, sourceElement, psiTargets));

        return psiTargets.toArray(new PsiElement[psiTargets.size()]);
    }


    @Override
    public @Nullable @Nls(capitalization = Nls.Capitalization.Title) String getActionText(@NotNull DataContext context) {
        return GotoDeclarationHandler.super.getActionText(context);
    }

    private interface DeclarationHandler {
        void handle(PsiElement elementParent, PsiElement sourceElement, Collection<PsiElement> psiTargets);
    }

    private static class ClassReferenceHandler implements DeclarationHandler {
        @Override
        public void handle(PsiElement elementParent, PsiElement sourceElement, Collection<PsiElement> psiTargets) {
            if (!(elementParent instanceof ClassReference clazz) ||
                    clazz.getNavigationElement() == null ||
                    !(clazz.getNavigationElement() instanceof ClassReference)) {
                return;
            }

            addClassDeclaration(sourceElement, psiTargets, clazz);
        }
    }

    private static class PhpDocTypeHandler implements DeclarationHandler {
        @Override
        public void handle(PsiElement elementParent, PsiElement sourceElement, Collection<PsiElement> psiTargets) {
            if (!(elementParent instanceof PhpDocType clazz) ||
                    clazz.getNavigationElement() == null ||
                    !(clazz.getNavigationElement() instanceof PhpDocType)) {
                return;
            }

            addClassDeclaration(sourceElement, psiTargets, clazz);
        }
    }

    private static class MethodReferenceHandler implements DeclarationHandler {
        @Override
        public void handle(PsiElement elementParent, PsiElement sourceElement, Collection<PsiElement> psiTargets) {
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

            AccessorSettings settings = sourceElement.getProject().getService(AccessorSettings.class);
            AccessorFinderService accessorFinderService = sourceElement.getProject().getService(AccessorFinderService.class);
            for (String className : pendingType.getTypes()) {
                Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(sourceElement.getProject()).getClassesByFQN(className);
                for (PhpClass phpClass : phpNamedElements) {
                    if (settings.containSettingDirectories(phpClass.getContainingFile().getVirtualFile().getPath())) {
                        continue;
                    }

                    Method method = phpClass.findMethodByName(sourceElement.getText());
                    if (method != null) {
                        psiTargets.add(method);
                        continue;
                    }

                    ClassMetadata classMetadata = accessorFinderService.getAccessorMetadata(phpClass.getFQN());
                    if (classMetadata == null) {
                        continue;
                    }

                    String fieldName = classMetadata.findFieldNameFromMethodName(sourceElement.getText());
                    phpClass.getFields().stream().filter(field -> field.getName().equals(fieldName)).findAny().ifPresent(psiTargets::add);
                }
            }
        }
    }

}
