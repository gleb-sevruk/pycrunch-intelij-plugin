package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class TogglePendingTests extends ToggleTestsAbstract {

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        PycrunchWindowStateService uiState = getUiState(e);
        return uiState._showPendingTests;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PycrunchWindowStateService uiState = getUiState(e);
        uiState._showPendingTests = state;
        notifyPycrunchUi(e);
    }
}
