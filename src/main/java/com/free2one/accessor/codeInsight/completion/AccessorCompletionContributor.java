package com.free2one.accessor.codeInsight.completion;

import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccessorCompletionContributor extends CompletionContributor {
    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (Optional.ofNullable(parameters.getPosition().getPrevSibling()).isEmpty() ||
                !parameters.getPosition().getPrevSibling().getText().equals("->")) {
            return;
        }

        PsiElement element = parameters.getPosition().getPrevSibling().getPrevSibling();
        AccessorSettings settings = element.getProject().getService(AccessorSettings.class);
        if (!(element instanceof Variable) ||
                !((Variable) element).getName().equals("this")) {
            return;
        }

        PhpType phpType = ((Variable) element).getDeclaredType();
        if (!phpType.isComplete()) {
            return;
        }

        for (String classname : phpType.getTypes()) {
            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(element.getProject()).getClassesByFQN(classname)
                    .stream()
                    .filter(clazz -> settings.containProxyDirectory(clazz.getContainingFile().getVirtualFile().getPath()))
                    .collect(Collectors.toCollection(ArrayList::new));
            phpNamedElements.forEach(clazz -> {
                Collection<Method> methods = clazz.getMethods();
                methods.forEach(method -> result.addElement(createLookupElement(method)));
            });
        }
    }

    private PhpLookupElement createLookupElement(Method method) {
        PhpLookupElement phpLookupElement = new PhpLookupElement(method);
        phpLookupElement.bold = true;
        phpLookupElement.handler = (InsertHandler<LookupElement>) (context, item) -> {
            context.getDocument().insertString(context.getTailOffset(), "()");
            int offset = item.getLookupString().startsWith("get") ? 0 : 1;
            context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() - offset);
        };
        return phpLookupElement;
    }

}
