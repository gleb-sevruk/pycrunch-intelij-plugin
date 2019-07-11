

import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointsDialogFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

public class ShowCoveringTestsAction extends AnAction implements AnAction.TransparentUpdate, DumbAware {
    private final MyPycrunchConnector _connector;
    private Object myInitialBreakpoint;
    private PyCrunchGutterIconRenderer _gutterIcon;

    public ShowCoveringTestsAction(PyCrunchGutterIconRenderer gutterIcon) {
        _gutterIcon = gutterIcon;
        _connector = ServiceManager.getService(MyPycrunchConnector.class);

    }


    public void actionPerformed(@NotNull AnActionEvent e) {
//        System.out.println("AAA?!");
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        JComponent component = editor.getComponent();
        HashSet<String> strings = _connector.GetCoveredLinesForFile(_gutterIcon._filename).TestsAtLine(_gutterIcon._line + 1);
        EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        Point centerPoint = gutterComponent.getCenterPoint(_gutterIcon);
        Point point = centerPoint;
        VisualPosition visualPosition = editor.getCaretModel().getVisualPosition();
        int vpostoxy = editor.visualPositionToXY(visualPosition).y;
        int y = vpostoxy + editor.getLineHeight() / 2;
        int x = gutterComponent.getWidth();
        point = new Point(x, y);

        JBPopupFactory.getInstance().createListPopup(new PycrunchTestGutterPopup(strings, _connector, project, _gutterIcon._line + 1)).show(new RelativePoint(gutterComponent, centerPoint));
    }

    public void update(@NotNull AnActionEvent event) {

        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
