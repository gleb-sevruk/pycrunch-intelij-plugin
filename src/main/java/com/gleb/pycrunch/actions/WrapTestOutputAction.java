package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

public class WrapTestOutputAction extends ToggleAction {
    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PycrunchWindowStateService uiState = ServiceManager.getService(project, PycrunchWindowStateService.class);
        return uiState._wrapOutput;

    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = e.getProject();
        PycrunchWindowStateService uiState = ServiceManager.getService(project, PycrunchWindowStateService.class);
        uiState._wrapOutput = state;

        MessageBus bus = project.getMessageBus();
        bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC).applyWordWrap();
    }


}
