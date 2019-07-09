import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;
import java.util.HashSet;

public class PycrunchTestGutterPopup extends BaseListPopupStep {
    private final ImageIcon _imageGreen;
    private final ImageIcon _imageRed;
    private HashSet<String> strings;
    private MyPycrunchConnector _connector;
    private Project _project;
    private int _line;

    public PycrunchTestGutterPopup(HashSet<String> strings, MyPycrunchConnector connector, Project project, int line) {
        super("A?", strings.toArray());
        this.strings = strings;
        _connector = connector;
        _project = project;
        _line = line;
        _imageGreen = new ImageIcon(getClass().getResource("/circle-green.png"));
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

        return _imageGreen;
    }


    @Override
    public @Nullable PopupStep onChosen(Object o, boolean b) {
        PycrunchTestMetadata testByFqn = _connector.FindTestByFqn((String) o);
        if (testByFqn == null) {
            System.out.println("test by fqn is null !!!");
            return FINAL_CHOICE;
        }
        // todo add line number in discovery metadata here!
        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(testByFqn.filename);
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(this._project, fileByPath, _line, -1, true);
        openFileDescriptor.navigate(true);
        return FINAL_CHOICE;
    }
}
