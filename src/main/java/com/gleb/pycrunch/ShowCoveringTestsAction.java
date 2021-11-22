package com.gleb.pycrunch;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashSet;

public class ShowCoveringTestsAction extends AnAction implements  DumbAware {
    private final PycrunchConnector _connector;
    private Object myInitialBreakpoint;
    private final Project _project;
    private PyCrunchGutterIconRenderer _gutterIcon;

    public ShowCoveringTestsAction(Project project, PyCrunchGutterIconRenderer gutterIcon) {
        _project = project;
        _gutterIcon = gutterIcon;
        _connector = ServiceManager.getService(_project, PycrunchConnector.class);

    }


    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        HashSet<String> strings = _connector.GetCoveredLinesForFile(_gutterIcon._filename).TestsAtLine(_gutterIcon._line + 1);
        EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        Point centerPoint = gutterComponent.getCenterPoint(_gutterIcon);
        Point point = centerPoint;
        VisualPosition visualPosition = editor.getCaretModel().getVisualPosition();
        int vpostoxy = editor.visualPositionToXY(visualPosition).y;
        int y = vpostoxy + editor.getLineHeight() / 2;
        int x = gutterComponent.getWidth();
//        point = new Point(x, y);

        JBPopupFactory.getInstance().createListPopup(new PycrunchTestGutterPopup(strings, _connector, project, _gutterIcon._line + 1)).show(new RelativePoint(gutterComponent, centerPoint));
    }

    public void update(@NotNull AnActionEvent event) {

        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
