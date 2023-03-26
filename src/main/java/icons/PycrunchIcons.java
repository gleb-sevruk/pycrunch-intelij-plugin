package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;


public interface PycrunchIcons {
    static Icon TEST_LIST_PENDING = IconLoader.getIcon("/list_pending.png", PycrunchIcons.class);
    static Icon TEST_LIST_QUEUED = IconLoader.getIcon("/list_queued.png", PycrunchIcons.class);
    static Icon TEST_LIST_FAILED = IconLoader.getIcon("/list_failed.png", PycrunchIcons.class);
    static Icon TEST_LIST_SUCCESS = IconLoader.getIcon("/list_success.png", PycrunchIcons.class);

    static Icon CIRCLE_GREEN = IconLoader.getIcon("/circle-green.png", PycrunchIcons.class);
    static Icon CIRCLE_RED= IconLoader.getIcon("/circle-red.png", PycrunchIcons.class);

    static Icon EXCEPTION_CROSS = IconLoader.getIcon("/exception-cross.png", PycrunchIcons.class);
    static Icon CIRCLE_PROGRESS = IconLoader.getIcon("/circle-progress.png", PycrunchIcons.class);
    static Icon CIRCLE_PENDING = IconLoader.getIcon("/circle-pending.png", PycrunchIcons.class);

    static Icon TOOLBAR_TOGGLE_PENDING = IconLoader.getIcon("/pending.png", PycrunchIcons.class);
    static Icon TOOLBAR_TOGGLE_FAILED = IconLoader.getIcon("/cross.png", PycrunchIcons.class);
    static Icon TOOLBAR_TOGGLE_PINNED = IconLoader.getIcon("/pin.png", PycrunchIcons.class);
    static Icon TOOLBAR_TOGGLE_SUCCESS = IconLoader.getIcon("/check.png", PycrunchIcons.class);

    static Icon TOOLBAR_RUN = IconLoader.getIcon("/run.png", PycrunchIcons.class);

    static Icon TOOLBAR_CONNECT = IconLoader.getIcon("/connect_icon.png", PycrunchIcons.class);
}

