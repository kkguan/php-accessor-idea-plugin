package com.free2one.accessor.actions;

import com.free2one.accessor.actions.generation.GenerateFieldAccessorHandlerBase;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.lang.actions.generation.PhpGenerateFieldAccessorHandlerBase;
import com.jetbrains.php.lang.actions.generation.PhpGenerateGettersAndSettersAction;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorMethodData;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorsGenerator;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * This class extends PhpGenerateGettersAndSettersAction to generate both getter and setter methods for PHP classes.
 */
public class GenerateGettersAndSettersAction extends PhpGenerateGettersAndSettersAction {

    /**
     * Handler for generating field accessors.
     */
    private final PhpGenerateFieldAccessorHandlerBase myHandler = new GenerateFieldAccessorHandlerBase() {
        /**
         * Creates accessors for a given PHP class and field.
         * @param targetClass The PHP class to generate accessors for.
         * @param field The field to generate accessors for.
         * @return An array of PhpAccessorMethodData representing the generated accessors.
         */
        protected PhpAccessorMethodData[] createAccessors(PhpClass targetClass, PsiElement field) {
            return (new PhpAccessorsGenerator(targetClass, (Field) field)).createAccessors(this.isFluentSetters(), false);
        }

        /**
         * Checks if a field in a PHP class is selectable for generating accessors.
         * @param phpClass The PHP class containing the field.
         * @param field The field to check.
         * @return true if no getters and setters exist for the field, false otherwise.
         */
        protected boolean isSelectable(PhpClass phpClass, Field field) {
            if (field.isReadonly()) {
                return false;
            } else {
                PhpAccessorsGenerator accessorsGenerator = new PhpAccessorsGenerator(phpClass, field);
                return accessorsGenerator.findGetters().length == 0 && accessorsGenerator.findSetters().length == 0;
            }
        }

        protected @Nls String getErrorMessage() {
            return PhpBundle.message("no.private.fields.to.generate.both.getters.and.setters.for");
        }

        /**
         * Checks if the handler contains setters.
         * @return true as this handler is for generating setters.
         */
        protected boolean containsSetters() {
            return true;
        }

        /**
         * Checks if the handler contains getters.
         * @return true as this handler is for generating getters.
         */
        protected boolean containsGetters() {
            return true;
        }
    };

    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        return this.myHandler.isValidFor(editor, file);
    }

    protected @NotNull CodeInsightActionHandler getHandler() {
        return this.myHandler;
    }


}
