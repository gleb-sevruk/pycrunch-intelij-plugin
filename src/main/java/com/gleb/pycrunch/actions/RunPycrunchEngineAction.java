package com.gleb.pycrunch.actions;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.shared.GlobalKeys;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configuration.AbstractRunConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.run.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class RunPycrunchEngineAction extends AnAction {
    private static int counter = 0;
    private final int id;
    private Map<Project, RunnerAndConfigurationSettings> _map;

    public RunPycrunchEngineAction() {
        super("_Run PyCrunch Engine");
        counter++;
        id = counter;
        if (_map == null) {
            _map = new Hashtable<>();
        }
    }

    public void actionPerformed(AnActionEvent event) {
        for (Project p : _map.keySet()) {
            if (p.isDisposed()) {
                _map.remove(p);
            }
        }
        Project project = event.getData(PlatformDataKeys.PROJECT);
        RunnerAndConfigurationSettings settings = _map.get(project);
        PythonConfigurationType.PythonConfigurationFactory factory = PythonConfigurationType.getInstance().getFactory();
        if (settings == null) {
            RunManager runManager = RunManager.getInstance(project);
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



            int port = 7777;
            ServerSocket s = null;
            try {
                s = new ServerSocket(0);
                port = s.getLocalPort();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            project.putUserData(GlobalKeys.PORT_KEY, port);

            parameters.setScriptParameters("--port=" + port);

            // should be working dir!
//        String folderName = xxx.getFolderName();
//        System.out.println(folderName);
            parameters.setModuleMode(true);
            _map.put(project, settings);
        }
        Executor runExecutorInstance = DefaultRunExecutor.getRunExecutorInstance();

        ExecutionUtil.runConfiguration(settings, runExecutorInstance);
//        ExecutionUtil.runConfiguration(xxx, runExecutorInstance);
//        runManager.addConfiguration(xxx);
//        PythonRunner pythonRunner = new PythonRunner();
//        String txt= Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon());
//        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information", Messages.getInformationIcon());
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