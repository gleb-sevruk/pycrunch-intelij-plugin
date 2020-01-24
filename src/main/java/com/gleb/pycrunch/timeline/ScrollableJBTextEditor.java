package com.gleb.pycrunch.timeline;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;

public class ScrollableJBTextEditor extends EditorTextField {
    public ScrollableJBTextEditor(String text, Project project, JsonFileType jsonFileType) {
        super(text, project, jsonFileType);
    }

    @Override
    protected EditorEx createEditor() {
        EditorEx x = super.createEditor();
        x.setVerticalScrollbarVisible(true);
        x.setHorizontalScrollbarVisible(true);
//        x.setSc
        return x;
    }

    @Override
    protected boolean isOneLineMode() {
        return false;
    }
}
