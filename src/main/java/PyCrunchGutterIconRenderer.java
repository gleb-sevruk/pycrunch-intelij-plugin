import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.URL;

public  class PyCrunchGutterIconRenderer extends GutterIconRenderer implements DumbAware {
    private final Icon _imageRed;
    private final Icon _imageIcon2;
    private final Icon _imageGreen;
    private int _line;
    private String status;

    public PyCrunchGutterIconRenderer(int line, String status) {
        super();
        this._line = line;
        this.status = status;

        URL resource = getClass().getResource("/circle-green.png");
        _imageGreen = new ImageIcon(resource);
        _imageIcon2 =new ImageIcon(getClass().getResource("/cherry-icon.png"));
        _imageRed = new ImageIcon(getClass().getResource("/circle-red.png"));

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
        if (status.equals("failed")) {
            return _imageRed;
        }

        return _imageGreen;
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
