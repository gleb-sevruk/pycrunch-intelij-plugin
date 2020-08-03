package com.gleb.pycrunch;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.PycrunchIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;

public  class PyCrunchGutterIconRenderer extends GutterIconRenderer implements DumbAware {
    private final Icon _imageRed;
    private final Icon _imageGreen;
    private final Icon _imageProgress;
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
        _imageGreen = PycrunchIcons.CIRCLE_GREEN;
        _imageRed = PycrunchIcons.CIRCLE_RED;
        _imageProgress = PycrunchIcons.CIRCLE_PROGRESS;

    }

    @Override
    @NotNull
    public GutterIconRenderer.Alignment getAlignment() {
        GutterIconRenderer.Alignment var10000 = Alignment.RIGHT ;
        return var10000;
    }
    @Override
    public boolean equals(Object o) {
        return (o instanceof PyCrunchGutterIconRenderer) && ((PyCrunchGutterIconRenderer) o)._line == _line;
    }

    @Override
    public int hashCode() {
        return _imageGreen.hashCode();
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
                return _imageGreen;
            case "failed":
                return _imageRed;
            case "queued":
            case "pending":
                return _imageProgress;
            default:
                return _imageGreen;
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
