package com.free2one.accessor.composer.environmentDetector.detector;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.composer.ComposerPackageManager;
import com.free2one.accessor.util.NotificationUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.addDependency.ComposerPackage;
import org.jetbrains.annotations.NotNull;

public class PackageInstallationDetector implements EnvironmentDetector {

    private Project project;

    @Override
    public void init(Project project) {
        this.project = project;
    }

    @Override
    public void scan() {
        ComposerDataService composerDataService = project.getService(ComposerDataService.class);
        if (composerDataService.getConfigFile() == null) {
            return;
        }

        ComposerPackage composerPackage = project.getService(ComposerPackageManager.class).findPackage(ComposerPackageManager.DependentPackage.PHP_ACCESSOR);
        if (composerPackage != null) {
            return;
        }

        NotificationUtil.notify(project, AccessorBundle.message("composer.php-accessor.not.found"), new NotificationAction(AccessorBundle.message("composer.php-accessor.install")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                project.getService(ComposerPackageManager.class).installPackage(ComposerPackageManager.DependentPackage.PHP_ACCESSOR, composerDataService.getConfigFile());
                notification.expire();
            }
        });
    }
}
