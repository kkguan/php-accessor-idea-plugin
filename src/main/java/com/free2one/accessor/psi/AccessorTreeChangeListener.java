package com.free2one.accessor.psi;

import com.free2one.accessor.method.ClassProcessor;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AccessorTreeChangeListener implements PsiTreeChangeListener {
    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        if (Optional.ofNullable(event.getFile()).isEmpty()) {
            return;
        }
        event.getFile().acceptChildren(new PhpElementVisitor() {
            @Override
            public void visitPhpElement(PhpPsiElement element) {
                element.acceptChildren(new PhpElementVisitor() {
                    @Override
                    public void visitPhpClass(PhpClass clazz) {
                        //未处理没有命名空间的情形
                    }

                    @Override
                    public void visitPhpNamespace(PhpNamespace namespace) {
                        namespace.getLastChild().acceptChildren(new PhpElementVisitor() {
                            @Override
                            public void visitPhpClass(PhpClass clazz) {
                                new ClassProcessor(clazz.getProject()).run(clazz);
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

    }
}
