package com.gleb.pycrunch.debugging;

import com.gleb.pycrunch.shared.FreePort;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jdom.Element;

public class PyRemoteDebugState {
    private RunnerAndConfigurationSettings _cachedConfiguration;

    public void build_configuration_and_run_debugger(Project project) {
        if (_cachedConfiguration == null) {
            _cachedConfiguration = create_run_configuration_for_project(project);
        }
        if (_cachedConfiguration == null) {
            // it means we are on CE version
            return;
        }

        Executor runExecutorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
        ExecutionUtil.runConfiguration(_cachedConfiguration, runExecutorInstance);
    }

    private RunnerAndConfigurationSettings create_run_configuration_for_project(Project project) {
        ConfigurationType type = ConfigurationTypeUtil.findConfigurationType("PyRemoteDebugConfigurationType");
        if (type == null) {
            Messages.showErrorDialog(project,
                "Python Remote Debug configuration type is not available.\nPlease make sure you are using PyCharm Professional.",
                "PyCrunch");
            return null;
        }

        int port = FreePort.find_free_port();
        project.putUserData(GlobalKeys.REMOTE_DEBUG_PORT_KEY, port);

        RunManager runManager = RunManager.getInstance(project);
        ConfigurationFactory factory = type.getConfigurationFactories()[0];
        RunnerAndConfigurationSettings settings = runManager.createConfiguration("pycrunch-engine - debugger", factory);

        Element configElement = new Element("configuration");
        configElement.addContent(new Element("option")
            .setAttribute("name", "HOST")
            .setAttribute("value", "127.0.0.1"));
        configElement.addContent(new Element("option")
            .setAttribute("name", "PORT")
            .setAttribute("value", String.valueOf(port)));

        try {
            settings.getConfiguration().readExternal(configElement);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure Python Remote Debug", e);
        }

        return settings;
    }
}
