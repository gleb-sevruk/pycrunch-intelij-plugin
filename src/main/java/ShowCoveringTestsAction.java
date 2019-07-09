

import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBList;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointsDialogFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.HashSet;

public class ShowCoveringTestsAction extends AnAction implements AnAction.TransparentUpdate, DumbAware {
    private final MyPycrunchConnector _connector;
    private Object myInitialBreakpoint;
    private String _filename;
    private int _line;

    public ShowCoveringTestsAction(String filename, int line) {
        this._filename = filename;
        this._line = line;
        _connector = ServiceManager.getService(MyPycrunchConnector.class);

    }


    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("AAA?!");
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        JComponent component = editor.getComponent();
        HashSet<String> strings = _connector.GetCoveredLinesForFile(_filename).TestsAtLine(_line + 1);
        JBPopupFactory.getInstance().createListPopup(new PycrunchTestGutterPopup(strings, _connector, project, _line + 1)).showInBestPositionFor(editor);
    }

    public void update(@NotNull AnActionEvent event) {

        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
