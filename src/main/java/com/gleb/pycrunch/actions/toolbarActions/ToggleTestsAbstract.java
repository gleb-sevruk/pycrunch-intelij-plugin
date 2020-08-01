package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

public abstract class ToggleTestsAbstract extends ToggleAction {

    protected PycrunchWindowStateService getUiState(@NotNull AnActionEvent e) {
        return ServiceManager.getService(e.getProject(), PycrunchWindowStateService.class);
    }

    protected void notifyPycrunchUi(@NotNull AnActionEvent e) {
        MessageBus bus = e.getProject().getMessageBus();
        bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC).refillTestList();
    }
}
