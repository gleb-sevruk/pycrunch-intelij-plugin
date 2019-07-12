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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PycrunchToolWindow {
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
    private JLabel label_engine_status;
    private JLabel label1;
    private Project _project;
    private MessageBus _bus;

    public PycrunchToolWindow(ToolWindow toolWindow, Project project, MessageBus bus, MyPycrunchConnector connector) {
        _bus = bus;
        _project = project;
        _connector = connector;
        attach_events();
        this.ui_will_mount();
        list1.setLayoutOrientation(JList.VERTICAL);



        list1.addListSelectionListener(e -> selection_did_change(e));
        connect_to_message_bus();


        list1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                List<PycrunchTestMetadata> selectedValuesList = list1.getSelectedValuesList();
                if (selectedValuesList == null || selectedValuesList.size() <= 0) {
                    System.out.println("null is selected instead of tests; returning...");
                    return;
                }
                JPopupMenu menu = new JPopupMenu();
                JMenuItem itemRemove = new JMenuItem("Navigate to test");
                itemRemove.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        new NavigateToTest().Go(selectedValuesList.get(0), _connector);
                    }
                });
                menu.add(itemRemove);
                menu.show(list1, e.getPoint().x, e.getPoint().y);

            }
        });
    }

    private void attach_events() {
        refreshToolWindowButton.addActionListener(e -> ui_will_mount());
        runSelectedButton.addActionListener(e -> run_selected());
//        highlightFileButton.addActionListener(e -> update_all_highlighting());
    }

    private void update_all_highlighting() {
        HashMap<String, TestRunResult> results = _connector.get_results();
        if (results.size() <= 0) {
            return;
        }

        PycrunchHighlighterMarkersState connector = ServiceManager.getService(PycrunchHighlighterMarkersState.class);

        HashSet<String> files_to_redraw = new HashSet<>();

        for (TestRunResult result: results.values()) {
            result.files_covered.values().forEach(__ -> {
                files_to_redraw.add(__.filename);

            });
        }

        files_to_redraw.forEach(__ -> {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(__);
            Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(fileByPath);
            if (cachedDocument != null) {
                connector.invalidate_markers(cachedDocument, _project);
            } else {
                System.out.println("cached document is null " + __);
            }
        });


    }

    public void connect_to_message_bus() {
        _bus.connect().subscribe(PycrunchBusNotifier.CHANGE_ACTION_TOPIC, new PycrunchBusNotifier() {
            @Override
            public void beforeAction(String event) {
                textArea1.setText(_connector.GetCapturedOutput("todo"));
                fill_test_list();

            }
            @Override
            public void engineDidConnect(String apiRoot) {
                label_engine_status.setText("Connected to " + apiRoot);
            }
            @Override
            public void engineDidDisconnect(String context){
                label_engine_status.setText("Lost connection to PyCrunch Engine");
            }
            @Override
            public void combinedCoverageDidUpdate(String context) {
                update_all_highlighting();
            }

        });
    }

    public void run_selected() {
        List<PycrunchTestMetadata> selectedValue = list1.getSelectedValuesList();
        if (selectedValue == null || selectedValue.size() <= 0) {
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