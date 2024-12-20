package com.free2one.accessor.composer.environmentDetector.detector;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.util.NotificationUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.ComposerInitSupportAction;
import org.jetbrains.annotations.NotNull;

public class ComposerConfigDetector implements EnvironmentDetector {

    private Project project;

    @Override
    public void init(Project project) {
        this.project = project;
    }

    @Override
    public void scan() {
        ComposerDataService composerDataService = project.getService(ComposerDataService.class);
        if (composerDataService.getConfigFile() != null) {
            return;
        }

        NotificationUtil.notify(project, AccessorBundle.message("composer.missing.config"), new NotificationAction(AccessorBundle.message("composer.install")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                CustomComposerInitSupportAction composerInitSupportAction = new CustomComposerInitSupportAction();
                composerInitSupportAction.actionPerformed(e);
                notification.expire();
            }
        });
    }
}
