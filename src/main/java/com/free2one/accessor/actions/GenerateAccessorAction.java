package com.free2one.accessor.actions;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.AccessorGeneratorService;
import com.free2one.accessor.settings.AccessorSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.ThrowableNotNullFunction;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.actions.ComposerActionCommandExecutor;
import com.jetbrains.php.composer.actions.ComposerCommandExecutor;
import com.jetbrains.php.composer.actions.log.ComposerLogMessageBuilder;
import com.jetbrains.php.composer.execution.ComposerExecution;
import com.jetbrains.php.composer.statistics.ComposerActionStatistics;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class extends AnAction to generate accessors for PHP classes.
 */
public class GenerateAccessorAction extends AnAction {

    /**
     * This method is called when the action is performed.
     * It generates accessors for the selected files in the project.
     *
     * @param e AnActionEvent object containing information about the event that triggered this action.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null || project == null) {
            return;
        }

        for (VirtualFile file : files) {
            this.executeCommand(project, file);
        }

        VirtualFile proxyFile = LocalFileSystem.getInstance().findFileByPath(AccessorSettings.getInstance(project).getProxyRootDirectory());
        if (proxyFile == null) {
            return;
        }

        proxyFile.refresh(false, true);
    }

    /**
     * Executes the command to generate accessors for a given file in the project.
     *
     * @param project The current project.
     * @param file    The file to generate accessors for.
     */
    private void executeCommand(Project project, VirtualFile file) {
        List<String> command = project.getService(AccessorGeneratorService.class).getCommandForGenerate(file.getPath());
        ComposerExecution execution = ComposerDataService.getInstance(project).getComposerExecution();
        ComposerDataService composerDataService = project.getService(ComposerDataService.class);
        VirtualFile configFile = composerDataService.getConfigFile();
        ComposerActionStatistics statistics = ComposerActionStatistics.create(ComposerActionStatistics.Action.RUN_SCRIPT_FROM_CONTEXT, "");

        new ComposerActionCommandExecutor.WithConfig(project, execution, configFile, "", statistics, true, true) {
            protected @Nls(
                    capitalization = Nls.Capitalization.Title
            ) @NotNull String getTaskTitle() {
                return AccessorBundle.message("composer.generate-proxy.task.title");
            }

            protected ComposerLogMessageBuilder.SummaryMessage createSuccessfulSummary() {
                return (new ComposerLogMessageBuilder.SummaryMessage()).appendText(AccessorBundle.message("composer.generate-proxy.task.success"));

            }

            protected ComposerLogMessageBuilder.SummaryMessage createFailureSummary() {
                return (new ComposerLogMessageBuilder.SummaryMessage()).appendText(AccessorBundle.message("composer.generate-proxy.task.failed"));
            }

            protected @Nls String getProgressTitle() {
                return AccessorBundle.message("composer.generate-proxy.task.title");
            }

            protected @NlsSafe String getActionName() {
                return AccessorBundle.message("composer.generate-proxy.action.name");
            }

            protected @NotNull List<String> getBasicCommand() {
                return command;
            }

            protected ThrowableNotNullFunction<Project, ComposerCommandExecutor, ExecutionException> getExecutorGenerator() {
                return null;
            }
        }.execute();
    }
}
