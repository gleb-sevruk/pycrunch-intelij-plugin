import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.util.ArrayList;

public class MyToolWindow {
    private final MyPycrunchConnector _connector;
    private JButton refreshToolWindowButton;
    private JButton hideToolWindowButton;
    private JLabel currentDate;
    private JLabel currentTime;
    private JLabel timeZone;
    private JPanel myToolWindowContent;
    private JList list1;
    private JButton runSelectedButton;

    public MyToolWindow(ToolWindow toolWindow) {
        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
        refreshToolWindowButton.addActionListener(e -> ui_will_mount());
        runSelectedButton.addActionListener(e -> run_selected());
        _connector = ServiceManager.getService(MyPycrunchConnector.class);

        this.ui_will_mount();
    }

    public void run_selected() {
        Object selectedValue = list1.getSelectedValue();
        if (selectedValue == null) {
            return;
        }

        PycrunchTestMetadata test_to_run = (PycrunchTestMetadata) selectedValue;

    }


    public void ui_will_mount() {
        // Get current date and time
        list1.setLayoutOrientation(JList.VERTICAL);
        String[] data = { "Chrome", "Firefox", "Internet Explorer", "Safari",
                "Opera", "Morrowind", "Oblivion", "NFS", "Half Life 2",

                "Windows", "Mac OS", "Ubuntu"
        };
        ArrayList<PycrunchTestMetadata> tests = _connector.GetTests();
        ArrayList<String> ss = new ArrayList<>();
        tests.forEach(__ -> ss.add(__.fqn));

        list1.setListData(tests.toArray());

    }

    public JPanel getContent() {
        return myToolWindowContent;
    }
}