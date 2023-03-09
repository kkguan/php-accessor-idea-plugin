package com.free2one.accessor.util;

import com.free2one.accessor.AccessorIcons;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class NotificationUtil {

    public static void notify(Project project, String content, @NotNull AnAction action) {
        NotificationGroupManager
                .getInstance()
                .getNotificationGroup("PHP Accessor Plugin")
                .createNotification("PHP Accessor", content, NotificationType.ERROR)
                .setIcon(AccessorIcons.ACCESSOR)
                .addAction(action)
                .notify(project);
    }
}
