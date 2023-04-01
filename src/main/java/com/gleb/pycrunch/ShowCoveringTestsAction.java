package com.gleb.pycrunch;

import com.gleb.pycrunch.exceptionPreview.ExceptionPopupForm;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class ShowCoveringTestsAction extends AnAction implements  DumbAware {
    private final PycrunchConnector _connector;
    private Object myInitialBreakpoint;
    private final Project _project;
    private PyCrunchGutterIconRenderer _gutterIcon;

    public ShowCoveringTestsAction(Project project, PyCrunchGutterIconRenderer gutterIcon) {
        _project = project;
        _gutterIcon = gutterIcon;
        _connector = _project.getService(PycrunchConnector.class);

    }


    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        Project project = e.getData(PlatformDataKeys.PROJECT);
        var singleFileCombinedCoverage = _connector.GetCoveredLinesForFile(_gutterIcon._filename);

        // Pycharm counts lines from 0, pycrunch from 1
        int current_line_number = _gutterIcon._line + 1;

        ;
        HashSet<String> test_names = singleFileCombinedCoverage.TestsAtLine(current_line_number);
        var regularTooltip = !singleFileCombinedCoverage._exceptions.contains(current_line_number);
        EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        Point centerPoint = gutterComponent.getCenterPoint(_gutterIcon);
        Point point = centerPoint;
        if (regularTooltip) {

            VisualPosition visualPosition = editor.getCaretModel().getVisualPosition();
            int vpostoxy = editor.visualPositionToXY(visualPosition).y;
            int y = vpostoxy + editor.getLineHeight() / 2;
            int x = gutterComponent.getWidth();
            JBPopupFactory.getInstance().createListPopup(new PycrunchTestGutterPopup(test_names, _connector, project, current_line_number)).show(new RelativePoint(gutterComponent, centerPoint));

        } else {
            //OPENAI:
            var tests = build_list_of_test_failing_on_this_line(test_names, _gutterIcon._filename, current_line_number);

// Set the maximum size of the JScrollPane


            ExceptionPopupForm exceptionPopupForm = new ExceptionPopupForm(_project, tests);
            JPanel mainPanel = exceptionPopupForm.getMainPanel();
            JPanel frame = new JPanel();
            frame.setLayout(new BorderLayout());

            Dimension maxPopupSize = getMaximumPopupSize();

            frame.setMaximumSize(maxPopupSize);
            frame.setPreferredSize(mainPanel.getPreferredSize());
            frame.add(mainPanel);
            var popupFactory = JBPopupFactory.getInstance().createComponentPopupBuilder(frame, mainPanel);
            popupFactory.setResizable(true);
            popupFactory.setMovable(true);
            popupFactory.setShowBorder(true);
            popupFactory.setTitle("Exception at line " + current_line_number);
            popupFactory.setFocusable(true);
            popupFactory.setRequestFocus(true);
            var popup = popupFactory.createPopup();
//            popupFactory.setMinSize(exceptionPopupForm._preferredSize);

            var preferredPopupSize = exceptionPopupForm._preferredSize;
            if (preferredPopupSize.width > maxPopupSize.width || preferredPopupSize.height > maxPopupSize.height) {
                popup.setSize(maxPopupSize);
            } else {
                popup.setSize(preferredPopupSize);
            }

            popup.setSize(exceptionPopupForm._preferredSize);
            popup.show(new RelativePoint(gutterComponent, centerPoint));;

//            END OPENAI
        }
    }

    @NotNull
    private static Dimension getMaximumPopupSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = (int) (screenSize.width * 0.8); // 80% of screen width
        int maxHeight = (int) (screenSize.height * 0.8); // 80% of screen height
        Dimension maxPopupSize = new Dimension(maxWidth, maxHeight);
        return maxPopupSize;
    }

    private ArrayList<TestRunResult>  build_list_of_test_failing_on_this_line(HashSet<String> test_names, String filename, int currentLineNumber) {
        // test_names is optimization to not look everywhere
        var results = new ArrayList<TestRunResult>();
        var testRunResults = _connector.get_test_run_results();
        if (test_names.size() == 0) {
//            Hack, search through all tests if we cannot find in failing first
            test_names.addAll(testRunResults.keySet());
        }
        for (var test_name : test_names) {
            var testRunResult = testRunResults.get(test_name);
            if (testRunResult == null)
                continue;

            if (testRunResult.captured_exception != null) {
                if (testRunResult.captured_exception.filename.equals(filename)) {
                    if (testRunResult.captured_exception.line_number == currentLineNumber) {
                        results.add(testRunResult);
                    }
                }
            }
        }
        return results;
    }

    public void update(@NotNull AnActionEvent event) {
        event.getPresentation().setEnabled(event.getProject() != null);
    }
}
