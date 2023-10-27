package com.free2one.accessor.actions.generation;

import com.free2one.accessor.AccessorFinderService;
import com.free2one.accessor.AccessorGeneratorService;
import com.free2one.accessor.PhpAccessorClassnames;
import com.free2one.accessor.method.GetterMethod;
import com.free2one.accessor.method.SetterMethod;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.actions.PhpNamedElementNode;
import com.jetbrains.php.lang.actions.generation.PhpGenerateFieldAccessorHandlerBase;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.intentions.generators.PhpAccessorMethodData;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.refactoring.importReferences.PhpClassReferenceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class GenerateFieldAccessorHandlerBase extends PhpGenerateFieldAccessorHandlerBase {
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PhpFile phpFile = (PhpFile) file;
        PhpClass targetClass = PhpCodeEditUtil.findClassAtCaret(editor, phpFile);

        if (targetClass == null) {
            return;
        }

        PhpNamedElementNode[] fieldsToShow = this.collectFields(targetClass);
        if (fieldsToShow.length == 0) {
            if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
                HintManager.getInstance().showErrorHint(editor, this.getErrorMessage());
            }
        } else {
            PhpNamedElementNode[] members = this.chooseMembers(fieldsToShow, true, file.getProject());
            if (members == null || members.length == 0) {
                return;
            }

            int insertPos = getSuitableEditorPosition(editor, (PhpFile) file);
            boolean genByPhpAccessor = AnnotationSearchUtil.isAnnotatedWith(targetClass, PhpAccessorClassnames.Data);
            ApplicationManager.getApplication().runWriteAction(() -> {
                if (genByPhpAccessor) {
                    targetClass.getProject().getService(AccessorGeneratorService.class).generate(targetClass.getContainingFile().getVirtualFile().getPath(), result -> {
                        if (!result.isSuccess()) {
                            return;
                        }

                        genAccessorsToFile(members, targetClass, project, editor, file, insertPos, true);
                    });
                } else {
                    genAccessorsToFile(members, targetClass, project, editor, file, insertPos, false);
                }
            });
        }
    }

    // same as in PhpGenerateFieldAccessorHandlerBase,just to make it accessible
    private static int getSuitableEditorPosition(Editor editor, PhpFile phpFile) {
        PsiElement currElement = phpFile.findElementAt(editor.getCaretModel().getOffset());
        if (currElement != null) {
            PsiElement parent = currElement.getParent();

            for (PsiElement prevParent = currElement; parent != null && !(parent instanceof PhpFile); parent = parent.getParent()) {
                if (isClassMember(parent)) {
                    return getNextPos(parent);
                }

                if (parent instanceof PhpClass) {
                    while (prevParent != null) {
                        if (isClassMember(prevParent) || PhpPsiUtil.isOfType(prevParent, PhpTokenTypes.chLBRACE)) {
                            return getNextPos(prevParent);
                        }

                        prevParent = prevParent.getPrevSibling();
                    }

                    for (PsiElement classChild = parent.getFirstChild(); classChild != null; classChild = classChild.getNextSibling()) {
                        if (PhpPsiUtil.isOfType(classChild, PhpTokenTypes.chLBRACE)) {
                            return getNextPos(classChild);
                        }
                    }
                }

                prevParent = parent;
            }
        }

        return -1;
    }

    // Part of the code is derived from PhpGenerateFieldAccessorHandlerBase.invoke
    // Need to optimize
    private void genAccessorsToFile(PhpNamedElementNode[] members, PhpClass targetClass, Project project, Editor editor, PsiFile file, int insertPos, boolean genByPhpAccessor) {
        CommonCodeStyleSettings settings = CodeStyle.getLanguageSettings(file, PhpLanguage.INSTANCE);
        boolean currLineBreaks = settings.KEEP_LINE_BREAKS;
        int currBlankLines = settings.KEEP_BLANK_LINES_IN_CODE;
        settings.KEEP_LINE_BREAKS = false;
        settings.KEEP_BLANK_LINES_IN_CODE = 0;
        PhpClassReferenceResolver resolver = new PhpClassReferenceResolver();
        StringBuffer textBuf = new StringBuffer();
        PhpNamedElementNode[] var11 = members;
        int var12 = members.length;
        int[] insertedElementCount = {0};

        ApplicationManager.getApplication().runReadAction(() -> {
            for (int var13 = 0; var13 < var12; ++var13) {
                PhpNamedElementNode member = var11[var13];
                PsiElement field = member.getPsiElement();
                PhpAccessorMethodData[] accessors = createAccessorsProxy(genByPhpAccessor, targetClass, field);
                PhpAccessorMethodData[] var17 = accessors;
                int var18 = accessors.length;
                for (int var19 = 0; var19 < var18; ++var19) {
                    PhpAccessorMethodData accessor = var17[var19];
                    if (accessor != null) {
                        PhpDocComment comment = accessor.getDocComment();
                        if (comment != null) {
                            textBuf.append(comment.getText());
                            if (field instanceof Field) {
                                PhpDocComment originalComment = ((Field) field).getDocComment();
                                if (originalComment != null) {
                                    resolver.processElement(originalComment);
                                    insertedElementCount[0]++;
                                }
                            }
                        }

                        textBuf.append('\n');
                        textBuf.append(accessor.getMethod().getText());
                    }
                }
            }
        });

        if (textBuf.length() <= 0 || insertPos < 0) {
            return;
        }

        // Must not change document outside command or undo-transparent action
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().insertString(insertPos, textBuf);

            int endPos = insertPos + textBuf.length();
            CodeStyleManager.getInstance(project).reformatText(file, insertPos, endPos);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            List<PsiElement> insertedElements = collectInsertedElements(file, insertPos, insertedElementCount[0]);
            if (insertedElements != null && !insertedElements.isEmpty()) {
                PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(insertedElements.get(0));
                if (scope != null) {
                    resolver.importReferences(scope, insertedElements);
                }
            }

            settings.KEEP_LINE_BREAKS = currLineBreaks;
            settings.KEEP_BLANK_LINES_IN_CODE = currBlankLines;
        });
    }

    // same as in PhpGenerateFieldAccessorHandlerBase,just to make it accessible
    private static boolean isClassMember(PsiElement element) {
        return element instanceof PhpClassFieldsList || element instanceof Method;
    }

    // same as in PhpGenerateFieldAccessorHandlerBase,just to make it accessible
    private static int getNextPos(PsiElement element) {
        PsiElement next = element.getNextSibling();
        return next != null ? next.getTextOffset() : -1;
    }

    private PhpAccessorMethodData[] createAccessorsProxy(boolean genByPhpAccessor, PhpClass targetClass, PsiElement field) {
        if (genByPhpAccessor) {
            List<PhpAccessorMethodData> accessorMethodData = new ArrayList<>();
            for (PhpAccessorMethodData phpAccessorMethodData : this.createAccessors(targetClass, field)) {
                Method method = null;
                switch (phpAccessorMethodData.getAccessorType()) {
                    case GETTER ->
                            method = targetClass.getProject().getService(AccessorFinderService.class).getGeneratedAccessorOfField((Field) field, GetterMethod.class);
                    case SETTER, FLUENT_SETTER ->
                            method = targetClass.getProject().getService(AccessorFinderService.class).getGeneratedAccessorOfField((Field) field, SetterMethod.class);

                }
                if (method != null) {
                    accessorMethodData.add(new PhpAccessorMethodData(phpAccessorMethodData.getClassField(), phpAccessorMethodData.getTargetClass(), phpAccessorMethodData.getDocComment(), method, phpAccessorMethodData.getAccessorType()));
                } else {
                    accessorMethodData.add(phpAccessorMethodData);
                }
            }

            return accessorMethodData.toArray(new PhpAccessorMethodData[0]);
        } else {

            return this.createAccessors(targetClass, field);
        }
    }

    // same as in PhpGenerateFieldAccessorHandlerBase,just to make it accessible
    private static @Nullable List<PsiElement> collectInsertedElements(@NotNull PsiFile file, int startPos, int count) {
        PsiElement element = file.findElementAt(startPos);
        if (element == null) {
            return null;
        } else {
            List<PsiElement> inserted = new ArrayList(count);
            for (PsiElement sibling = PhpPsiUtil.getNextSiblingIgnoreWhitespace(element, false); sibling != null && inserted.size() < count; sibling = PhpPsiUtil.getNextSiblingIgnoreWhitespace(sibling, true)) {
                if (sibling instanceof PhpDocComment) {
                    inserted.add(sibling);
                }
            }

            return inserted;
        }
    }
}
