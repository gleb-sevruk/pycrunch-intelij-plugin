package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

public class TerminateTestRun extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        MessageBus bus = project.getMessageBus();
        try {
            ((PycrunchToolbarBus) bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC)).terminateTestRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PycrunchConnector connector = ServiceManager.getService(project, PycrunchConnector.class);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(connector._canTerminateTestRun);
    }
}
