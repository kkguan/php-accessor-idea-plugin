package com.free2one.accessor.actions;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.AccessorFinderService;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.PsiElementMemberChooserObject;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.parser.PhpStubElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import icons.PhpIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenerateObjectSettersAction extends CodeInsightAction {

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);

        return file instanceof PhpFile && element instanceof PsiWhiteSpace;
    }

    @Override
    protected @NotNull CodeInsightActionHandler getHandler() {
        return new LanguageCodeInsightActionHandler() {
            @Override
            public boolean isValidFor(Editor editor, PsiFile file) {
                return true;
            }

            @Override
            public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
                int cursorPosition = editor.getCaretModel().getOffset();
                PsiElement sourceElement = file.findElementAt(cursorPosition);
                if (sourceElement == null) {
                    return;
                }

                PsiElement sourceElementParent = sourceElement.getParent();
                List<SetterObjectElement> setterElements = new ArrayList<>();
                PsiElementVisitor visitor = new Visitor(sourceElementParent, setterElements, cursorPosition);
                file.acceptChildren(visitor);

                if (setterElements.isEmpty()) {
                    if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
                        HintManager.getInstance().showErrorHint(editor, AccessorBundle.message("action.accessor.actions.generate-object-setters-action.no-elements-to-generate"));
                    }
                    return;
                }

                ApplicationManager.getApplication().invokeLater(() -> {
                    MemberChooser<SetterObjectElement> chooser = new MemberChooser<>(setterElements.toArray(new SetterObjectElement[0]), true, false, project);
                    chooser.setCopyJavadocVisible(false);
                    chooser.show();

                    List<SetterObjectElement> selectedNodes = chooser.getSelectedElements();
                    if (selectedNodes == null) {
                        return;
                    }

                    StringBuffer textBuf = new StringBuffer();
                    AccessorFinderService accessorFinderService = project.getService(AccessorFinderService.class);
                    for (SetterObjectElement selectedNode : selectedNodes) {
                        PsiElement targetElement = selectedNode.getPsiElement();
                        Map<String, Method> methods = accessorFinderService.findSetterMethods((PhpTypedElement) targetElement);

                        if (targetElement instanceof VariableImpl variable) {
                            for (Method method : methods.values()) {
                                textBuf.append("$").append(variable.getName()).append("->").append(method.getName()).append("();").append("\n");
                            }
                        } else if (targetElement instanceof Parameter parameter) {
                            for (Method method : methods.values()) {
                                textBuf.append("$").append(parameter.getName()).append("->").append(method.getName()).append("();").append("\n");
                            }
                        } else if (targetElement instanceof Field field) {
                            for (Method method : methods.values()) {
                                textBuf.append("$this->").append(field.getName()).append("->").append(method.getName()).append("();").append("\n");
                            }
                        }
                    }

                    if (textBuf.isEmpty()) {
                        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
                            HintManager.getInstance().showErrorHint(editor, AccessorBundle.message("action.accessor.actions.generate-object-setters-action.no-setter-methods-found"));
                        }
                        return;
                    }

                    ApplicationManager.getApplication().runWriteAction(
                            () -> CommandProcessor.getInstance().executeCommand(
                                    project,
                                    () -> {
                                        editor.getDocument().insertString(cursorPosition, textBuf);
                                        int endPos = cursorPosition + textBuf.length();
                                        CodeStyleManager.getInstance(project).reformatText(file, cursorPosition, endPos);
                                        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
                                    },
                                    "GenerateObjectSetters",
                                    null
                            )
                    );
                });
            }
        };
    }

    private static class Visitor extends PhpElementVisitor {

        private final int cursorPosition;

        private final PsiElement sourceElementParent;

        private final List<SetterObjectElement> setterElements;

        public Visitor(PsiElement sourceElementParent, List<SetterObjectElement> setterElements, int cursorPosition) {
            this.sourceElementParent = sourceElementParent;
            this.setterElements = setterElements;
            this.cursorPosition = cursorPosition;
        }

        @Override
        public void visitPhpElement(PhpPsiElement element) {
            if (element.getFirstChild() != null) {
                element.acceptChildren(this);
            }
        }

        @Override
        public void visitPhpParameter(Parameter parameter) {
            if (isDefinedBelowCursor(parameter)) {
                return;
            }
            if (parameter.getParent().getParent().equals(sourceElementParent.getParent())) {
                setterElements.add(new SetterObjectElement(parameter));
            }
        }

        private boolean isDefinedBelowCursor(PsiElement element) {
            return element.getTextRange().getEndOffset() >= cursorPosition;
        }

        @Override
        public void visitPhpVariable(Variable variable) {
            if (isDefinedBelowCursor(variable)) {
                return;
            }
            for (String type :
                    variable.getType().getTypes()) {
                if (!PhpType.isPrimitiveType(type) && variable.getParent().getParent().getParent().equals(sourceElementParent)) {
                    setterElements.add(new SetterObjectElement(variable));
                    break;
                }
            }

        }

        @Override
        public void visitPhpField(Field field) {
            if (isDefinedBelowCursor(field)) {
                return;
            }

            if (field.getParent().getParent() == null
                    || sourceElementParent.getParent().getParent() == null
                    || !field.getParent().getParent().equals(sourceElementParent.getParent().getParent())) {
                return;
            }

            for (String type : field.getType().getTypes()) {
                if (!PhpType.isPrimitiveType(type)) {
                    setterElements.add(new SetterObjectElement(field));
                    break;
                }
            }
        }
    }


    private static class SetterObjectElement extends PsiElementMemberChooserObject implements ClassMember {
        SetterObjectElement(@NotNull PsiElement psiElement) {
            super(psiElement, getText(psiElement), getIcon(psiElement));
        }

        private static String getText(PsiElement element) {
            if (element instanceof Function) {
                PsiElement parent = element.getParent();
                if (PhpPsiUtil.isOfType(parent, PhpElementTypes.CLOSURE)) {
                    StringBuilder funcHeader = new StringBuilder();

                    for (PsiElement child = element.getFirstChild(); !PhpPsiUtil.isOfType(child, PhpElementTypes.ANY_GROUP_STATEMENT); child = child.getNextSibling()) {
                        funcHeader.append(child.getText());
                    }

                    String var10000 = funcHeader.toString().trim().replace("\n", "");
                    return var10000.replaceAll("\\s", "") + "{...}";
                }
            }

            if (element instanceof PhpNamedElement) {
                return ((PhpNamedElement) element).getName();
            } else {
                return element instanceof PhpFile ? ((PhpFile) element).getName() : element.getText();
            }
        }

        private static Icon getIcon(PsiElement element) {
            if (element instanceof PhpNamedElement) {
                return ((PhpNamedElement) element).getIcon();
            } else {
                return element instanceof PhpFile ? PhpIcons.PhpIcon : null;
            }
        }

        public MemberChooserObject getParentNodeDelegate() {
            PsiElement element = this.getPsiElement();
            PsiElement parent = element.getParent();
            if (PhpPsiUtil.isOfType(element, PhpStubElementTypes.DEFINE, PhpStubElementTypes.CONST) && parent instanceof Statement) {
                parent = parent.getParent();
            }

            if (parent instanceof GroupStatement || element instanceof Field) {
                parent = parent.getParent();
            }

            if (PhpPsiUtil.isOfType(parent, PhpElementTypes.CLOSURE)) {
                while (parent != null) {
                    if (parent instanceof AssignmentExpression) {
                        parent = ((AssignmentExpression) parent).getVariable();
                        break;
                    }

                    if (parent instanceof PhpFile) {
                        break;
                    }

                    parent = parent.getParent();
                }
            }

            if (parent == null) {
                parent = element;
            }

            return new SetterObjectElement(parent);
        }
    }

}
