package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

// For some reason since 2021.1 icons in gutter were not cached and loaded on each appearance in the viewport
// This will ensure the icons are not loaded every time
public class PycrunchCachedIcons {
    public static Icon CIRCLE_GREEN = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_GREEN);
    public static Icon CIRCLE_RED = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_RED);
    public static Icon EXCEPTION_CROSS = IconLoader.getIconSnapshot(PycrunchIcons.EXCEPTION_CROSS);
    public static Icon CIRCLE_PROGRESS = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_PROGRESS);
    public static Icon CIRCLE_PENDING = IconLoader.getIconSnapshot(PycrunchIcons.CIRCLE_PENDING);

}
