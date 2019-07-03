import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.InputStream;
import java.net.URL;

public  class PyCrunchGutterIconRenderer extends GutterIconRenderer implements DumbAware {
    private final Icon _imageIcon3;
    private final Icon _imageIcon2;
    private final Icon _imageIcon;
    private int _line;

    public PyCrunchGutterIconRenderer(int line) {
        super();
        this._line = line;

        URL resource = getClass().getResource("/circle-green.png");
        _imageIcon = new ImageIcon(resource);
        _imageIcon2 =new ImageIcon(getClass().getResource("/cherry-icon.png"));
        _imageIcon3 = new ImageIcon(getClass().getResource("/circle-red.png"));

    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof PyCrunchGutterIconRenderer) && ((PyCrunchGutterIconRenderer) o)._line == _line;
    }

    @Override
    public int hashCode() {
        return _imageIcon.hashCode();
    }

    @Override
    public @NotNull Icon getIcon() {
        if (_line % 3 == 0 ){
            return _imageIcon3;
        }
        if (_line % 2 == 0 ){
            return _imageIcon2;
        }

        return _imageIcon;
    }
    public AnAction getClickAction() {
        return null;
    }

    @Nullable
    public AnAction getMiddleButtonClickAction() {
        return null;
    }

    @Nullable
    public AnAction getRightButtonClickAction() {
        return null;
    }

}
