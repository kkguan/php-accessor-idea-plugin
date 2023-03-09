package com.free2one.accessor.composer.environmentDetector.detector;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.util.NotificationUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.composer.ComposerDataService;
import org.jetbrains.annotations.NotNull;

public class ComposerExecutableDetector implements EnvironmentDetector {

    private Project project;

    @Override
    public void init(Project project) {
        this.project = project;
    }

    @Override
    public void scan() {
        ComposerDataService composerDataService = project.getService(ComposerDataService.class);
        if (composerDataService.isExecutionWellConfigured(false)) {
            return;
        }

        NotificationUtil.notify(project, AccessorBundle.message("composer.incorrectly.configured"), new NotificationAction(AccessorBundle.message("composer.settings")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Composer");
                notification.expire();
            }
        });
    }
}
