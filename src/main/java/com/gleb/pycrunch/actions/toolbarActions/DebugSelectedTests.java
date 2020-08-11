package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;

public class DebugSelectedTests extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        MessageBus bus = project.getMessageBus();
        try {
            ((PycrunchToolbarBus) bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC)).debugSelectedTests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
