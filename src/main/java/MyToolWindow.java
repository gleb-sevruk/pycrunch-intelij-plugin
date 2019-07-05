import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.messages.MessageBus;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.List;

public class MyToolWindow {
    private MyPycrunchConnector _connector;
    private JButton refreshToolWindowButton;
    private JButton hideToolWindowButton;
    private JLabel currentDate;
    private JLabel currentTime;
    private JLabel timeZone;
    private JPanel myToolWindowContent;
    private JList list1;
    private JButton runSelectedButton;
    private JTextArea textArea1;
    private JButton highlightFileButton;
    private JLabel label1;
    private Project _project;
    private MessageBus _bus;

    public MyToolWindow(ToolWindow toolWindow, Project project, MessageBus bus, MyPycrunchConnector connector) {
        _bus = bus;
        _project = project;
        _connector = connector;
        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
        attach_events();
        this.ui_will_mount();
        list1.setLayoutOrientation(JList.VERTICAL);



        list1.addListSelectionListener(e -> selection_did_change(e));
        connect_to_message_bus();


    }

    private void attach_events() {
        refreshToolWindowButton.addActionListener(e -> ui_will_mount());
        runSelectedButton.addActionListener(e -> run_selected());
        highlightFileButton.addActionListener(e -> update_all_highlighting());
    }

    private void update_all_highlighting() {
        TestRunResult result = _connector.get_result();
        if (result == null) {
            return;
        }

        PycrunchHighlighterMarkersState connector = ServiceManager.getService(PycrunchHighlighterMarkersState.class);


        result.files_covered.values().forEach(__ -> {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(__.filename);
            Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(fileByPath);
            if (cachedDocument != null) {
                connector.invalidate_markers(cachedDocument, _project);
            } else {
                System.out.println("cached document is null");
            }


        });
    }

    public void connect_to_message_bus() {
        _bus.connect().subscribe(ChangeActionNotifier.CHANGE_ACTION_TOPIC, new ChangeActionNotifier() {
            @Override
            public void beforeAction(String context) {
                textArea1.setText(_connector.GetCapturedOutput(context));
                fill_test_list();
                update_all_highlighting();

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