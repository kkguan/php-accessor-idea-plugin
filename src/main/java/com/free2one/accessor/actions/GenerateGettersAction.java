package com.free2one.accessor.actions;

import com.free2one.accessor.actions.generation.GenerateFieldAccessorHandlerBase;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.lang.actions.generation.PhpGenerateFieldAccessorHandlerBase;
import com.jetbrains.php.lang.actions.generation.PhpGenerateGettersAction;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorMethodData;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorsGenerator;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class GenerateGettersAction extends PhpGenerateGettersAction {


    protected final PhpGenerateFieldAccessorHandlerBase myHandler = new GenerateFieldAccessorHandlerBase() {
        protected PhpAccessorMethodData[] createAccessors(PhpClass targetClass, PsiElement field) {
            return (new PhpAccessorsGenerator(targetClass, (Field) field)).createGetters(false);
        }

        protected boolean isSelectable(PhpClass phpClass, Field field) {
            return (new PhpAccessorsGenerator(phpClass, field)).findGetters().length == 0;
        }

        protected @Nls String getErrorMessage() {
            return PhpBundle.message("no.private.fields.to.generate.getters.for", new Object[0]);
        }

        protected boolean containsSetters() {
            return false;
        }

        protected boolean containsGetters() {
            return true;
        }
    };

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return super.isValidForFile(project, editor, file);
    }

    protected @NotNull CodeInsightActionHandler getHandler() {
        return this.myHandler;
    }


}
