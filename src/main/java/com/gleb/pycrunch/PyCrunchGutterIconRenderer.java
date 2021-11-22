package com.gleb.pycrunch;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.PycrunchCachedIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public  class PyCrunchGutterIconRenderer extends GutterIconRenderer implements DumbAware {
    public int _line;
    private String status;
    public String _filename;
    private Project _project;

    public PyCrunchGutterIconRenderer(int line, String status, String filename, Project project) {
        super();
        this._line = line;
        this.status = status;
        _filename = filename;
        _project = project;

    }

    @Override
    @NotNull
    public GutterIconRenderer.Alignment getAlignment() {
        return Alignment.LEFT;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof PyCrunchGutterIconRenderer) && ((PyCrunchGutterIconRenderer) o)._line == _line;
    }

    @Override
    public int hashCode() {
        return PycrunchCachedIcons.CIRCLE_GREEN.hashCode();
    }

    @Override
    public @NotNull Icon getIcon() {
//        if (_line % 3 == 0 ){
//            return _imageRed;
//        }
//        if (_line % 2 == 0 ){
//            return _imageIcon2;
//        }
        switch (status) {
            case "success":
                return PycrunchCachedIcons.CIRCLE_GREEN;
            case "failed":
                return PycrunchCachedIcons.CIRCLE_RED;
            case "queued":
            case "pending":
                return PycrunchCachedIcons.CIRCLE_PROGRESS;
            default:
                return PycrunchCachedIcons.CIRCLE_GREEN;
        }
    }
    public AnAction getClickAction() {
        return new ShowCoveringTestsAction(_project, this);
    }

    @Nullable
    public AnAction getMiddleButtonClickAction() {
        return null;
    }

    @Nullable
    public AnAction getRightButtonClickAction() {
//        return new ShowCoveringTestsAction();
        return null;
    }

}
