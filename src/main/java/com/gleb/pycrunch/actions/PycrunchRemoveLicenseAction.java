package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.MyPycrunchConnector;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class PycrunchRemoveLicenseAction extends AnAction {
    public PycrunchRemoveLicenseAction() {
        // Set the menu item name.
        super("Remove _License");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        MyPycrunchConnector connector = ServiceManager.getService(MyPycrunchConnector.class);
        int dialog_result = Messages.showOkCancelDialog(project, "Are you sure you want to revoke license? \nYou will need to sign in again to use Pycrunch", "Remove Pycrunch License", "Remove license", "Cancel", Messages.getWarningIcon());
        System.out.println("dialog result: " + dialog_result);
        if (dialog_result == Messages.YES) {
            connector.remove_license();
        }
    }
}
