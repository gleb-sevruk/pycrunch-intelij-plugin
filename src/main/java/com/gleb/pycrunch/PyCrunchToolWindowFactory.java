package com.gleb.pycrunch;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.*;
import com.intellij.util.messages.MessageBus;

public class PyCrunchToolWindowFactory implements ToolWindowFactory {
    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        MessageBus bus = project.getMessageBus();
        PycrunchConnector connector = project.getService(PycrunchConnector.class);
        toolWindow.setAutoHide(false);
        PycrunchToolWindow pycrunchToolWindow = new PycrunchToolWindow(toolWindow, project, bus, connector);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
//        TODO: Change to after 2022.2 only is supported
//        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(pycrunchToolWindow.getContent(), "Tests", false);
        toolWindow.getContentManager().addContent(content);
        try {
            connector.AttachToEngine(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


//import com.intellij.ui.content.Content;
//        import com.intellij.ui.content.ContentFactory;
//
//        import java.lang.reflect.Method;

//public class ContentFactoryUtil {
//    public static Content createContent() {
//        ContentFactory contentFactory;
//
//        try {
//            // Check if the getInstance() method is available (for IntelliJ version 2020.3 and later)
//            Method getInstanceMethod = ContentFactory.class.getMethod("getInstance");
//            contentFactory = (ContentFactory) getInstanceMethod.invoke(null);
//        } catch (NoSuchMethodException e) {
//            // If the getInstance() method is not available, use the deprecated SERVICE field (for older IntelliJ versions)
//            contentFactory = ContentFactory.SERVICE.getInstance();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to get ContentFactory instance", e);
//        }
//
//        return contentFactory.createContent(null, "", false);
//    }
//}