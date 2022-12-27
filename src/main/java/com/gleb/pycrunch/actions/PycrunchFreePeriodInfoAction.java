package com.gleb.pycrunch.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
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
                "This product is free to use. \nIf you found it useful, consider making a donation on the website. \n\nhttps://pycrunch.com/donate", "Pycrunch License", Messages.getInformationIcon());
    }
}
