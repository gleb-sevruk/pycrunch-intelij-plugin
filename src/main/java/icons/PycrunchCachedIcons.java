package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

// For some reason since 2021.1 icons in gutter were not cached and loaded on each appearance in the viewport
// This will ensure the icons are not loaded every time
public final class PycrunchCachedIcons {
    public static final Icon CIRCLE_GREEN = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_GREEN);
    public static final Icon CIRCLE_RED = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_RED);
    public static final Icon EXCEPTION_CROSS = IconLoader.getIconSnapshot(PycrunchIcons.EXCEPTION_CROSS);
    public static final Icon CIRCLE_PROGRESS = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_PROGRESS);
    public static final Icon CIRCLE_PENDING = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_PENDING);

    private PycrunchCachedIcons() {
    }
}
