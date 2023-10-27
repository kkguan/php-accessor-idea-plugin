package com.free2one.accessor;

import com.free2one.accessor.composer.ComposerProcessListener;
import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.openapi.progress.DumbProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.ComposerUtils;
import com.jetbrains.php.composer.actions.ComposerCommandRunner;
import com.jetbrains.php.composer.execution.ComposerExecution;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AccessorGeneratorService {

    private final Project project;

    public AccessorGeneratorService(Project project) {
        this.project = project;
    }

    public void generate(String path, TaskCallback callback) {
        List<String> command = getCommandForGenerate(path);
        ComposerExecution composer = ComposerDataService.getInstance(project).getComposerExecution();
        Task.Backgroundable task = new Task.Backgroundable(project, AccessorBundle.message("composer.generate-proxy.task.title")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ComposerCommandRunner runner = new ComposerCommandRunner(composer, project, project.getBasePath(), new DumbProgressIndicator());
                ComposerCommandRunner.ExecutionResult result = runner.runCommand(command, new ComposerProcessListener());
                if (callback != null) {
                    callback.onProgress(result);
                }
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(task);
        } else {
            // Run the scan task when the thread is in the foreground.
            SwingUtilities.invokeLater(() -> ProgressManager.getInstance().run(task));
        }
    }

    public List<String> getCommandForGenerate(String path) {
        AccessorSettings settings = project.getService(AccessorSettings.class);
        ArrayList<String> commandLineOptions = new ArrayList<>();
        commandLineOptions.add(path);
        commandLineOptions.add("-d");
        commandLineOptions.add(project.getBasePath());
        commandLineOptions.add("--");
        commandLineOptions.add("--dir=" + settings.getProxyRootDirectory());
        commandLineOptions.add("--gen-meta=yes");

        return ComposerUtils.getRunScriptCommand("php-accessor", commandLineOptions);
    }

    public interface TaskCallback {
        void onProgress(ComposerCommandRunner.ExecutionResult result);
    }

}
