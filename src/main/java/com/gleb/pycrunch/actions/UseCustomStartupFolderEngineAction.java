package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.shared.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class UseCustomStartupFolderEngineAction extends AnAction {
    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public UseCustomStartupFolderEngineAction() {
        // Set the menu item name.
        super("Use Custom Startup Directory...");

        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    public void actionPerformed(AnActionEvent event) {

        Project project = event.getData(PlatformDataKeys.PROJECT);
        CachedRuntimeConfigurations cache = project.getService(CachedRuntimeConfigurations.class);
        String current_dir = RecentlyUsedFolders.getLastSelectedFolder(project);
        String userInput = Messages.showEditableChooseDialog(
                "Custom directory where to run the engine",
                "Setting up custom working directory",
                Messages.getQuestionIcon(),
                RecentlyUsedFolders.getRecentFolders(project).toArray(new String[0]),
                current_dir, null);

        if (userInput == null) {
//            cancel
            return;
        }
        RecentlyUsedFolders.addRecentFolder(project, userInput);

        PycrunchConnector connector = project.getService(PycrunchConnector.class);
        try {
            if (!current_dir.equals(userInput)) {
                // Removing run configuration so it is no longer cached with the old directory
                cache.remove_project(project);
            }
            RecentlyUsedFolders.saveLastSelectedFolder(project, userInput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}