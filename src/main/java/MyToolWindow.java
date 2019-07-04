import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.messages.MessageBus;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.List;

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
    private JTextArea textArea1;
    private JButton initMessageBusButton;
    private JLabel label1;

    public MyToolWindow(ToolWindow toolWindow) {
        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
        refreshToolWindowButton.addActionListener(e -> ui_will_mount());
        runSelectedButton.addActionListener(e -> run_selected());
        initMessageBusButton.addActionListener(e -> initMessageBus());
        list1.setLayoutOrientation(JList.VERTICAL);
        _connector = ServiceManager.getService(MyPycrunchConnector.class);
        this.ui_will_mount();



        list1.addListSelectionListener(e -> selection_did_change(e));


    }

    public void initMessageBus() {
        MessageBus bus = _connector.GetMessageBus();
        bus.connect().subscribe(ChangeActionNotifier.CHANGE_ACTION_TOPIC, new ChangeActionNotifier() {
            @Override
            public void beforeAction(String context) {
                textArea1.setText(_connector.GetCapturedOutput(context));
                fill_test_list();

            }
            @Override
            public void afterAction(String context) {
                // Process 'after action' event.
            }
        });
    }

    public void run_selected() {
        List<PycrunchTestMetadata> selectedValue = list1.getSelectedValuesList();
        if (selectedValue == null) {
            return;
        }

        try {
            _connector.RunTests(selectedValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void ui_will_mount() {
        // Get current date and time
        fill_test_list();

    }

    private void fill_test_list() {
        ArrayList<PycrunchTestMetadata> tests = _connector.GetTests();
        ArrayList<String> ss = new ArrayList<>();
        tests.forEach(__ -> ss.add(__.fqn));

        list1.setListData(tests.toArray());
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private void selection_did_change(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            Object selectedValue = list1.getSelectedValue();
            PycrunchTestMetadata selectedValue1 = null;
            if (selectedValue instanceof PycrunchTestMetadata) {
                selectedValue1 = (PycrunchTestMetadata) selectedValue;
                textArea1.setText(_connector.GetCapturedOutput(selectedValue1.fqn));
            }
        }
    }
}