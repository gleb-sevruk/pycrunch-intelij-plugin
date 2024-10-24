package com.gleb.pycrunch.debugging;

import com.gleb.pycrunch.shared.FreePort;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.gleb.pycrunch.shared.IdeNotifications;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.PythonRunConfiguration;
import com.jetbrains.python.run.PythonConfigurationType;

public class PyRemoteDebugState {
    private RunnerAndConfigurationSettings _cachedConfiguration;

    public void build_configuration_and_run_debugger(Project project) {
        IdeNotifications.notify(project,"Remote debugging temporary not supported in Community Edition", "You can help with development at \n\n https://github.com/gleb-sevruk/pycrunch-intelij-plugin\n\n ", NotificationType.WARNING);
        return;
//        if (_cachedConfiguration == null) {
//            _cachedConfiguration = create_run_configuration_for_project(project);
//        }
//
//        Executor runExecutorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
//        ExecutionUtil.runConfiguration(_cachedConfiguration, runExecutorInstance);
    }

//    private RunnerAndConfigurationSettings create_run_configuration_for_project(Project project) {
//        IdeNotifications.notify(project,"Remote debugging temporary not supported in Community Edition", "You can help with development at \n\n https://github.com/gleb-sevruk/pycrunch-intelij-plugin\n\n ", NotificationType.WARNING);
//        RunnerAndConfigurationSettings settings;
//        RunManager runManager = RunManager.getInstance(project);
//
//        ConfigurationFactory factory = PyRemoteDebugConfigurationType.getInstance().getConfigurationFactories()[0];
//        settings = runManager.createConfiguration("pycrunch-engine - debugger", factory);
////
////        PyRemoteDebugConfigurationParams parameters = (PyRemoteDebugConfigurationParams) settings.getConfiguration();
////        parameters.setHost("127.0.0.1");
////
////        int port = FreePort.find_free_port();
////
////        project.putUserData(GlobalKeys.REMOTE_DEBUG_PORT_KEY, port);
////        parameters.setPort(port);
//////        settings.getConfiguration().setAllowRunningInParallel(false);
////
//        return settings;
//    }
}
