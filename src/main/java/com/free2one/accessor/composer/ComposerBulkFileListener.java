package com.free2one.accessor.composer;

import com.free2one.accessor.AccessorGeneratorService;
import com.free2one.accessor.PhpAccessorClassnames;
import com.free2one.accessor.settings.AccessorSettings;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class ComposerBulkFileListener implements BulkFileListener {

    public ComposerBulkFileListener() {
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        events.forEach(this::generateProxy2);
    }


    private void generateProxy2(VFileEvent vFileEvent) {
        if (vFileEvent.getFile() == null) {
            return;
        }

        Project project = ProjectLocator.getInstance().guessProjectForFile(vFileEvent.getFile());
        if (project == null) {
            return;
        }

        AccessorSettings settings = project.getService(AccessorSettings.class);
        if (!isPendingFile(vFileEvent, settings, project)) {
            return;
        }

        project.getService(AccessorGeneratorService.class).generate(vFileEvent.getFile().getPath(), null);
    }

    private boolean isPendingFile(VFileEvent vFileEvent, AccessorSettings settings, Project project) {
        if (vFileEvent instanceof VFileDeleteEvent ||
                vFileEvent.getFile() == null ||
                !(vFileEvent.getFile().getFileType() instanceof PhpFileType) ||
                vFileEvent.getPath().contains(project.getBasePath() + File.separator + "vendor")) {
            return false;
        }

        if (settings.containSettingDirectories(vFileEvent.getPath())) {
            return false;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(vFileEvent.getFile());
        if (!(psiFile instanceof PhpFile)) {
            return false;
        }

        Collection<PhpClass> phpClasses = PhpPsiUtil.findAllClasses(psiFile);
        if (phpClasses.isEmpty()) {
            return false;
        }

        return AnnotationSearchUtil.isAnnotatedWith(phpClasses, PhpAccessorClassnames.Data);
    }
}
