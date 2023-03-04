package com.free2one.accessor.method;

import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpAttribute;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClassProcessor {

    private static final String ProcessingSignature = "\\PhpAccessor\\Attribute\\Data";

    private final MethodMetaDataRepository methodMetaDataRepository;

    public ClassProcessor(Project project) {
        methodMetaDataRepository = new MethodMetaDataRepository(project);
    }

    public void run(PhpClass clazz) {
        if (!needGenerate(clazz)) {
            return;
        }

        Builder builder = new Builder(clazz);
        builder.build();
        if (builder.methodMap.isEmpty()) {
            return;
        }

        ClassMetadata accessorMeta = new ClassMetadata(clazz.getProject().getName(), clazz.getFQN());
        builder.methodMap.forEach((s, method) -> accessorMeta.addMethod(method));
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(clazz.getProject(), () -> methodMetaDataRepository.save(accessorMeta)));
    }

    private boolean needGenerate(PhpClass clazz) {
        Collection<PhpAttribute> attributes = clazz.getAttributes(ProcessingSignature);
        return attributes.size() >= 1;
    }

    private static class Builder {

        private final PhpClass originClass;

        private final Map<String, AccessorMethod> methodMap;

        public Builder(PhpClass clazz) {
            this.originClass = clazz;
            methodMap = new HashMap<>();
        }

        public void build() {
            Collection<Field> fields = originClass.getFields();
            if (fields.isEmpty()) {
                return;
            }

            fields.forEach(field -> {
                //不处理常量
                if (field instanceof ClassConstImpl) {
                    return;
                }

                Map<String, AccessorMethod> method = MethodFactory.createFromField(field);
                Optional.ofNullable(method).ifPresent(methodMap::putAll);
            });
            //先做复制，后续将可能生成新结构
            PhpClass genClazz = (PhpClass) originClass.copy();
            genClazz.acceptChildren(new PhpElementVisitor() {
                @Override
                public void visitPhpMethod(Method method) {
                    methodMap.remove(method.getName());
                }
            });
        }
    }

}
