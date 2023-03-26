package com.gleb.pycrunch.exceptionPreview;

import com.gleb.pycrunch.TestRunResult;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import org.json.JSONException;

public class ExceptionPopupForm {
    public Dimension _preferredSize;
    private final ArrayList<TestRunResult> _tests_with_exceptions;
    private EditorEx _editor;
    private Document _document;
    private JButton _btnPrevTest;
    private JLabel _lblTestName;
    private JButton _btnNextTest;
    private int currentIndex = 0;
    private int totalTests = 0;
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JPanel _scrollpaneExceptionMessage;
    private Tree tree;
    private JScrollPane _treeViewSurface;
    private TestRunResult _currentTest;
    private Project _project;
    private TreeSpeedSearch _treeSpeedSearch;

    public JPanel getMainPanel() {
        return mainPanel;
    }
    public ExceptionPopupForm(Project project, ArrayList<TestRunResult> tests_with_exceptions) {
        _project = project;
        mainPanel.setPreferredSize(new Dimension(400, 300));
        currentIndex = 0;
        _tests_with_exceptions = tests_with_exceptions;
        totalTests = tests_with_exceptions.size();
        if (totalTests == 1) {
            _btnPrevTest.setEnabled(false);
            _btnNextTest.setEnabled(false);
        }
        _currentTest = tests_with_exceptions.get(0);
        updateLabel();

        attachEventListeners();

        String exceptionMessage = _currentTest.captured_exception.traceback;
        initialize_editor(project, exceptionMessage);
        scrollToBottom();

        // Set the editor to be read-only
        _editor.setViewer(true);
        _scrollpaneExceptionMessage.setLayout(new BorderLayout());

        computeEditorWidthBasedOnContent();
        _scrollpaneExceptionMessage.add(_editor.getComponent());

        try {
            configureVariablesTree();
            setVariableTreeFromCurrentTest();
        } catch (JSONException e) {
            System.out.println("Error setting variable tree from current test");
        }

        invalidateButtonState();
    }

    private void attachEventListeners() {
        _btnPrevTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle left button click
                // Update the currentIndex variable
                updateTestNavigation(currentIndex - 1, totalTests);
            }
        });

        _btnNextTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle right button click
                // Update the currentIndex variable
                updateTestNavigation(currentIndex + 1, totalTests);
            }
        });
    }

    private void computeEditorWidthBasedOnContent() {
        JComponent editorComponent = _editor.getComponent();

        FontMetrics fontMetrics = editorComponent.getFontMetrics(editorComponent.getFont());
        String[] lines = _currentTest.captured_exception.traceback.split("\n");
        int maxWidth = 200;
        for (String line : lines) {
            int lineWidth = fontMetrics.stringWidth(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }
        _preferredSize = new Dimension(maxWidth, editorComponent.getPreferredSize().height);
        editorComponent.setPreferredSize(_preferredSize);
        editorComponent.revalidate();
    }

    private void initialize_editor(Project project, String exceptionMessage) {
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("py");
        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("ExceptionText_" + _currentTest.fqn + ".py", fileType, exceptionMessage);
        _document = psiFile.getViewProvider().getDocument();
        EditorFactory editorFactory = EditorFactory.getInstance();
        _editor = (EditorEx) editorFactory.createEditor(_document);
        _editor.getContentComponent().setFocusable(true);
        _editor.getContentComponent().requestFocus();
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        EditorHighlighter highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(fileType, scheme, project);
        _editor.setHighlighter(highlighter);
    }

    private void configureVariablesTree() {
        tree.setCellRenderer(new CustomTreeCellRenderer());
        tree.setRootVisible(false);
        _treeSpeedSearch = new TreeSpeedSearch(tree);
        _treeViewSurface.setBorder(BorderFactory.createEmptyBorder());

    }

    private void setVariableTreeFromCurrentTest() throws JSONException {
        VariableTreeJsonTreeStructure treeStructure;
        treeStructure = new VariableTreeJsonTreeStructure(_currentTest.captured_exception.variables);
        var treeModel = new DefaultTreeModel(treeStructure.getRoot());
        tree.setModel(treeModel);
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(
                () -> _editor.getScrollingModel().scrollVertically(_editor.getDocument().getLineEndOffset(_editor.getDocument().getLineCount() - 1))
        );
    }

    private void updateTestNavigation(int currentIndex, int totalTests) {
        this.currentIndex = currentIndex;
        _currentTest = _tests_with_exceptions.get(currentIndex);
        updateLabel();
        invalidateButtonState();
        try {
            setVariableTreeFromCurrentTest();
        } catch (JSONException e) {
            System.out.println("Error setting variable tree from current test");
        }
        updateExceptionMessageContent();
    }

    private void updateExceptionMessageContent() {
        String exceptionMessage = _currentTest.captured_exception.traceback;
        _document.setText(exceptionMessage);
    }

    private void updateLabel() {
        _lblTestName.setText(String.format("Test `%s` [%d..%d]", _currentTest.fqn, currentIndex + 1, totalTests));
    }

    private void invalidateButtonState() {
        _btnPrevTest.setEnabled(currentIndex > 0);
        _btnNextTest.setEnabled(currentIndex < totalTests - 1);
    }

}



