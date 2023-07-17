package com.free2one.accessor.refactoring;

import com.free2one.accessor.PhpAccessorClassnames;
import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.naming.AutomaticRenamer;
import com.intellij.usageView.UsageInfo;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.refactoring.rename.automaticRenamers.FieldAccessorsRenamerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

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

            ClassMetadata classMetadata = ReadAction.compute(() -> {
                MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(phpClass.getProject());
                return methodMetaDataRepository.getFromClassname(phpClass.getFQN());
            });
            if (classMetadata == null) {
                return;
            }

            String fieldName = field.getName();
            Collection<String> methodNames = classMetadata.findMethodNamesFromFieldName(fieldName);
            if (methodNames.isEmpty()) {
                return;
            }

            ReadAction.compute(() -> {
                Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(phpClass.getProject()).getTraitsByName(classMetadata.getAccessorClassname());
                for (PhpClass clazz : phpNamedElements) {
                    Collection<Method> elements = clazz.getMethods().stream()
                            .filter(method -> methodNames.stream().anyMatch(name -> name.equals(method.getName())))
                            .collect(Collectors.toCollection(ArrayList::new));
                    this.myElements.addAll(elements);
                    elements.forEach(method -> {
                        Parameter[] parameters = method.getParameters();
                        if (parameters.length == 1 && PhpLangUtil.equalsFieldNames(parameters[0].getName(), fieldName)) {
                            this.myElements.add(parameters[0]);
                        }
                    });
                }
                return null;
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
