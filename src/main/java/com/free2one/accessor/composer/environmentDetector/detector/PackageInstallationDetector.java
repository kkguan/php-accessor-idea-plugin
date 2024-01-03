package com.free2one.accessor.composer.environmentDetector.detector;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.composer.ComposerPackageManager;
import com.free2one.accessor.composer.PackageInstalledNotifier;
import com.free2one.accessor.util.NotificationUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.addDependency.ComposerPackage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PackageInstallationDetector implements EnvironmentDetector {

    private static final Map<String, Detector> detectors = new HashMap<>();

    static {
        detectors.put(HyperfDetector.class.getName(), new HyperfDetector());
        detectors.put(LaravelDetector.class.getName(), new LaravelDetector());
        detectors.put(PhpAccessorDetector.class.getName(), new PhpAccessorDetector());
    }

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

        for (Detector detector : detectors.values()) {
            if (detector.detect(project, composerDataService)) {
                break;
            }
        }
    }

    private interface Detector {
        boolean detect(Project project, ComposerDataService composerDataService);
    }

    private static class HyperfDetector implements Detector {
        @Override
        public boolean detect(Project project, ComposerDataService composerDataService) {
            ComposerPackage hyperfPackage = project.getService(ComposerPackageManager.class).findPackage(ComposerPackageManager.DependentPackage.HYPERF_FRAMEWORK);
            if (hyperfPackage == null) {
                return false;
            }

            ComposerPackage hyperfPhpAccessorPackage = project.getService(ComposerPackageManager.class).findPackage(ComposerPackageManager.DependentPackage.HYPERF_PHP_ACCESSOR);
            if (hyperfPhpAccessorPackage != null) {
                PackageInstalledNotifier publisher = project.getMessageBus()
                        .syncPublisher(PackageInstalledNotifier.ACCESSOR_PACKAGE_INSTALL_TOPIC);
                publisher.installed(hyperfPhpAccessorPackage, ComposerPackageManager.DependentPackage.HYPERF_PHP_ACCESSOR);
                return false;
            }

            NotificationUtil.notify(project, AccessorBundle.message("composer.hyperf-php-accessor.not.found"), new NotificationAction(AccessorBundle.message("composer.php-accessor.install")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    project.getService(ComposerPackageManager.class).installPackage(ComposerPackageManager.DependentPackage.HYPERF_PHP_ACCESSOR, composerDataService.getConfigFile());
                    notification.expire();
                }
            });

            return true;
        }
    }

    private static class LaravelDetector implements Detector {
        @Override
        public boolean detect(Project project, ComposerDataService composerDataService) {
            ComposerPackage laravelPackage = project.getService(ComposerPackageManager.class).findPackage(ComposerPackageManager.DependentPackage.LARAVEL_FRAMEWORK);
            if (laravelPackage == null) {
                return false;
            }

            ComposerPackage laravelPhpAccessorPackage = project.getService(ComposerPackageManager.class).findPackage(ComposerPackageManager.DependentPackage.LARAVEL_PHP_ACCESSOR);
            if (laravelPhpAccessorPackage != null) {
                return false;
            }

            NotificationUtil.notify(project, AccessorBundle.message("composer.laravel-php-accessor.not.found"), new NotificationAction(AccessorBundle.message("composer.php-accessor.install")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    project.getService(ComposerPackageManager.class).installPackage(ComposerPackageManager.DependentPackage.LARAVEL_PHP_ACCESSOR, composerDataService.getConfigFile());
                    notification.expire();
                }
            });

            return true;
        }
    }

    private static class PhpAccessorDetector implements Detector {
        @Override
        public boolean detect(Project project, ComposerDataService composerDataService) {

            ComposerPackage composerPackage = project.getService(ComposerPackageManager.class).findPackage(ComposerPackageManager.DependentPackage.PHP_ACCESSOR);
            if (composerPackage != null) {
                return false;
            }

            NotificationUtil.notify(project, AccessorBundle.message("composer.php-accessor.not.found"), new NotificationAction(AccessorBundle.message("composer.php-accessor.install")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    project.getService(ComposerPackageManager.class).installPackage(ComposerPackageManager.DependentPackage.PHP_ACCESSOR, composerDataService.getConfigFile());
                    notification.expire();
                }
            });

            return true;
        }
    }

}
