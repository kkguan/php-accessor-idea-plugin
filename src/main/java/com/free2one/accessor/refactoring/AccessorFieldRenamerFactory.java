package com.free2one.accessor.refactoring;

import com.free2one.accessor.AccessorFinderService;
import com.free2one.accessor.PhpAccessorClassnames;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.rename.naming.AutomaticRenamer;
import com.intellij.usageView.UsageInfo;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.refactoring.rename.automaticRenamers.FieldAccessorsRenamerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AccessorFieldRenamerFactory extends FieldAccessorsRenamerFactory {
    @Override
    public @NotNull AutomaticRenamer createRenamer(PsiElement element, String newName, Collection<UsageInfo> usages) {
        return new MyFieldAccessorsRenamer((Field) element, newName);
    }


    public static class MyFieldAccessorsRenamer extends AutomaticRenamer {

//        @Override
//        public void findUsages(List<UsageInfo> result, boolean searchInStringsAndComments, boolean searchInNonJavaFiles, List<UnresolvableCollisionUsageInfo> unresolvedUsages, Map<PsiElement, String> allRenames) {
//            super.findUsages(result, searchInStringsAndComments, searchInNonJavaFiles, unresolvedUsages, allRenames);
//        }

        public MyFieldAccessorsRenamer(@NotNull Field field, @NotNull String newName) {
            super();
            PhpClass phpClass = field.getContainingClass();
            if (phpClass == null) {
                return;
            }

            if (!AnnotationSearchUtil.isAnnotatedWith(phpClass, PhpAccessorClassnames.Data)) {
                return;
            }

            String fieldName = field.getName();
            AccessorFinderService accessorFinderService = field.getProject().getService(AccessorFinderService.class);
            Collection<PsiElement> elements = ReadAction.compute(() -> accessorFinderService.getGeneratedAccessorOfField(field));
            if (elements.isEmpty()) {
                return;
            }

            elements.forEach(element -> {
                this.myElements.add((PsiNamedElement) element);
                Parameter[] parameters = ((Method) element).getParameters();
                if (parameters.length == 1 && PhpLangUtil.equalsFieldNames(parameters[0].getName(), fieldName)) {
                    this.myElements.add(parameters[0]);
                }
            });

            this.suggestAllNames(fieldName, newName);
        }

        public boolean isSelectedByDefault() {
            return true;
        }

        public String getDialogTitle() {
            return PhpBundle.message("refactoring.rename.automatic.renamer.field.accessors.dialog.title", new Object[0]);
        }

        public String getDialogDescription() {
            return PhpBundle.message("refactoring.rename.automatic.renamer.field.accessors.dialog.description", new Object[0]);
        }

        public String entityName() {
            return PhpBundle.message("refactoring.rename.automatic.renamer.field.accessors.entity.name", new Object[0]);
        }
    }
}
