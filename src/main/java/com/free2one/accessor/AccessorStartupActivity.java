package com.free2one.accessor;

import com.free2one.accessor.composer.ComposerBulkFileListener;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.php.composer.ComposerDataService;
import org.jetbrains.annotations.NotNull;

public class AccessorStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new ComposerBulkFileListener(project));
//        ComposerExecution composer = ComposerDataService.getInstance(project).getComposerExecution();
        if (!ComposerDataService.getInstance(project).isConfigWellConfigured()) {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("PHP Accessor Plugin")
                    .createNotification("PHP Accessor", "Composer seems to be set up incorrectly, PHP Accessor may not work.", NotificationType.WARNING)
                    .notify(project);
        }
    }
}
