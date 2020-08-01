package com.gleb.pycrunch.actions.toolbarActions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.PycrunchIcons;
import org.jetbrains.annotations.NotNull;

public class DynamicActionGroup extends ActionGroup {

    /**
     * Returns an array of menu actions for the group.
     *
     * @param e Event received when the associated group-id menu is chosen.
     * @return AnAction[]  An instance of AnAction, in this case containing a single instance of the
     * PopupDialogAction class.
     */
    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent e) {
        return new AnAction[]{
                new PopupDialogAction("Action Added at Runtime",
                        "Dynamic Action Demo",
                        PycrunchIcons.TOOLBAR_RUN)
        };
    }
}