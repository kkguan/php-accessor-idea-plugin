package com.free2one.accessor.meta;

import com.free2one.accessor.method.AccessorMethod;
import com.free2one.accessor.settings.AccessorSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class MethodMetaDataRepository {

    private final String basePath;

    private final Project project;

    private final String fileExtension = JsonFileType.INSTANCE.getDefaultExtension();

    public MethodMetaDataRepository(Project project) {
        this.project = project;
        this.basePath = project.getBasePath();
    }

//    public ClassMetadata getFromClassname(String classname) {
//        Path phpFilePath = PathManager.findBinFile(getDir() + File.separator + decodeFileName(classname) + "." + fileExtension);
//        if (Optional.ofNullable(phpFilePath).isEmpty()) {
//            return null;
//        }
//
//        Gson gson = new GsonBuilder()
//                .serializeNulls()
//                .registerTypeAdapter(AccessorMethod.class, AccessorMethodDeserializerFactory.create())
//                .create();
//        VirtualFile file = VfsUtil.findFile(phpFilePath, true);
//        if (Optional.ofNullable(file).isEmpty()) {
//            return null;
//        }
//
////        file.refresh(false, false);
//        try {
//            String json = new String(file.getInputStream().readAllBytes());
//            return gson.fromJson(json, ClassMetadata.class);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public ClassMetadata getFromClassname(String classname) {
        Path phpFilePath = PathManager.findBinFile(getDir() + File.separator + decodeFileName(classname) + "." + fileExtension);
        if (Optional.ofNullable(phpFilePath).isEmpty()) {
            return null;
        }

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(AccessorMethod.class, AccessorMethodDeserializerFactory.create())
                .create();

        VirtualFile file = ReadAction.compute(() -> VfsUtil.findFile(phpFilePath, true));
        if (Optional.ofNullable(file).isEmpty()) {
            return null;
        }

//        WriteAction.run(() -> file.refresh(false, false));

//        ApplicationManager.getApplication().invokeAndWait(() -> WriteAction.run(() -> file.refresh(false, false)));
        ApplicationManager.getApplication().invokeLater(() -> WriteAction.run(() -> file.refresh(false, false)));


        try {
            String json = new String(file.getInputStream().readAllBytes());
            return gson.fromJson(json, ClassMetadata.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDir() {
        AccessorSettings settings = project.getService(AccessorSettings.class);
        return settings.getProxyRootDirectory() + File.separator + "meta";
//        return basePath + File.separator + ".php-accessor" + File.separator + "meta";
    }

    private String decodeFileName(String fnq) {
        return fnq.replaceAll("\\\\", "@");
    }

    public void save(ClassMetadata classMetadata) {
        String dir = getDir();
        String fileName = getFileName(classMetadata.getClassname());
        Path path = PathManager.findBinFile(dir + File.separator + fileName + "." + fileExtension);
        Optional.ofNullable(path).ifPresent(ph -> {
            VirtualFile metaFile = VfsUtil.findFile(ph, true);
            Optional.ofNullable(metaFile).ifPresent(vf -> {
                try {
                    vf.delete(null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        try {
            VirtualFile phpVirtualFile = VfsUtil.createDirectoryIfMissing(dir);
            PsiManagerImpl psiManager = new PsiManagerImpl(project);
            PsiDirectory psiDirectory = new PsiDirectoryImpl(psiManager, phpVirtualFile);
            String json = new Gson().toJson(classMetadata);
            PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(fileName + "." + fileExtension, JsonFileType.INSTANCE, json);
            psiDirectory.add(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getFileName(String fnq) {
        return fnq.replaceAll("\\\\", "@");
    }
}
