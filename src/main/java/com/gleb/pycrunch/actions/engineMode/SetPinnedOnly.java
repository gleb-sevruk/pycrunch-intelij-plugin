package com.gleb.pycrunch.actions.engineMode;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.shared.EngineMode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

public class SetPinnedOnly extends BaseSetEngineMode {

    public SetPinnedOnly() {
        super(EngineMode.mode_pinned_automatically);
    }
}