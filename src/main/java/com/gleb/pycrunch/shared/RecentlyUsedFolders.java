package com.gleb.pycrunch.shared;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentlyUsedFolders {
    private static final String PYCRUNCH_ENGINE_RECENT_FOLDERS = "pycrunch.engine.recentFolders";
    private static final String LAST_SELECTED_FOLDER = "pycrunch.engine.lastSelectedFolder";
    private static final int MAX_SIZE = 10;

    public static String getLastSelectedFolder(Project project) {
        PropertiesComponent props = PropertiesComponent.getInstance(project);
        String value = props.getValue(LAST_SELECTED_FOLDER);
        if (value == null) {
            value = project.getBasePath();
        }

        return value;
    }
    public static void saveLastSelectedFolder(Project project, String folderPath) {
        PropertiesComponent props = PropertiesComponent.getInstance(project);
        props.setValue(LAST_SELECTED_FOLDER, folderPath);
    }
    public static List<String> getRecentFolders(Project project) {
        PropertiesComponent props = PropertiesComponent.getInstance(project);
        String value = props.getValue(PYCRUNCH_ENGINE_RECENT_FOLDERS);
        if (value == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(",")));
    }

    public static void addRecentFolder(Project project, String folderPath) {
        PropertiesComponent props = PropertiesComponent.getInstance(project);
        List<String> recentFolders = getRecentFolders(project);
        recentFolders.remove(folderPath);
        recentFolders.add(0, folderPath);
        if (recentFolders.size() > MAX_SIZE) {
            recentFolders = recentFolders.subList(0, MAX_SIZE);
        }
        props.setValue(PYCRUNCH_ENGINE_RECENT_FOLDERS, String.join(",", recentFolders));
    }
}
