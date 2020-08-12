package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.debugging.PyRemoteDebugState;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

public class RunDebugPycrunchEngineAction extends AnAction {

    public RunDebugPycrunchEngineAction() {
        super("_Run Remote Debugger Process");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        PyRemoteDebugState debugState = ServiceManager.getService(project, PyRemoteDebugState.class);

        debugState.build_configuration_and_run_debugger(project);
    }


}