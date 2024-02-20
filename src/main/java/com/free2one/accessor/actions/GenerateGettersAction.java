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

/**
 * This class extends PhpGenerateGettersAction to generate getter methods for PHP classes.
 */
public class GenerateGettersAction extends PhpGenerateGettersAction {

    /**
     * Handler for generating field accessors.
     */
    protected final PhpGenerateFieldAccessorHandlerBase myHandler = new GenerateFieldAccessorHandlerBase() {
        /**
         * Creates accessors for a given PHP class and field.
         * @param targetClass The PHP class to generate accessors for.
         * @param field The field to generate accessors for.
         * @return An array of PhpAccessorMethodData representing the generated accessors.
         */
        protected PhpAccessorMethodData[] createAccessors(PhpClass targetClass, PsiElement field) {
            return (new PhpAccessorsGenerator(targetClass, (Field) field)).createGetters(false);
        }

        /**
         * Checks if a field in a PHP class is selectable for generating accessors.
         * @param phpClass The PHP class containing the field.
         * @param field The field to check.
         * @return true if no getters exist for the field, false otherwise.
         */
        protected boolean isSelectable(PhpClass phpClass, Field field) {
            return (new PhpAccessorsGenerator(phpClass, field)).findGetters().length == 0;
        }

        protected @Nls String getErrorMessage() {
            return PhpBundle.message("no.private.fields.to.generate.getters.for");
        }

        /**
         * Checks if the handler contains setters.
         * @return true as this handler is for generating setters.
         */
        protected boolean containsSetters() {
            return false;
        }

        /**
         * Checks if the handler contains getters.
         * @return true as this handler is for generating getters.
         */
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
