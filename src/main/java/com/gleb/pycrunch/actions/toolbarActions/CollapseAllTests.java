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

public class CollapseAllTests extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        MessageBus bus = event.getProject().getComponent(MessageBus.class);
        ((PycrunchToolbarBus) bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC)).collapseAll();

    }
}
