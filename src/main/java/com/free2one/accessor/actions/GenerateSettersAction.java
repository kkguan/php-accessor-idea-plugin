package com.free2one.accessor.actions;

import com.free2one.accessor.actions.generation.GenerateFieldAccessorHandlerBase;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.lang.actions.generation.PhpGenerateFieldAccessorHandlerBase;
import com.jetbrains.php.lang.actions.generation.PhpGenerateSettersAction;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorMethodData;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorsGenerator;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class GenerateSettersAction extends PhpGenerateSettersAction {
    private static final PhpGenerateFieldAccessorHandlerBase myHandler = new GenerateFieldAccessorHandlerBase() {
        protected PhpAccessorMethodData[] createAccessors(PhpClass targetClass, PsiElement field) {
            return (new PhpAccessorsGenerator(targetClass, (Field) field)).createSetters(this.isFluentSetters(), false);
        }

        protected boolean isSelectable(PhpClass phpClass, Field field) {
            if (field.isReadonly()) {
                return false;
            } else {
                return (new PhpAccessorsGenerator(phpClass, field)).findSetters().length == 0;
            }
        }

        protected @Nls String getErrorMessage() {
            return PhpBundle.message("no.private.fields.to.generate.setters.for", new Object[0]);
        }

        protected boolean containsSetters() {
            return true;
        }
    };


    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return myHandler.isValidFor(editor, file);
    }

    protected @NotNull CodeInsightActionHandler getHandler() {
        return myHandler;
    }
}
