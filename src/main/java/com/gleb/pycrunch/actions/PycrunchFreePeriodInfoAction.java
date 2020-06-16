package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class PycrunchFreePeriodInfoAction extends AnAction {
    public PycrunchFreePeriodInfoAction() {
        // Set the menu item name.
        super("Free _Edition");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Messages.showMessageDialog(project,
                "This version is free to use. \nIf you found this product useful, consider making a donation on the website.", "Pycrunch License", Messages.getInformationIcon());
    }
}
