package com.gleb.pycrunch.debugging;

import com.gleb.pycrunch.shared.FreePort;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.python.pro.debugger.remote.PyRemoteDebugConfigurationParams;
import com.intellij.python.pro.debugger.remote.PyRemoteDebugConfigurationType;

public class PyRemoteDebugState {
    private RunnerAndConfigurationSettings _cachedConfiguration;

    public void build_configuration_and_run_debugger(Project project) {
        if (_cachedConfiguration == null) {
            _cachedConfiguration = create_run_configuration_for_project(project);
        }

        Executor runExecutorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
        ExecutionUtil.runConfiguration(_cachedConfiguration, runExecutorInstance);
    }

    private RunnerAndConfigurationSettings create_run_configuration_for_project(Project project) {
        RunnerAndConfigurationSettings settings;
        RunManager runManager = RunManager.getInstance(project);

        ConfigurationFactory factory = PyRemoteDebugConfigurationType.getInstance().getConfigurationFactories()[0];
        settings = runManager.createConfiguration("pycrunch-engine - debugger", factory);

        PyRemoteDebugConfigurationParams parameters = (PyRemoteDebugConfigurationParams) settings.getConfiguration();
        parameters.setHost("127.0.0.1");

        int port = FreePort.find_free_port();

        project.putUserData(GlobalKeys.REMOTE_DEBUG_PORT_KEY, port);
        parameters.setPort(port);
//        settings.getConfiguration().setAllowRunningInParallel(false);

        return settings;
    }
}
