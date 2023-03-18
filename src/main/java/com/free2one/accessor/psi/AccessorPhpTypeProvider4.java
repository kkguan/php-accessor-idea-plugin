package com.free2one.accessor.psi;

import com.free2one.accessor.PhpAccessorClassnames;
import com.free2one.accessor.meta.ClassMetadata;
import com.free2one.accessor.meta.MethodMetaDataRepository;
import com.free2one.accessor.settings.AccessorSettings;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.Variable;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public class AccessorPhpTypeProvider4 implements PhpTypeProvider4 {

    @Override
    public char getKey() {
        return 'Ȣ';
    }

    @Override
    public @Nullable PhpType getType(PsiElement psiElement) {
        if (!(psiElement instanceof Variable) || !psiElement.textMatches("$this")) {
            return null;
        }

        if (psiElement.getContainingFile().getVirtualFile() == null ||
                psiElement.getContainingFile().getVirtualFile().getPath().contains(psiElement.getProject().getBasePath() + File.separator + "vendor")) {
            return null;
        }

        AccessorSettings settings = psiElement.getProject().getService(AccessorSettings.class);
        if (settings.containSettingDirectories(psiElement.getContainingFile().getVirtualFile().getPath())) {
            return null;
        }

        PsiElement targetElement = ((Variable) psiElement).resolve();
        if (targetElement instanceof PhpClass phpClass) {
            if (!AnnotationSearchUtil.isAnnotatedWith(phpClass, PhpAccessorClassnames.Data)) {
                return null;
            }

//            ClassMetadata classMetadata = ReadAction.compute(() -> {
//                MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(psiElement.getProject());
//                return methodMetaDataRepository.getFromClassname(phpClass.getFQN());
//            });
            MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(psiElement.getProject());
            ClassMetadata classMetadata = methodMetaDataRepository.getFromClassname(phpClass.getFQN());
            if (classMetadata == null) {
                return null;
            }

            PhpType phpType = new PhpType();
            phpType.add("#" + getKey() + phpClass.getFQN() + getKey());
            return phpType;
        }

        return null;
    }

    @Override
    public @Nullable PhpType complete(String s, Project project) {
        int indexOfSign = s.indexOf(getKey());
        int indexOfDelimiter = s.indexOf(getKey(), indexOfSign + 1);
        if (indexOfSign == -1 || indexOfDelimiter == -1) {
            return null;
        }

        String classFqn = s.substring(indexOfSign + 1, indexOfDelimiter);
        ClassMetadata classMetadata = ReadAction.compute(() -> {
            MethodMetaDataRepository methodMetaDataRepository = new MethodMetaDataRepository(project);
            return methodMetaDataRepository.getFromClassname(classFqn);
        });
        if (classMetadata == null) {
            return null;
        }

        return ReadAction.compute(() -> {
            Collection<? extends PhpClass> phpNamedElements = PhpIndex.getInstance(project).getTraitsByName(classMetadata.getAccessorClassname());
            for (PhpClass phpClass : phpNamedElements) {
                return new PhpType().add(phpClass);
            }
            return null;
        });
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
