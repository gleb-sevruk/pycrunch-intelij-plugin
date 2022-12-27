package com.gleb.pycrunch.actions.engineMode;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.shared.EngineMode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class BaseSetEngineMode extends ToggleAction {

    private final String currentMode;

    public BaseSetEngineMode(String currentMode) {
        this.currentMode = currentMode;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        EngineMode uiState = project.getService(EngineMode.class);
        return uiState._mode.equals(currentMode);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = e.getProject();
        PycrunchConnector _connector = project.getService(PycrunchConnector.class);
        EngineMode service = project.getService(EngineMode.class);
        if (state != true && service._mode.equals(currentMode)) {
            return;
        }
        service.WillChangeTo(currentMode);
        _connector.update_mode(currentMode);
    }

}
