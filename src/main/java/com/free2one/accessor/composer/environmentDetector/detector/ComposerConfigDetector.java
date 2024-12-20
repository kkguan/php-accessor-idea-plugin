package com.free2one.accessor.composer.environmentDetector.detector;

import com.free2one.accessor.AccessorBundle;
import com.free2one.accessor.util.NotificationUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.composer.ComposerConfigurable;
import com.jetbrains.php.composer.ComposerDataService;
import com.jetbrains.php.composer.ComposerInitSupportAction;
import com.jetbrains.php.composer.ComposerUtils;
import com.jetbrains.php.composer.statistics.ComposerActionStatistics;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

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

    private static class CustomComposerInitSupportAction extends ComposerInitSupportAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {

            if (e == null) {
                return;
            }

            final Project project = e.getProject();
            if (project != null) {
                VirtualFile parentFolder = getParentFolder(project);
                if (parentFolder != null) {
                    ComposerActionStatistics.reportAction(ComposerActionStatistics.Action.INIT, e.getPlace(), e.getProject());
                    ComposerDataService dataService = ComposerDataService.getInstance(project);
                    if (dataService.askForValidConfigurationIfNeeded()) {
                        if (!dataService.isConfigWellConfigured()) {
                            VirtualFile config = parentFolder.findChild("composer.json");
                            if (config == null || !config.exists()) {
                                config = findSuitableConfig(project);
                            }

                            if (config != null && config.exists()) {
                                AnAction changeSettingAction = new DumbAwareAction(PhpBundle.message("composer.change.setting.action.name", new Object[0])) {
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        if (e == null) {
                                            return;
                                        }

                                        ShowSettingsUtil.getInstance().editConfigurable(project, new ComposerConfigurable(project));
                                    }
                                };
                                dataService.setConfigPathAndLibraryUpdateStatus(config.getPath(), true);
                                Notification errorNotification = new Notification(ComposerUtils.getComposerGroupDisplayId(), PhpBundle.message("framework.composer.notification.title.init.composer", new Object[0]), PhpBundle.message("framework.composer.file.0.set.as.composer.config.change.setting.a.href.here.a", new Object[]{FileUtil.toSystemIndependentName(config.getPath())}), NotificationType.INFORMATION);
                                errorNotification.addAction(changeSettingAction);
                                Notifications.Bus.notify(errorNotification, project);
                            } else {
                                Runnable initializer = () -> ComposerUtils.initConfig(parentFolder, project, this);
                                ApplicationManager.getApplication().runWriteAction(initializer);
                            }
                        }
                    }
                }
            }
        }

        private static VirtualFile findSuitableConfig(Project project) {
            if (project == null) {
                return null;
            } else {
                GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
                Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName("composer.json", scope);
                if (files.isEmpty()) {
                    return null;
                } else {
                    MultiMap<VirtualFile, VirtualFile> parentToConfig = new MultiMap();

                    for(VirtualFile file : files) {
                        VirtualFile parent = file.getParent();
                        if (parent != null) {
                            parent = parent.getParent();
                            if (parent != null) {
                                parentToConfig.putValue(parent, file);
                            }
                        }
                    }

                    VfsUtilCore.DistinctVFilesRootsCollection roots = new VfsUtilCore.DistinctVFilesRootsCollection(parentToConfig.keySet());
                    VirtualFile potentialConfigParent = null;

                    for(VirtualFile root : roots) {
                        if (parentToConfig.get(root).size() == 1 && !"vendor".equals(root.getName())) {
                            if (potentialConfigParent != null) {
                                return null;
                            }

                            potentialConfigParent = root;
                        }
                    }

                    return potentialConfigParent == null ? null : (VirtualFile)parentToConfig.get(potentialConfigParent).iterator().next();
                }
            }
        }
    }
}
