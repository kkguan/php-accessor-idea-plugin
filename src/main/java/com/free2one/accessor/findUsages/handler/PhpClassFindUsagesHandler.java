package com.free2one.accessor.findUsages.handler;

import com.free2one.accessor.findUsages.AccessorFindUsagesSearchScope;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhpClassFindUsagesHandler extends AccessorFindUsagesHandler {
    public PhpClassFindUsagesHandler(@NotNull PsiElement psiElement) {
        super(psiElement);
    }

    @Override
    public boolean findable() {
        return myPsiElement instanceof PhpClass;
    }

    @Override
    public @NotNull FindUsagesOptions getFindUsagesOptions(@Nullable DataContext dataContext) {
        FindUsagesOptions options = super.getFindUsagesOptions(dataContext);
        options.searchScope = new AccessorFindUsagesSearchScope(myPsiElement.getProject());
        return options;
    }
}
