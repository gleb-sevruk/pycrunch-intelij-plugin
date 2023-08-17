package com.gleb.pycrunch;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import icons.PycrunchIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;

public class PycrunchTestGutterPopup extends BaseListPopupStep {
    private Collection<String> strings;
    private PycrunchConnector _connector;
    private Project _project;
    private int _line;

    public PycrunchTestGutterPopup(Collection<String> strings, PycrunchConnector connector, Project project, int line) {
        super("Tests covering this line:", strings.toArray());
        this.strings = strings;
        _connector = connector;
        _project = project;
        _line = line;
    }

    @Override
    public @NotNull String getTextFor(Object o) {
        return o.toString();
    }

    @Override
    public Icon getIconFor(Object value) {
        String status = _connector.GetTestStatus((String) value);

        if (status.equals("failed")) {
            return PycrunchIcons.CIRCLE_RED;
        }
//        This smells strange, why two icons on the same state? (seems to be fixed, but lets keep monitoring)
        if (status.equals("queued")) {
            return PycrunchIcons.CIRCLE_PROGRESS;
        }

        if (status.equals("pending")) {
            return PycrunchIcons.CIRCLE_PROGRESS;
        }

        return PycrunchIcons.CIRCLE_GREEN;
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
