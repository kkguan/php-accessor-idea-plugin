package com.free2one.accessor;

import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.free2one.accessor.method.AccessorMethod;
import com.free2one.accessor.settings.AccessorSettings;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AccessorFinderService {

    private final Project project;

    public AccessorFinderService(Project project) {
        this.project = project;
    }

    public <T extends PhpTypedElement> Map<String, Method> findSetterMethods(T phpTypedElement) {
        Map<String, Method> accessMethods = new HashMap<>();
        PhpType pendingType = phpTypedElement.getType().isComplete() ? phpTypedElement.getType() : phpTypedElement.getGlobalType();
        for (String type : pendingType.getTypes()) {
            if (PhpType.isPrimitiveType(type)) {
                return accessMethods;
            }
        }

        for (String classname : pendingType.getTypes()) {
            Collection<PhpClass> phpNamedElements = PhpIndex.getInstance(project).getClassesByFQN(classname);
            for (PhpClass phpClass : phpNamedElements) {
                phpClass.getMethods().stream()
                        .filter(method -> method.getName().startsWith("set"))
                        .forEach(method -> accessMethods.put(method.getName(), method));
            }
        }

        return accessMethods;
    }

    public Collection<Method> getGeneratedAccessorsByPhpType(PhpType phpType) {
        Collection<Method> accessMethods = new ArrayList<>();
        AccessorSettings settings = project.getService(AccessorSettings.class);

        for (String classname : phpType.getTypes()) {
            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(project).getClassesByFQN(classname)
                    .stream()
                    .filter(clazz -> settings.containProxyDirectory(clazz.getContainingFile().getVirtualFile().getPath()))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (phpNamedElements.isEmpty()) {
                continue;
            }

            phpNamedElements.forEach(clazz -> accessMethods.addAll(clazz.getMethods()));
        }

        return accessMethods;
    }

//    public Collection<PsiElement> getGeneratedAccessorOfField(Field field) {
//        Collection<PsiElement> elements = new ArrayList<>();
//        PhpClass containingClass = field.getContainingClass();
//
//        if (containingClass == null) {
//            return elements;
//        }
//
//        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
//        ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(containingClass.getFQN());
//        if (classMetadata == null) {
//            return elements;
//        }
//
//        Collection<String> relevantMethods = classMetadata.findMethodNamesFromFieldName(field.getName());
//        if (relevantMethods.isEmpty()) {
//            return elements;
//        }
//
//        Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(project).getClassesByFQN(classMetadata.getClassname());
//        AccessorSettings settings = AccessorSettings.getInstance(project);
//        for (PhpClass clazz : phpNamedElements) {
//            if (!settings.containProxyDirectory(clazz.getContainingFile().getVirtualFile().getPath())) {
//                continue;
//            }
//
//            Collection<Method> relatedMethods = clazz.getMethods().stream()
//                    .filter(method -> relevantMethods.contains(method.getName()) && containingClass.findMethodByName(method.getName()) == null)
//                    .toList();
//            elements.addAll(relatedMethods);
//        }
//
//        return elements;
//    }

    public Collection<PsiElement> getGeneratedAccessorOfField(Field field) {
        Collection<PsiElement> elements = new ArrayList<>();
        PhpClass containingClass = field.getContainingClass();

        if (containingClass == null) {
            return elements;
        }

        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
        ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(containingClass.getFQN());
        if (classMetadata == null) {
            return elements;
        }

        Collection<String> relevantMethods = classMetadata.findMethodNamesFromFieldName(field.getName());
        if (relevantMethods.isEmpty()) {
            return elements;
        }

        DumbService dumbService = DumbService.getInstance(project);
        if (dumbService.isDumb()) {
            dumbService.runWhenSmart(() -> {
                Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(project).getClassesByFQN(classMetadata.getClassname());
                AccessorSettings settings = AccessorSettings.getInstance(project);
                for (PhpClass clazz : phpNamedElements) {
                    if (!settings.containProxyDirectory(clazz.getContainingFile().getVirtualFile().getPath())) {
                        continue;
                    }

                    Collection<Method> relatedMethods = clazz.getMethods().stream()
                            .filter(method -> relevantMethods.contains(method.getName()) && containingClass.findMethodByName(method.getName()) == null)
                            .toList();
                    elements.addAll(relatedMethods);
                }
            });
        } else {
            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(project).getClassesByFQN(classMetadata.getClassname());
            AccessorSettings settings = AccessorSettings.getInstance(project);
            for (PhpClass clazz : phpNamedElements) {
                if (!settings.containProxyDirectory(clazz.getContainingFile().getVirtualFile().getPath())) {
                    continue;
                }

                Collection<Method> relatedMethods = clazz.getMethods().stream()
                        .filter(method -> relevantMethods.contains(method.getName()) && containingClass.findMethodByName(method.getName()) == null)
                        .toList();
                elements.addAll(relatedMethods);
            }
        }

        return elements;
    }

    public Method getGeneratedAccessorOfField(Field field, Class<? extends AccessorMethod> accessorMethod) {
        PhpClass containingClass = field.getContainingClass();
        if (containingClass == null) {
            return null;
        }

        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
        ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(containingClass.getFQN());
        if (classMetadata == null) {
            return null;
        }

        String methodName = classMetadata.findMethodNameFromFieldName(field.getName(), accessorMethod);
        if (methodName == null) {
            return null;
        }

        Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(project).getClassesByFQN(classMetadata.getClassname());
        AccessorSettings settings = AccessorSettings.getInstance(project);
        for (PhpClass clazz : phpNamedElements) {
            if (!settings.containProxyDirectory(clazz.getContainingFile().getVirtualFile().getPath())) {
                continue;
            }

            Method method = clazz.findMethodByName(methodName);
            if (method != null) {
                return method;
            }
        }

        return null;
    }

    public ClassMetadata getAccessorMetadata(PhpType type) {
        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
        for (String classname : type.getTypes()) {
            ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(classname);
            if (classMetadata == null) {
                continue;
            }

            return classMetadata;
        }

        return null;
    }

    public ClassMetadata getAccessorMetadata(Collection<? extends PhpNamedElement> phpNamedElements) {
        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
        for (PhpNamedElement phpNamedElement : phpNamedElements) {
            if (!(phpNamedElement instanceof PhpClass phpClass)) {
                continue;
            }

            if (!AnnotationSearchUtil.isAnnotatedWith(phpClass, PhpAccessorClassnames.Data)) {
                continue;
            }

            ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(phpNamedElement.getFQN());
            if (classMetadata == null) {
                continue;
            }

            return classMetadata;
        }

        return null;
    }

    public ClassMetadata getAccessorMetadata(PhpClass phpClass) {
        if (!AnnotationSearchUtil.isAnnotatedWith(phpClass, PhpAccessorClassnames.Data)) {
            return null;
        }

        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
        return methodMetaDataRepository.getFromClassname(phpClass.getFQN());
    }

    public ClassMetadata getAccessorMetadata(String classFQN) {
        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
        return methodMetaDataRepository.getFromClassname(classFQN);
    }


    public boolean isAccessor(MethodReference reference) {
        PhpType sourceElementType = reference.getType();
        PhpType pendingType = sourceElementType.isComplete() ? sourceElementType : reference.getGlobalType();

        MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(reference.getProject());
        AccessorSettings settings = reference.getProject().getService(AccessorSettings.class);
        for (String classname : pendingType.getTypes()) {
            Collection<PhpClass> phpNamedElements = PhpIndex.getInstance(reference.getProject()).getClassesByFQN(classname);
            for (PhpClass phpClass : phpNamedElements) {
                if (settings.containSettingDirectories(phpClass.getContainingFile().getVirtualFile().getPath())) {
                    continue;
                }

                if (!AnnotationSearchUtil.isAnnotatedWith(phpClass, PhpAccessorClassnames.Data)) {
                    continue;
                }

                ClassMetadata classMetadata = ReadAction.compute(() -> methodMetaDataRepository.getFromClassname(phpClass.getFQN()));
                if (classMetadata == null) {
                    continue;
                }

                for (AccessorMethod method : classMetadata.getMethods()) {
                    if (method.getMethodName().equals(reference.getName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
