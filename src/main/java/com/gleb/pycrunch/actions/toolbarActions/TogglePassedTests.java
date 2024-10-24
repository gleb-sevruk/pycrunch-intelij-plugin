package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.jetbrains.python.requirements.False;
import org.jetbrains.annotations.NotNull;

public class TogglePassedTests extends ToggleTestsAbstract {

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        PycrunchWindowStateService uiState = getUiState(e);
        if (uiState == null) {
            return false;
        }
        return uiState._showPassedTests;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PycrunchWindowStateService uiState = getUiState(e);
        if (uiState == null){
            return;
        }
        uiState._showPassedTests = state;
        notifyPycrunchUi(e);
    }
}
