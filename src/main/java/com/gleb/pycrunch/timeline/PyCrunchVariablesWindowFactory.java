package com.gleb.pycrunch.timeline;

import com.gleb.pycrunch.PycrunchConnector;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;

@SuppressWarnings("ALL")
public class PyCrunchVariablesWindowFactory implements ToolWindowFactory {
    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        MessageBus bus = project.getMessageBus();
        PycrunchConnector connector = project.getService(PycrunchConnector.class);
        toolWindow.setAutoHide(false);
        PyCrunchVariablesWindow pycrunchToolWindow = new PyCrunchVariablesWindow(toolWindow, project, bus, connector);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
//        TODO: Change to after 2022.2 only is supported
//        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(pycrunchToolWindow.getContent(), "Variables", false);
        toolWindow.getContentManager().addContent(content);
        content.setCloseable(false);
    }
}