package com.free2one.accessor.findUsages.handler;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

abstract public class AccessorFindUsagesHandler extends com.intellij.find.findUsages.FindUsagesHandler implements UsagesFindable {
    protected AccessorFindUsagesHandler(@NotNull PsiElement psiElement) {
        super(psiElement);
    }
}
