package com.free2one.accessor.findUsages;

import com.free2one.accessor.findUsages.handler.AccessorFindUsagesHandler;
import com.free2one.accessor.findUsages.handler.FieldFindUsagesHandler;
import com.free2one.accessor.findUsages.handler.PhpClassFindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.findUsages.PhpFindUsagesHandlerFactory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class AccessorFindUsagesHandlerFactory extends PhpFindUsagesHandlerFactory {

    private final String[] handlers = {
            FieldFindUsagesHandler.class.getName(),
            PhpClassFindUsagesHandler.class.getName(),
    };

    public AccessorFindUsagesHandlerFactory(Project project) {
        super(project);
    }

    @Override
    public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, @NotNull OperationMode operationMode) {
        for (String handler : handlers) {
            try {
                Class<?> c = Class.forName(handler);
                Constructor<?> cons = c.getConstructor(PsiElement.class); //获取有两个参数的构造器
                Object obj = cons.newInstance(element);
                AccessorFindUsagesHandler handler1 = (AccessorFindUsagesHandler) obj;
                if (handler1.findable()) {
                    return handler1;
                }
            } catch (Exception ignored) {
            }
        }

        return super.createFindUsagesHandler(element, operationMode);
    }

}
