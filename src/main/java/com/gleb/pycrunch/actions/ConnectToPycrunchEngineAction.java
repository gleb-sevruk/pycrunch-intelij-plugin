package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;

public class ConnectToPycrunchEngineAction extends AnAction {
    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public ConnectToPycrunchEngineAction() {
        // Set the menu item name.
        super("Connect to PyCrunch _Engine");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    public void actionPerformed(AnActionEvent event) {

        Project project = event.getData(PlatformDataKeys.PROJECT);
//        ProjectManager.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
//        });
        PycrunchConnector connector = ServiceManager.getService(PycrunchConnector.class);
        try {
            connector.AttachToEngine(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
//        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());
    }
}