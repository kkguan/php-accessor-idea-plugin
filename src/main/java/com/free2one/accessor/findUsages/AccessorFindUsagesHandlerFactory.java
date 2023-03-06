package com.free2one.accessor.findUsages;

import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.free2one.accessor.method.AccessorMethod;
import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class AccessorFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
    @Override
    public boolean canFindUsages(@NotNull PsiElement element) {
        if (!(element instanceof Field)) {
            return false;
        }

        PhpClass phpClass = ((Field) element).getContainingClass();
        if (phpClass == null) {
            return false;
        }

        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(element.getProject());
        ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(phpClass.getFQN());
        return classMetadata != null;
    }

    @Override
    public @Nullable FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
        return new MyFindUsagesHandler(element);
    }

    private static class MyFindUsagesHandler extends FindUsagesHandler {

        protected MyFindUsagesHandler(@NotNull PsiElement psiElement) {
            super(psiElement);
        }


        @Override
        public @NotNull FindUsagesOptions getFindUsagesOptions(@Nullable DataContext dataContext) {
            FindUsagesOptions options = super.getFindUsagesOptions(dataContext);
            options.searchScope = new AccessorFindUsagesSearchScope(myPsiElement.getProject());
            return options;
        }

        @Override
        public PsiElement @NotNull [] getSecondaryElements() {
            if (!(myPsiElement instanceof Field)) {
                return PsiElement.EMPTY_ARRAY;
            }

            PhpClass containingClass = ((Field) myPsiElement).getContainingClass();
            if (containingClass == null) {
                return PsiElement.EMPTY_ARRAY;
            }

            ArrayList<String> relevantMethods = getRelevantMethodsWithField(containingClass);
            if (relevantMethods.isEmpty()) {
                return PsiElement.EMPTY_ARRAY;
            }

            final Collection<PsiElement> elements = new ArrayList<>();
            setRelatedQueryMethods(elements, containingClass, relevantMethods);
            if (!elements.isEmpty()) {
                return PsiUtilCore.toPsiElementArray(elements);
            }

            return PsiElement.EMPTY_ARRAY;
        }

        private ArrayList<String> getRelevantMethodsWithField(PhpClass containingClass) {
            ArrayList<String> relevantMethods = new ArrayList<>();
            ArrayList<AccessorMethod> accessorMethods = ReadAction.compute(() -> {
                MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(myPsiElement.getProject());
                ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(containingClass.getFQN());
                if (classMetadata == null) {
                    return null;
                }

                return classMetadata.getMethods();
            });
            if (accessorMethods == null) {
                return relevantMethods;
            }

            relevantMethods.addAll(accessorMethods.stream()
                    .filter(accessorMethod -> accessorMethod.getFieldName().equals(((Field) myPsiElement).getName()))
                    .map(AccessorMethod::getMethodName)
                    .collect(Collectors.toCollection(ArrayList::new)));

            return relevantMethods;
        }


        private void setRelatedQueryMethods(Collection<PsiElement> elements, PhpClass containingClass, ArrayList<String> relevantMethods) {
            Collection<? extends PhpClass> phpNamedElements = ReadAction.compute(() -> PhpIndex.getInstance(myPsiElement.getProject()).getClassesByFQN(containingClass.getFQN()));
            AccessorSettings settings = myPsiElement.getProject().getService(AccessorSettings.class);
            for (PhpClass clazz : phpNamedElements) {
                if (!settings.containProxyDirectory(clazz.getContainingFile().getVirtualFile().getPath())) {
                    continue;
                }

                Collection<Method> relatedMethods = clazz.getMethods().stream()
                        .filter(method -> relevantMethods.contains(method.getName()) && containingClass.findMethodByName(method.getName()) == null)
                        .toList();
                elements.addAll(relatedMethods);
            }
        }

    }

}
