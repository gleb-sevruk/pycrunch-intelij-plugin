import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import static com.intellij.openapi.components.ServiceManager.getService;

public class MyProjComponent implements ProjectComponent {

    public MyProjComponent(Project project) {
        System.out.println("aaa");
    }
    @Override
    public void projectOpened() {
       MyCounter counter = ServiceManager.getService(MyCounter.class);
        counter.IncreaseCounter();
    }

    @Override
    public void projectClosed() {

    }
}
