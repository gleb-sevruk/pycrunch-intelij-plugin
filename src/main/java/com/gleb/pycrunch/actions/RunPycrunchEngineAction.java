package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.PycrunchHighlighterMarkersState;
import com.gleb.pycrunch.shared.FreePort;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.AbstractPythonRunConfigurationParams;
import com.jetbrains.python.run.PythonConfigurationType;
import com.jetbrains.python.run.PythonRunConfigurationParams;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RunPycrunchEngineAction extends AnAction {
    private ConcurrentHashMap<Project, RunnerAndConfigurationSettings> _map;

    public RunPycrunchEngineAction() {
        super("_Run/Restart PyCrunch Engine");
        if (_map == null) {
            _map = new ConcurrentHashMap<>();
        }
    }

    public void actionPerformed(AnActionEvent event) {
        cleanup_disposed_projects();
        Project project = event.getData(PlatformDataKeys.PROJECT);
        cleanup_all_markers(project);
        build_configuration_and_run_engine(project);
        connect_to_engine(project);
    }

    private void cleanup_all_markers(Project project) {
        PycrunchHighlighterMarkersState connector = project.getService(PycrunchHighlighterMarkersState.class);
        connector.cleanup_everything(project);
    }

    private void connect_to_engine(Project project) {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

        exec.schedule(new Runnable() {
            public void run() {
                PycrunchConnector connector = project.getService(PycrunchConnector.class);
                try {
                    connector.AttachToEngine(project);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1, TimeUnit.SECONDS);


    }

    private void build_configuration_and_run_engine(Project project) {
        RunnerAndConfigurationSettings settings = _map.get(project);
        if (settings == null) {
            settings = create_run_configuration_for_project(project);
            _map.put(project, settings);
        }
        Executor runExecutorInstance = DefaultRunExecutor.getRunExecutorInstance();
        ExecutionUtil.runConfiguration(settings, runExecutorInstance);
    }

    private void cleanup_disposed_projects() {
        for (Project p : _map.keySet()) {
            if (p.isDisposed()) {
                _map.remove(p);
            }
        }
    }

    @NotNull
    private RunnerAndConfigurationSettings create_run_configuration_for_project(Project project) {
        RunnerAndConfigurationSettings settings;
        RunManager runManager = RunManager.getInstance(project);

        PythonConfigurationType.PythonConfigurationFactory factory = PythonConfigurationType.getInstance().getFactory();

        settings = runManager.createConfiguration("pycrunch-engine - auto", factory);

//        runManager.addConfiguration(xxx);
//        runManager.
//        runManager.makeStable(xxx);
//        List<RunConfiguration> allConfigurationsList = runManager.getAllConfigurationsList();
        PythonRunConfigurationParams parameters = (PythonRunConfigurationParams) settings.getConfiguration();
        String basePath = project.getBasePath();
        AbstractPythonRunConfigurationParams baseParams = parameters.getBaseParams();
        baseParams.setWorkingDirectory(basePath);
        parameters.setScriptName("pycrunch.main");

        int port = FreePort.find_free_port();

        project.putUserData(GlobalKeys.PORT_KEY, port);

        parameters.setScriptParameters("--port=" + port);

        // should be working dir!
//        String folderName = xxx.getFolderName();
//        System.out.println(folderName);
        parameters.setModuleMode(true);
        return settings;
    }
}