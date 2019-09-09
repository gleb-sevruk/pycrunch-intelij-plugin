package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;

public class ConnectToCustomPycrunchEngineAction extends AnAction {
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
        String txt= Messages.showInputDialog(project, "Custom port when engine is running?", "Connect to PyCrunch Engine", Messages.getQuestionIcon());
        Integer port = tryParse(txt);
        if (port == null) {
            Messages.showMessageDialog(project, "Invalid port entered, " + txt + "!", "PyCrunch ", Messages.getInformationIcon());
            return;
        }

        PycrunchConnector connector = ServiceManager.getService(project, PycrunchConnector.class);
        try {
            project.putUserData(GlobalKeys.PORT_KEY, port);
            connector.AttachToEngine(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}