package com.gleb.pycrunch;

import com.gleb.pycrunch.actions.ToggleTestPinnedState;
import com.gleb.pycrunch.activation.SampleDialogWrapper;
import com.gleb.pycrunch.shared.EngineMode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class PycrunchToolWindow {
    private EngineMode _engineMode;
    private MyPycrunchConnector _connector;
    private JButton refreshToolWindowButton;
    private JLabel currentDate;
    private JLabel currentTime;
    private JLabel timeZone;
    private JPanel myToolWindowContent;
    private com.intellij.ui.components.JBList list1;
    private JButton runSelectedButton;
    private JTextArea textArea1;
    private JLabel label_engine_status;
    private JToolBar top_toolbar;
    private JToggleButton togglePassedTests;
    private JToggleButton toggleFailedTests;
    private JToggleButton togglePendingTests;
    private JButton settingsButton;
    private JToggleButton togglePinnedTests;
    private JButton activateButton;
    private JLabel label1;
    private Project _project;
    private MessageBus _bus;
    private String _selectedTestFqn;
    private boolean _showPassedTests;
    private boolean _showFailedTests;
    private boolean _showPendingTests;
    private boolean _showPinnedTests;

    public PycrunchToolWindow(ToolWindow toolWindow, Project project, MessageBus bus, MyPycrunchConnector connector) {
        _bus = bus;
        _project = project;
        _connector = connector;

        attach_events();
        this.ui_will_mount();
        list1.setLayoutOrientation(JList.VERTICAL);
        _engineMode = new EngineMode(_connector);


        list1.addListSelectionListener(e -> selection_did_change(e));
        connect_to_message_bus();


        list1.addMouseListener(list_mouse_click_listener());
//        top_toolbar.setRollover(false);
//        togglePassedTests.setBackground(JBColor.background());

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        settingsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JBPopupMenu menu = create_engine_mode_popup();
                menu.show(settingsButton, e.getPoint().x, e.getPoint().y);
            }
        });
        activateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SampleDialogWrapper wrap = new SampleDialogWrapper();

                boolean pressed_ok = wrap.showAndGet();
                if (pressed_ok) {
                    String email = wrap._emailTextBox.getText();
                    String password = wrap._passwordTextBox.getText();
                    System.out.println("OK!!, email: " + email);
                    System.out.println("OK!!, pass: " + password);
                    _connector.tryActivate(email, password);
                }
            }
        });
    }

    private JBPopupMenu create_engine_mode_popup() {

        JBPopupMenu menu = new JBPopupMenu();
        JBRadioButton option1 = new JBRadioButton("Run tests automatically");
        option1.setSelected(true);
        option1.setMargin(JBUI.insets(10));
        option1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _engineMode.SetAutomaticMode();
            }
        });


        JBRadioButton option2 = new JBRadioButton("Run all tests manually");
        option2.setMargin(JBUI.insets(10));
        option2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _engineMode.SetManualMode();
            }
        });

        JBRadioButton option3 = new JBRadioButton("Run pinned automatically, others manually");
        option3.setMargin(JBUI.insets(10));
        option3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _engineMode.SetPinnedOnlyMode();
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(option1);
        group.add(option2);
        group.add(option3);

        menu.add(option1);
        menu.add(option2);
        menu.add(option3);

        return menu;
    }

    @NotNull
    private MouseAdapter list_mouse_click_listener() {
        return new MouseAdapter() {
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
                JBPopupMenu menu = create_test_list_popup(selectedValuesList);
                menu.show(list1, e.getPoint().x, e.getPoint().y);

            }
        };
    }

    @NotNull
    private JBPopupMenu create_test_list_popup(List<PycrunchTestMetadata> selectedValuesList) {
        JBPopupMenu menu = new JBPopupMenu();
        JBMenuItem navigateToTest = new JBMenuItem("Navigate to test");
        navigateToTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new NavigateToTest().Go(selectedValuesList.get(0), _connector);
            }
        });


        String ending = "";
        if (selectedValuesList.size() > 1) {
            ending = "s";
        }
        JBMenuItem pinTest = new JBMenuItem("Pin test" + ending);
        pinTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ToggleTestPinnedState().Run(selectedValuesList, _connector, true);
            }
        });
        JBMenuItem unpinTest = new JBMenuItem("Unpin test" + ending);
        unpinTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ToggleTestPinnedState().Run(selectedValuesList, _connector, false);
            }
        });

        menu.add(navigateToTest);
        menu.add(pinTest);
        menu.add(unpinTest);

        return menu;
    }

    private void attach_events() {
        refreshToolWindowButton.addActionListener(e -> ui_will_mount());
        runSelectedButton.addActionListener(e -> run_selected());
//        highlightFileButton.addActionListener(e -> update_all_highlighting());
    }

    private void update_all_highlighting() {
        long start = System.nanoTime();
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
//                System.out.println("cached document is null " + __);
            }
        });

        long elapsedTime = System.nanoTime() - start;
        long diffInMillis = elapsedTime/1000000;
        System.out.println("redraw markers elapsed: " + diffInMillis + "ms");
    }

    public void connect_to_message_bus() {
        _bus.connect().subscribe(PycrunchBusNotifier.CHANGE_ACTION_TOPIC, new PycrunchBusNotifier() {
            @Override
            public void beforeAction(String event) {
                textArea1.setText(_connector.GetCapturedOutput("todo"));
                fill_test_list();
//                update_all_highlighting();

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
        configure_buttons();

    }

    private void configure_buttons() {
        togglePassedTests.setFocusable(false);
        togglePassedTests.setUI(get_metal_toggle_ui());
        togglePassedTests.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = togglePassedTests.isSelected();
                if (state) {

                    System.out.println("Selected passed tests!");
                } else {
                    System.out.println("Deselected passed tests");
                }
                _showPassedTests = state;
                fill_test_list();
            }
        });

        toggleFailedTests.setFocusable(false);
        toggleFailedTests.setUI(get_metal_toggle_ui());
        toggleFailedTests.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = toggleFailedTests.isSelected();
                if (state) {
                    System.out.println("Selected failed tests!");
                } else {
                    System.out.println("Deselected failed tests");
                }
                _showFailedTests = state;
                fill_test_list();
            }
        });

        togglePendingTests.setFocusable(false);
        togglePendingTests.setUI(get_metal_toggle_ui());
        togglePendingTests.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = togglePendingTests.isSelected();
                if (state) {
                    System.out.println("Selected pending tests!");
                } else {
                    System.out.println("Deselected pending tests");
                }
                _showPendingTests = state;

                fill_test_list();

            }
        });

        togglePinnedTests.setFocusable(false);
        togglePinnedTests.setUI(get_metal_toggle_ui());
        togglePinnedTests.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = togglePinnedTests.isSelected();
                if (state) {
                    System.out.println("Selected pinned tests!");
                } else {
                    System.out.println("Deselected pinned tests");
                }
                _showPinnedTests = state;

                fill_test_list();

            }
        });
    }

    @NotNull
    private MetalToggleButtonUI get_metal_toggle_ui() {
        return new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return JBColor.GREEN.brighter();
            }
        };
    }

    private void fill_test_list() {
        preserveListSelection();
        Collection<PycrunchTestMetadata> tests = _connector.GetTests();
        ArrayList<PycrunchTestMetadata> list = new ArrayList<>();
        for (PycrunchTestMetadata test: tests) {
            if (pass_list_filter(test)) {
                list.add(test);
            }
        }
        list1.setListData(list.toArray());
        restoreSelectedTest(tests);
    }

    private boolean pass_list_filter(PycrunchTestMetadata test) {
        if (_showPinnedTests && test.pinned) {
            return true;
        }

        if (_showPassedTests && test.state.equals("success")){
            return true;
        }
        if (_showFailedTests && test.state.equals("failed")) {
            return true;
        }

        if (_showPendingTests && (test.state.equals("pending") || test.state.equals("queued"))) {
            return true;
        }

        return false;
    }

    private void restoreSelectedTest(Collection<PycrunchTestMetadata> listData) {
        if (_selectedTestFqn != null) {
            for (PycrunchTestMetadata test: listData) {
                if (test.fqn.equals(_selectedTestFqn))  {
                    list1.setSelectedValue(test, true);
                    break;
                }
            }
        }
    }

    private void preserveListSelection() {
        Object selectedValue = list1.getSelectedValue();
        if (selectedValue != null) {
            _selectedTestFqn = ((PycrunchTestMetadata)selectedValue).fqn;
        }
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

    @NotNull
    private MouseAdapter run_test_mouse_click_listener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                run_selected();
            }
        };
    }

    private void createUIComponents() {

//        Presentation presentation = new Presentation("hui");
//        presentation.setIcon(new ImageIcon(getClass().getResource("/run.png")));
//        actionButton1 = new ActionButton(new Kurlik(this), presentation, "PyCrunch", new Dimension(32,32));

//        top_toolbar.add("test", actionButton1);


    }
    class Kurlik extends AnAction {
        private PycrunchToolWindow _window;

        Kurlik(PycrunchToolWindow window) {
            _window = window;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            _window.run_selected();

        }
    }

}