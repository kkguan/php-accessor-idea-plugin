package com.free2one.accessor.codeInspection;

import com.free2one.accessor.PhpAccessorClassnames;
import com.free2one.accessor.codeInspection.quickfix.AddMethodDeclarationQuickFix;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.codeInspection.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.inspections.PhpRenameWrongReferenceQuickFix;
import com.jetbrains.php.lang.inspections.PhpUndefinedFieldInspection;
import com.jetbrains.php.lang.inspections.PhpUndefinedMethodInspection;
import com.jetbrains.php.lang.inspections.quickfix.PhpAddMethodTagQuickFix;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.MemberReferenceImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


/**
 * 以下编码基本从PhpStorm复制，未做任何优化
 */
public class UndefinedMethodInspection extends PhpUndefinedMethodInspection {
    private static final LocalQuickFix[] FIXES;

    static {
        FIXES = new LocalQuickFix[]{AddMethodDeclarationQuickFix.INSTANCE, PhpAddMethodTagQuickFix.INSTANCE};
    }


    public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {

        return new PhpElementVisitor() {
            public void visitPhpMethodReference(MethodReference reference) {
                this.processPhpReference(reference, reference.getClassReference(), reference.isStatic());
            }

            public void visitPhpCallableMethod(PhpCallableMethod reference) {
                this.processPhpReference(reference, reference.getClassReference(), reference.isStatic());
            }

            private void processPhpReference(@NotNull PhpReference reference, @Nullable PhpExpression classReference, boolean isStatic) {
//                if (reference == null) {
//                    return;
//                }

                ASTNode nameNode = reference.getNameNode();
                if (nameNode == null || classReference == null) {
                    return;
                }
//                if (nameNode != null) {
//                if (classReference != null) {
                PsiElement psi = nameNode.getPsi();
                if (MemberReferenceImpl.chainIsTooDeep(classReference)) {
                    return;
                }

                if (classReference instanceof PhpReference && PhpUndefinedFieldInspection.noResolvedDeclarationExists((PhpReference) classReference)) {
                    return;
                }
//                if (!MemberReferenceImpl.chainIsTooDeep(classReference)) {
//                if (!(classReference instanceof PhpReference) || !PhpUndefinedFieldInspection.noResolvedDeclarationExists((PhpReference) classReference)) {
                Project project = reference.getProject();
                PhpType type = classReference.getGlobalType();
                if (!UndefinedMethodInspection.this.isWarnOnMixed() && isUnresolvedType(project, classReference, type)) {
                    return;
                }
//                if (UndefinedMethodInspection.this.isWarnOnMixed() || !isUnresolvedType(project, classReference, type)) {
                if (!UndefinedMethodInspection.this.isWarnOnMixed() && UndefinedMethodInspection.stringDefinedViaClassExists(classReference, type)) {
                    return;
                }
//                if (UndefinedMethodInspection.this.isWarnOnMixed() || !UndefinedMethodInspection.stringDefinedViaClassExists(classReference, type)) {
                Reachability reachability = PhpUndefinedMethodInspection.findDfaReachability(reference, classReference);
                if (reachability == PhpUndefinedMethodInspection.Reachability.DEFINED) {
                    return;
                }
//                if (reachability != PhpUndefinedMethodInspection.Reachability.DEFINED) {
                PhpIndex index = PhpIndex.getInstance(project);
                if (reference.multiResolve(false).length != 0) {
                    return;
                }
                /*
                  新增逻辑 start
                 */
                if (classReference instanceof Variable variableRef) {
                    Collection<PhpClass> phpNamedElements = index.getClassesByFQN(variableRef.getFQN());
                    if (AnnotationSearchUtil.isAnnotatedWith(phpNamedElements, PhpAccessorClassnames.Data)) {
                        for (PhpClass clazz : phpNamedElements) {
                            if (clazz.getMethods().stream().anyMatch(method -> method.getName().equals(psi.getText()))) {
                                return;
                            }
                        }
                    }
                }
                /*
                  新增逻辑 end
                 */

//                if (reference.multiResolve(false).length == 0) {
                boolean hasMagic = false;
                if (UndefinedMethodInspection.this.DOWNGRADE_SEVERITY) {
                    String magicMethod = isStatic ? "__callStatic" : "__call";
                    hasMagic = PhpCodeInsightUtil.hasMagicMethod(type, index, magicMethod);
                    if (!hasMagic) {
                        hasMagic = PhpUndefinedFieldInspection.isTraitWithMagicMethodsInAllUsages(type, index, magicMethod);
                    }
                }

                assert psi != null;
//                                                System.out.println(psi.getText());
                holder.registerProblem(this.createDescriptor(psi, reference, reachability, type, hasMagic));
//                }

//                }
//                }
//                }
//                }
//                }
//                }
//                }
            }

            private ProblemDescriptor createDescriptor(@NotNull PsiElement nameIdentifier, PhpReference methodReference, Reachability reachability, PhpType type, boolean hasMagic) {
                LocalQuickFix[] fixes = LocalQuickFix.EMPTY_ARRAY;
                if (UndefinedMethodInspection.isQuickFixApplicable(nameIdentifier, isOnTheFly)) {
                    fixes = PhpRenameWrongReferenceQuickFix.appendQuickFix(methodReference, methodReference instanceof MethodReference ? UndefinedMethodInspection.FIXES : fixes);
                }

                int reachabilityChoice = reachability == PhpUndefinedMethodInspection.Reachability.MIGHT_BE_DEFINED ? 1 : 0;
                String template = isOnTheFly ? PhpBundle.message("method.is.undefined.in.class", new Object[]{type.toStringRelativized(methodReference.getNamespaceName()), reachabilityChoice}) : PhpBundle.message("method.is.undefined", new Object[]{reachabilityChoice});
                ProblemHighlightType highlightType = (!UndefinedMethodInspection.this.DOWNGRADE_SEVERITY || !hasMagic) && reachability != PhpUndefinedMethodInspection.Reachability.MIGHT_BE_DEFINED ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING : ProblemHighlightType.WEAK_WARNING;
                return InspectionManager.getInstance(nameIdentifier.getProject()).createProblemDescriptor(nameIdentifier, template, isOnTheFly, fixes, highlightType);
            }
        };
    }

    public static boolean isUnresolvedType(Project project, @Nullable PhpExpression classReference, PhpType classReferenceType) {
        PhpType filteredClassReferenceType = classReferenceType.filterUnknown().filterNull();
        if (!PhpType.isSubType(PhpType.MIXED, filteredClassReferenceType) && !PhpType.isSubType(PhpType.OBJECT, filteredClassReferenceType) && !filteredClassReferenceType.isEmpty()) {
            PhpIndex index = PhpIndex.getInstance(project);
            PhpType typesWithoutPrimitive = classReferenceType.filterPrimitives().filterOut(PhpType::isPluralType).filter(PhpType.RESOURCE).filter(PhpType.NUMBER);
            if (PhpLangUtil.isThisReference(classReference)) {
                typesWithoutPrimitive = typesWithoutPrimitive.filterOut(PhpType::isAnonymousClass);
            }

            return !typesWithoutPrimitive.isEmpty() && typesWithoutPrimitive.getTypes().stream().flatMap((fqn) -> {
                return index.getAnyByFQN(fqn).stream();
            }).limit(1L).findAny().isEmpty();
        } else {
            return true;
        }
    }

    private static boolean isQuickFixApplicable(@NotNull PsiElement element, boolean isOnTheFly) {
        if (element == null) {
            return false;
        }

        return isOnTheFly && element.getContainingFile().isWritable();
    }
}
