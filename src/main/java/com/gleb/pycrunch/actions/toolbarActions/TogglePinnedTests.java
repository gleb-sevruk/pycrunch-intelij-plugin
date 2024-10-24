package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class TogglePinnedTests extends ToggleTestsAbstract {

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        PycrunchWindowStateService uiState = getUiState(e);
        if (uiState == null){
            return false;
        }
        return uiState._showPinnedTests;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PycrunchWindowStateService uiState = getUiState(e);
        if (uiState == null){
            return;
        }
        uiState._showPinnedTests = state;
        notifyPycrunchUi(e);
    }
}
