package com.gleb.pycrunch;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashSet;

public class PycrunchTestGutterPopup extends BaseListPopupStep {
    private final ImageIcon _imageGreen;
    private final ImageIcon _imageRed;
    private final Icon _imageProgress;
    private HashSet<String> strings;
    private MyPycrunchConnector _connector;
    private Project _project;
    private int _line;

    public PycrunchTestGutterPopup(HashSet<String> strings, MyPycrunchConnector connector, Project project, int line) {
        super("Tests covering this line:", strings.toArray());
        this.strings = strings;
        _connector = connector;
        _project = project;
        _line = line;
        _imageGreen = new ImageIcon(getClass().getResource("/circle-green.png"));
        _imageProgress = new ImageIcon(getClass().getResource("/circle-progress.png"));
        _imageRed = new ImageIcon(getClass().getResource("/circle-red.png"));
    }

    @Override
    public @NotNull String getTextFor(Object o) {
        return o.toString();
    }

    @Override
    public Icon getIconFor(Object value) {
        String status = _connector.GetTestStatus((String) value);

        if (status.equals("failed")) {
            return _imageRed;
        }
        if (status.equals("queued")) {
            return _imageProgress;
        }
        return _imageGreen;
    }


    @Override
    public @Nullable PopupStep onChosen(Object o, boolean b) {
        PycrunchTestMetadata testByFqn = _connector.FindTestByFqn((String) o);
        if (testByFqn == null) {
            System.out.println("test by fqn is null !!!");
            return FINAL_CHOICE;
        }
        NavigateToTest toTest = new NavigateToTest();
        toTest.Go(testByFqn, _connector);
        // todo add line number in discovery metadata here!


//        FileDocumentManager.getInstance().getFile(_connector._project.)
//        FileDocumentManager.getInstance().getDocument(fileByPath).get
        return FINAL_CHOICE;
    }
}
