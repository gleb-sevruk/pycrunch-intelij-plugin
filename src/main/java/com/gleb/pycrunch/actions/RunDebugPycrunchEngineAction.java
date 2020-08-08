package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.shared.GlobalKeys;
import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.debugger.remote.*;
import com.jetbrains.python.run.AbstractPythonRunConfigurationParams;
import com.jetbrains.python.run.PythonConfigurationType;
import com.jetbrains.python.run.PythonRunConfigurationParams;
import com.jetbrains.python.run.PythonRunConfigurationProducer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("Duplicates")
public class RunDebugPycrunchEngineAction extends AnAction {
    private static int counter = 0;
    private final int id;
    private ConcurrentHashMap<Project, RunnerAndConfigurationSettings> _map;

    public RunDebugPycrunchEngineAction() {
        super("_Run Remote Debugger Process");
        counter++;
        id = counter;
        if (_map == null) {
            _map = new ConcurrentHashMap<>();
        }
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        build_configuration_and_run_debugger(project);
    }

    private void build_configuration_and_run_debugger(Project project) {
          create_run_configuration_for_project(project);

//        RunnerAndConfigurationSettings settings = _map.get(project);
//        if (settings == null) {
//            settings = create_run_configuration_for_project(project);
//            _map.put(project, settings);
//        }
//        Executor runExecutorInstance = DefaultRunExecutor.getRunExecutorInstance();
//        @NotNull Executor[] registeredExecutors = ExecutorRegistry.getInstance().getRegisteredExecutors();
////        ExecutionManager.getInstance(project).restartRunProfile().build());
//
//        ExecutionUtil.runConfiguration(settings, runExecutorInstance);
    }

    @NotNull
    private void create_run_configuration_for_project(Project project) {
        RunnerAndConfigurationSettings settings;
        RunManager runManager = RunManager.getInstance(project);

        ConfigurationFactory factory = PyRemoteDebugConfigurationType.getInstance().getConfigurationFactories()[0];
        settings = runManager.createConfiguration("pycrunch-engine - debugger", factory);

        PyRemoteDebugConfigurationParams parameters = (PyRemoteDebugConfigurationParams) settings.getConfiguration();
        parameters.setHost("127.0.0.1");
        parameters.setPort(21349);

//        int port = find_available_port();

//        project.putUserData(GlobalKeys.PORT_KEY, port);
        ProgramRunnerUtil.executeConfiguration(settings, DefaultDebugExecutor.getDebugExecutorInstance());
    }

    private int find_available_port() {
        int port = 7777;
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    public void actionPerformed222(@NotNull AnActionEvent e) {
        PsiFile var2 = (PsiFile) e.getData(CommonDataKeys.PSI_FILE);
        if (var2 != null) {
            Project var3 = e.getProject();
            if (var3 != null) {
                ConfigurationContext var4 = ConfigurationContext.getFromContext(e.getDataContext());

                ConfigurationFromContext var5 = ((PythonRunConfigurationProducer) RunConfigurationProducer.getInstance(PythonRunConfigurationProducer.class)).createConfigurationFromContext(var4);

                if (var5 != null) {
                    RunnerAndConfigurationSettings var6 = var5.getConfigurationSettings();
                    PythonRunConfigurationParams var7 = (PythonRunConfigurationParams) var6.getConfiguration();
                    var7.setShowCommandLineAfterwards(true);
                    RunManager var8 = RunManager.getInstance(var3);
                    var8.setTemporaryConfiguration(var6);
                    var8.setSelectedConfiguration(var6);
                    ExecutionEnvironmentBuilder var9 = ExecutionEnvironmentBuilder.createOrNull(DefaultRunExecutor.getRunExecutorInstance(), var6);
                    if (var9 != null) {
                        ExecutionManager.getInstance(var3).restartRunProfile(var9.build());
                    }

                }
            }
        }
    }
}