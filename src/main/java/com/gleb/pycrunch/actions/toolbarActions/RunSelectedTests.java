package com.gleb.pycrunch.actions.toolbarActions;

import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

public class RunSelectedTests extends AnAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if (project == null){
            return;
        }
        MessageBus bus = project.getMessageBus();
        try {
            ((PycrunchToolbarBus) bus.syncPublisher(PycrunchToolbarBus.CHANGE_ACTION_TOPIC)).runSelectedTests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean displayTextInToolbar() {
        // Display 'run' text near icon to improve hit region
        return true;
    }
}
