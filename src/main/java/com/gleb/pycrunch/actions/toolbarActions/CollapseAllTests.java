package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.messages.MessageBus;

public class CollapseAllTests extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        MessageBus bus = event.getProject().getMessageBus();
        ((PycrunchToolbarBus) bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC)).collapseAll();
    }
}
