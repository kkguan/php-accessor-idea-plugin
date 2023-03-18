package com.free2one.accessor.codeInspection.quickfix;

import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.codeInsight.CodeInsightUtilCore;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.inspections.classes.PhpHierarchyChecksInspection;
import com.jetbrains.php.lang.inspections.quickfix.PhpAddFieldDeclarationQuickFix;
import com.jetbrains.php.lang.inspections.quickfix.PhpAddMethodDeclarationQuickFix;
import com.jetbrains.php.lang.inspections.quickfix.PhpParameterInfo;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.elements.MemberReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class AddMethodDeclarationQuickFix extends PhpAddMethodDeclarationQuickFix {

    public static final PhpAddMethodDeclarationQuickFix INSTANCE = new AddMethodDeclarationQuickFix();


    private static @Nullable Collection<PhpClass> resolveClasses(@NotNull MemberReference reference, boolean classesOnly) {
        Project project = reference.getProject();
        PhpIndex index = PhpIndex.getInstance(project);
        PhpExpression classReference = reference.getClassReference();
        PhpType type = PhpHierarchyChecksInspection.unwrapAlias((new PhpType()).add(classReference).global(project), project);
        return PhpType.isAnonymousClass((String) ContainerUtil.getOnlyItem(type.getTypes())) && PhpLangUtil.isThisReference(classReference) ? ContainerUtil.createMaybeSingletonList(PhpClassImpl.getContainingClass(reference)) : StreamEx.of(type.getTypes()).flatMap((fqn) -> {
            return classesOnly ? index.getClassesByFQN(fqn).stream() : index.getAnyByFQN(fqn).stream();
        }).select(PhpClass.class).toList();
    }

    public static @Nullable PhpClass resolveClassWithErrorReporting(@NotNull Project project, @Nullable MemberReference reference, boolean classesOnly) {

        Collection<PhpClass> classes = resolveClasses(reference, classesOnly);
        if (classes != null && !classes.isEmpty()) {
            //过滤掉代理类
            classes.removeIf(phpClass -> phpClass.getProject().getService(AccessorSettings.class)
                    .containProxyDirectory(phpClass.getContainingFile().getVirtualFile().getPath()));
            if (classes.size() <= 1) {
                return (PhpClass) classes.iterator().next();
            }

            String fqn = PhpLangUtil.toPresentableFQN(StringUtil.notNullize(((PhpClass) classes.iterator().next()).getFQN()));
            showErrorMessage(project, PhpBundle.message("quickfix.multiple.target.class.resolve", new Object[]{fqn}), reference);
        } else {
            showErrorMessage(project, PhpBundle.message("quickfix.cannot.find.target.class", new Object[]{reference.getText()}), reference);
        }

        return null;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        MemberReference reference = (MemberReference) PsiTreeUtil.getParentOfType(element, MemberReference.class, false);
        if (reference != null) {
            PhpClass klass = findClassWithValidation(project, element, reference);
            if (klass != null) {
                WriteAction.run(() -> {
                    boolean staticRef = reference.getReferenceType().isStatic();
                    Collection<PhpParameterInfo> infos = getParametersInfos(reference);
                    Method method = (Method) CodeInsightUtilCore.forcePsiPostprocessAndRestoreElement(buildMethod(element, makeParameterList(project, infos, klass, false, true), element.getText(), staticRef, klass));
                    PhpClass containingClass = method.getContainingClass();
                    if (containingClass != null) {
                        runMethodTemplate(project, method, containingClass, infos);
                    }
                });
            }
        }
    }


    protected static Method buildMethod(PsiElement element, String parameterList, String functionName, boolean staticRef, @NotNull PhpClass klass) {
        StringBuilder template = new StringBuilder();
        if (PhpCodeUtil.belongsTo(element, klass)) {
            template.append("private");
        } else {
            template.append("public");
        }

        template.append(" ");
        template.append(staticRef ? "static " : "");
        template.append("function ");
        template.append(functionName);
        template.append(parameterList);
        if (klass.isInterface()) {
            template.append(";");
        } else {
            template.append("{\n}");
        }

        Method templateMethod = PhpCodeUtil.createMethodFromTemplate(klass, klass.getProject(), template.toString());

        assert templateMethod != null;

        PsiElement inserted = PhpCodeEditUtil.insertClassMember(klass, templateMethod);
        return (Method) inserted;
    }

    static @Nullable PhpClass findClassWithValidation(@NotNull Project project, @NotNull PsiElement element, @NotNull MemberReference reference) {
        PhpClass klass = resolveClassWithErrorReporting(project, reference, false);
        if (klass != null && FileModificationService.getInstance().prepareFileForWrite(klass.getContainingFile())) {
            String error = validateClass(project, klass);
            if (StringUtil.isNotEmpty(error)) {
                showErrorMessage(project, error, element);
                return null;
            } else {
                return klass;
            }
        } else {
            return null;
        }
    }


    private static void runMethodTemplate(final @NotNull Project project, @NotNull Method method, @NotNull PhpClass klass, Collection<PhpParameterInfo> infos) {
        PhpAddFieldDeclarationQuickFix.runTemplate(method, createTemplate(method, klass, infos), new TemplateEditingAdapter() {
            public void templateFinished(@NotNull Template template1, boolean brokenOff) {
                if (!brokenOff) {
                    PhpCodeEditUtil.setupMethodBody(project);
                }
            }
        });
    }
}
