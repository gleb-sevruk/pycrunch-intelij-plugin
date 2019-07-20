package com.gleb.pycrunch;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.*;
import com.intellij.util.messages.MessageBus;

public class PyCrunchToolWindowFactory implements ToolWindowFactory {
    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        MessageBus bus = project.getMessageBus();
        MyPycrunchConnector connector = ServiceManager.getService(MyPycrunchConnector.class);
        toolWindow.setAutoHide(false);
        PycrunchToolWindow pycrunchToolWindow = new PycrunchToolWindow(toolWindow, project, bus, connector);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(pycrunchToolWindow.getContent(), "Tests", false);
        toolWindow.getContentManager().addContent(content);
        try {
            connector.AttachToEngine(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}