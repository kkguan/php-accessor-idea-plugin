package com.free2one.accessor.composer;

import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.ComposerUtils;
import com.jetbrains.php.composer.actions.ComposerCommandRunner;
import com.jetbrains.php.composer.execution.ComposerExecution;
import com.jetbrains.php.lang.PhpFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ComposerBulkFileListener implements BulkFileListener {

    private final Project project;

    public ComposerBulkFileListener(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        events.forEach(this::updateMetadata);
    }

    private void updateMetadata(VFileEvent vFileEvent) {
        if (vFileEvent.getFile() == null ||
                !(vFileEvent.getFile().getFileType() instanceof PhpFileType)) {
            return;
        }

        AccessorSettings settings = project.getService(AccessorSettings.class);
        if (settings.containSettingDirectories(vFileEvent.getPath())) {
            return;
        }

        ArrayList<String> commandLineOptions = new ArrayList<>();
        commandLineOptions.add(vFileEvent.getFile().getPath());
        commandLineOptions.add("-d");
        commandLineOptions.add(project.getBasePath());
        commandLineOptions.add("--");
//        commandLineOptions.add("--dir=" + project.getBasePath());
        commandLineOptions.add("--dir=" + settings.getProxyRootDirectory());
        commandLineOptions.add("--gen-meta=yes");
//        commandLineOptions.add("--gen-proxy=no");
        List<String> command = ComposerUtils.getRunScriptCommand("php-accessor", commandLineOptions);
        ComposerExecution composer = ComposerDataService.getInstance(project).getComposerExecution();
        Task.Backgroundable task = new Task.Backgroundable(project, "Building metadata") {
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
}
