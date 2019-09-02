package com.gleb.pycrunch.shared;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class IdeNotifications {
    private static final NotificationGroup GROUP_DISPLAY_ID_INFO =
            new NotificationGroup("Pycrunch",
                    NotificationDisplayType.BALLOON, true);

    public static void notify(Project project, String subtitle, String content, NotificationType type) {
        Notification orel = GROUP_DISPLAY_ID_INFO.createNotification("Pycrunch",subtitle, content, NotificationType.INFORMATION);
        orel.notify(project);
    }
}
