package com.free2one.accessor.composer;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.PhpAccessorClassnames;
import com.free2one.accessor.settings.AccessorSettings;
import com.free2one.accessor.util.AnnotationSearchUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.ComposerUtils;
import com.jetbrains.php.composer.actions.ComposerCommandRunner;
import com.jetbrains.php.composer.execution.ComposerExecution;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComposerBulkFileListener implements BulkFileListener {

    private final Project project = null;

    public ComposerBulkFileListener() {
    }

//    public ComposerBulkFileListener(Project project) {
//        this.project = project;
//    }

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

        ArrayList<String> commandLineOptions = new ArrayList<>();
        commandLineOptions.add(vFileEvent.getFile().getPath());
        commandLineOptions.add("-d");
        commandLineOptions.add(project.getBasePath());
        commandLineOptions.add("--");
        commandLineOptions.add("--dir=" + settings.getProxyRootDirectory());
        commandLineOptions.add("--gen-meta=yes");
        List<String> command = ComposerUtils.getRunScriptCommand("php-accessor", commandLineOptions);
        ComposerExecution composer = ComposerDataService.getInstance(project).getComposerExecution();
        Task.Backgroundable task = new Task.Backgroundable(project, AccessorBundle.message("composer.generate-proxy.task.title")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ComposerCommandRunner runner = new ComposerCommandRunner(composer, project, project.getBasePath(), new ComposerProgressIndicator());
                ComposerCommandRunner.ExecutionResult result = runner.runCommand(command, new ComposerProcessListener());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(task);
        } else {
            // Run the scan task when the thread is in the foreground.
            SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(task));
        }
    }

//    private void generateProxy(VFileEvent vFileEvent) {
//        AccessorSettings settings = project.getService(AccessorSettings.class);
//        if (!isPendingFile(vFileEvent, settings)) {
//            return;
//        }
//
//        ArrayList<String> commandLineOptions = new ArrayList<>();
//        commandLineOptions.add(vFileEvent.getFile().getPath());
//        commandLineOptions.add("-d");
//        commandLineOptions.add(project.getBasePath());
//        commandLineOptions.add("--");
//        commandLineOptions.add("--dir=" + settings.getProxyRootDirectory());
//        commandLineOptions.add("--gen-meta=yes");
//        List<String> command = ComposerUtils.getRunScriptCommand("php-accessor", commandLineOptions);
//        ComposerExecution composer = ComposerDataService.getInstance(project).getComposerExecution();
//        Task.Backgroundable task = new Task.Backgroundable(project, AccessorBundle.message("composer.generate-proxy.task.title")) {
//            @Override
//            public void run(@NotNull ProgressIndicator indicator) {
//                ComposerCommandRunner runner = new ComposerCommandRunner(composer, project, project.getBasePath(), new ComposerProgressIndicator());
//                ComposerCommandRunner.ExecutionResult result = runner.runCommand(command, new ComposerProcessListener());
//            }
//        };
//        if (SwingUtilities.isEventDispatchThread()) {
//            ProgressManager.getInstance().run(task);
//        } else {
//            // Run the scan task when the thread is in the foreground.
//            SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(task));
//        }
//    }

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

        if (!AnnotationSearchUtil.isAnnotatedWith(phpClasses, PhpAccessorClassnames.Data)) {
            return false;
        }

        return true;
    }
}
