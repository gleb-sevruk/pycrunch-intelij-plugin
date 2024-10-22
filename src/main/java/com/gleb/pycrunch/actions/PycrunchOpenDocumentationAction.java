package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.activation.ActivationConnector;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URI;

public class PycrunchOpenDocumentationAction extends AnAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public PycrunchOpenDocumentationAction() {
        // Set the menu item name.
        super("_Documentation");
    }

    public void actionPerformed(AnActionEvent event) {
        open_pycrunch_site();
    }

    private void open_pycrunch_site() {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(ActivationConnector.site_url + "/docs"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
