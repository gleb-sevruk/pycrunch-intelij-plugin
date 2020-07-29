package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;


public interface PycrunchIcons {
    Icon TEST_LIST_PENDING = IconLoader.getIcon("/list_pending.png");
    Icon TEST_LIST_QUEUED = IconLoader.getIcon("/list_queued.png");
    Icon TEST_LIST_FAILED = IconLoader.getIcon("/list_failed.png");
    Icon TEST_LIST_SUCCESS = IconLoader.getIcon("/list_success.png");

    Icon CIRCLE_GREEN = IconLoader.getIcon("/circle-green.png");
    Icon CIRCLE_RED= IconLoader.getIcon("/circle-red.png");
    Icon CIRCLE_PROGRESS= IconLoader.getIcon("/circle-progress.png");
}
