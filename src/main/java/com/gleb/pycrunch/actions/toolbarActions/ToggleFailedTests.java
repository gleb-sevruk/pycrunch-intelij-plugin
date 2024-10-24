package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ToggleFailedTests extends ToggleTestsAbstract {

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        PycrunchWindowStateService uiState = getUiState(e);
        if (uiState == null) {
            return false;
        }
        return uiState._showFailedTests;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PycrunchWindowStateService uiState = getUiState(e);
        if (uiState == null){
            return;
        }
        uiState._showFailedTests = state;
        notifyPycrunchUi(e);
    }
}
