package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.debugging.PyRemoteDebugState;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RunDebugPycrunchEngineAction extends AnAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }


    public RunDebugPycrunchEngineAction() {
        super("_Run Remote Debugger Process");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        PyRemoteDebugState debugState = project.getService(PyRemoteDebugState.class);

        debugState.build_configuration_and_run_debugger(project);
    }


}