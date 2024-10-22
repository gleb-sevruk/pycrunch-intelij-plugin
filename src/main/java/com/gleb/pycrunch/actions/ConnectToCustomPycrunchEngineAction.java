package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.gleb.pycrunch.shared.IdeNotifications;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ConnectToCustomPycrunchEngineAction extends AnAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public ConnectToCustomPycrunchEngineAction() {
        // Set the menu item name.
        super("Connect to Custom PyCrunch _Engine");

        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    public static Integer tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void actionPerformed(AnActionEvent event) {

        Project project = event.getData(PlatformDataKeys.PROJECT);
//        ProjectManager.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
//        });
        Object pycrunch_port = project.getUserData(GlobalKeys.PORT_KEY);
        if (pycrunch_port == null) {
            pycrunch_port = 5000;
        }

        String txt = Messages.showInputDialog(
                project,
                "Custom port where engine is running?",
                "Connect to PyCrunch Engine",
                Messages.getQuestionIcon(),
                pycrunch_port.toString(),
                null);
        if (txt == null) {
//            cancel
            return;
        }
        Integer port = tryParse(txt);
        if (port == null) {
            IdeNotifications.notify(
                    project,
                    "PyCrunch",
                    "Cannot connect to custom port: " + txt,
                    NotificationType.WARNING);
            return;
        }

        PycrunchConnector connector = project.getService(PycrunchConnector.class);
        try {
            project.putUserData(GlobalKeys.PORT_KEY, port);
            connector.AttachToEngine(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}