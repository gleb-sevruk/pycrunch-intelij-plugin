package com.gleb.pycrunch.shared;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.sun.istack.Nullable;

public class IdeNotifications {

    private static final String NotificationGroupName = "Pycrunch-engine Connector Plugin";


    public static NotificationGroup getNotificationGroupInstance(@Nullable Project project) {
        return NotificationGroupManager.getInstance().getNotificationGroup(NotificationGroupName);
    }

    public static void notify(Project project, String subtitle, String content, NotificationType type) {
//  from https://plugins.jetbrains.com/docs/intellij/notifications.html#top-level-notifications-balloons
        getNotificationGroupInstance(project)
                .createNotification(content, type)
                .setSubtitle(subtitle)
                .setContent(content)
                .notify(project);
    }
}
