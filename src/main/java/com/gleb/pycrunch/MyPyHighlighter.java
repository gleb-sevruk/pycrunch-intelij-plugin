package com.gleb.pycrunch;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class MyPyHighlighter extends  AnAction {
    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public MyPyHighlighter() {
        // Set the menu item name.
        super("Highlight Some _Lines");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        PycrunchHighlighterMarkersState connector = ServiceManager.getService(project, PycrunchHighlighterMarkersState.class);

        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        connector.invalidate_markers(editor.getDocument(), project);
    }


}


