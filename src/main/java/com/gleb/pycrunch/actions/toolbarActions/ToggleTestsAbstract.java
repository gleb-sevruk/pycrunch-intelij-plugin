package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

public abstract class ToggleTestsAbstract extends ToggleAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    protected PycrunchWindowStateService getUiState(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        return project.getService(PycrunchWindowStateService.class);
    }

    protected void notifyPycrunchUi(@NotNull AnActionEvent e) {
        MessageBus bus = e.getProject().getMessageBus();
        bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC).refillTestList();
    }
}
