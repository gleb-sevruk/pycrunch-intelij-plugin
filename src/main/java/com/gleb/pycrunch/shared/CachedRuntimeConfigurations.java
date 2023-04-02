package com.gleb.pycrunch.shared;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;

import java.util.concurrent.ConcurrentHashMap;

public class CachedRuntimeConfigurations {
    public ConcurrentHashMap<Project, RunnerAndConfigurationSettings> _map;
    public CachedRuntimeConfigurations() {
        _map = new ConcurrentHashMap<>();
    }
    public void cleanup_disposed_projects() {
        for (Project p : _map.keySet()) {
            if (p.isDisposed()) {
                _map.remove(p);
            }
        }
    }

    public void remove_project(Project project) {
        _map.remove(project);
    }
}
