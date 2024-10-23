package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.PycrunchHighlighterMarkersState;
import com.gleb.pycrunch.shared.*;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.PythonConfigurationType;
import com.jetbrains.python.run.PythonRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RunPycrunchEngineAction extends AnAction {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public RunPycrunchEngineAction() {
        super("Run/Restart PyCrunch Engine");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        CachedRuntimeConfigurations cache = project.getService(CachedRuntimeConfigurations.class);
        cache.cleanup_disposed_projects();
        cleanupMarkers(project);
        buildConfigurationAndRun(project, cache);
        connectToEngine(project);
    }

    private void cleanupMarkers(@NotNull Project project) {
        project.getService(PycrunchHighlighterMarkersState.class)
                .cleanup_everything(project);
    }

    private void connectToEngine(@NotNull Project project) {
        executor.schedule(() -> {
            try {
                project.getService(PycrunchConnector.class)
                        .AttachToEngine(project);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, TimeUnit.SECONDS);
    }

    private void buildConfigurationAndRun(@NotNull Project project, @NotNull CachedRuntimeConfigurations cache) {
        RunManager runManager = RunManager.getInstance(project);

        // First try to find existing configuration
        RunnerAndConfigurationSettings settings = runManager.findConfigurationByName("pycrunch-engine - auto");

        if (settings == null) {
            // Only create if it doesn't exist
            settings = createRunConfiguration(project);
            runManager.addConfiguration(settings);
            runManager.setSelectedConfiguration(settings);
        }

        // Store in cache
        cache._map.put(project, settings);

        // Run the configuration
        ExecutionUtil.runConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
    }

    @NotNull
    private RunnerAndConfigurationSettings createRunConfiguration(@NotNull Project project) {
        RunManager runManager = RunManager.getInstance(project);
        PythonConfigurationType.PythonConfigurationFactory factory = PythonConfigurationType.getInstance().getFactory();

        RunnerAndConfigurationSettings settings = runManager.createConfiguration(
                "pycrunch-engine - auto",
                factory
        );

        PythonRunConfiguration configuration = (PythonRunConfiguration) settings.getConfiguration();
        String basePath = RecentlyUsedFolders.getLastSelectedFolder(project);

        configuration.setWorkingDirectory(basePath);
        configuration.setScriptName("pycrunch.main");
        configuration.setModuleMode(true);

        int port = FreePort.find_free_port();
        project.putUserData(GlobalKeys.PORT_KEY, port);
        configuration.setScriptParameters("--port=" + port);

        return settings;
    }
}