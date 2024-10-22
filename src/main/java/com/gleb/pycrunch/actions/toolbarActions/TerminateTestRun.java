package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

public class TerminateTestRun extends AnAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

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
        PycrunchConnector connector = project.getService(PycrunchConnector.class);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(connector._canTerminateTestRun);
    }
}
