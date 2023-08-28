package com.free2one.accessor.findUsages.handler;

import com.free2one.accessor.AccessorFinderService;
import com.free2one.accessor.findUsages.AccessorFindUsagesSearchScope;
import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class FieldFindUsagesHandler extends AccessorFindUsagesHandler {

    public FieldFindUsagesHandler(@NotNull PsiElement psiElement) {
        super(psiElement);
    }

    @Override
    public boolean findable() {
        if (myPsiElement instanceof Field) {
            PhpClass phpClass = ((Field) myPsiElement).getContainingClass();
            if (phpClass != null) {
                MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(myPsiElement.getProject());
                ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(phpClass.getFQN());
                return classMetadata != null;
            }
        }

        return false;
    }

    @Override
    public PsiElement @NotNull [] getSecondaryElements() {
        if (!(myPsiElement instanceof Field field)) {
            return PsiElement.EMPTY_ARRAY;
        }

        AccessorFinderService accessorFinderService = myPsiElement.getProject().getService(AccessorFinderService.class);
        Collection<PsiElement> elements = ReadAction.compute(() -> accessorFinderService.getGeneratedAccessorOfField(field));
        if (elements.isEmpty()) {
            return PsiElement.EMPTY_ARRAY;
        }

        return PsiUtilCore.toPsiElementArray(elements);
    }

    @Override
    public @NotNull FindUsagesOptions getFindUsagesOptions(@Nullable DataContext dataContext) {
        FindUsagesOptions options = super.getFindUsagesOptions(dataContext);
        options.searchScope = new AccessorFindUsagesSearchScope(myPsiElement.getProject());
        return options;
    }

}
