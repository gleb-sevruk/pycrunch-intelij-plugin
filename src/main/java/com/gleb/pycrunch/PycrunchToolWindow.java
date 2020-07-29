package com.gleb.pycrunch;

import com.gleb.pycrunch.actions.ToggleTestPinnedState;
import com.gleb.pycrunch.activation.ActivationForm;
import com.gleb.pycrunch.shared.EngineMode;
import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.gleb.pycrunch.ui.PycrunchDefaultTestTree;
import com.gleb.pycrunch.ui.PycrunchTreeState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.*;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PycrunchToolWindow {
    private PycrunchWindowStateService _uiState;
    private EngineMode _engineMode;
    private PycrunchConnector _connector;
    private JPanel myToolWindowContent;
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
    private JSplitPane _splitPane;
    private Tree _testTree;
    private JButton _expandAllButton;
    private JButton _collapseAllButton;
    private JLabel _lblWatchdogText;
    private JPanel panelWatchdog;
    private JButton _btnTerminate;
    private JLabel label1;
    private Project _project;
    private MessageBus _bus;
    private String _selectedTestFqn;

    private ListSpeedSearch _listSpeedSearch;
    private TreeSpeedSearch _treeSpeedSearch;
    private PycrunchTreeState _treeState;
    private PycrunchConnectionState _connectionState = new PycrunchConnectionState();

    public PycrunchToolWindow(ToolWindow toolWindow, Project project, MessageBus bus, PycrunchConnector connector) {
        _bus = bus;
        _project = project;
        _connector = connector;
        _uiState = ServiceManager.getService(_project, PycrunchWindowStateService.class);
        _treeState = new PycrunchTreeState();
        top_toolbar.setVisible(false);
        _splitPane.setVisible(false);
        _engineMode = new EngineMode(_connector);

        attach_events();
        this.ui_will_mount();


        connect_to_message_bus();
        _connector.invalidateLicenseStateAndNotifyUI();

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
                ActivationForm wrap = new ActivationForm(_project);

                boolean pressed_ok = wrap.showAndGet();
                if (pressed_ok) {
                    System.out.println("Activation in another form");
                }
            }
        });
    }

    private JBPopupMenu create_engine_mode_popup() {
        String currentMode = _engineMode._mode;
        JBPopupMenu menu = new JBPopupMenu();
        JBRadioButton option_auto = new JBRadioButton("Run tests automatically");
        option_auto.setSelected(currentMode.equals(_engineMode.mode_run_all_automatically));
        option_auto.setMargin(JBUI.insets(10));
        option_auto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _engineMode.SetAutomaticMode();
            }
        });


        JBRadioButton option_manual = new JBRadioButton("Run all tests manually");
        option_manual.setSelected(currentMode.equals(_engineMode.mode_manual));
        option_manual.setMargin(JBUI.insets(10));
        option_manual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _engineMode.SetManualMode();
            }
        });

        JBRadioButton option_pinned_only = new JBRadioButton("Run pinned automatically, others manually");
        option_pinned_only.setSelected(currentMode.equals(_engineMode.mode_pinned_automatically));
        option_pinned_only.setMargin(JBUI.insets(10));
        option_pinned_only.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _engineMode.SetPinnedOnlyMode();
            }
        });

        JBCheckBox wrap_output_checkbox = new JBCheckBox("Wrap test output");
        wrap_output_checkbox.setSelected(_uiState._wrapOutput);
        wrap_output_checkbox.setMargin(JBUI.insets(10));
        wrap_output_checkbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _uiState._wrapOutput = !_uiState._wrapOutput;
                applyWordWrap();
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(option_auto);
        group.add(option_manual);
        group.add(option_pinned_only);

        menu.add(option_auto);
        menu.add(option_manual);
        menu.add(option_pinned_only);

        menu.add(wrap_output_checkbox);

        return menu;
    }

    private void applyWordWrap() {
        textArea1.setLineWrap(_uiState._wrapOutput);
    }

    @NotNull
    private MouseAdapter tree_mouse_click_listener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        Object lastSelectedPathComponent = _testTree.getLastSelectedPathComponent();
                        if ( lastSelectedPathComponent != null) {
                            DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) lastSelectedPathComponent;
                            Object userObject = selected_node.getUserObject();
                            if (userObject instanceof PycrunchTestMetadata) {
                                PycrunchTestMetadata selectedValue1 = (PycrunchTestMetadata) userObject;
                                new NavigateToTest().Go(selectedValue1, _connector);
                            }
                        }


                    }
                }
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                List<PycrunchTestMetadata> selectedValuesList = get_selected_tests_from_tree();

                if (selectedValuesList.size() <= 0) {
                    System.out.println("null is selected instead of tests; returning...");
                    return;
                }
                JBPopupMenu menu = create_test_list_popup(selectedValuesList);
                menu.show(_testTree, e.getPoint().x, e.getPoint().y);

            }
        };
    }

    @NotNull
    private List<PycrunchTestMetadata> get_selected_tests_from_tree() {
        TreePath[] paths = _testTree.getSelectionPaths();
        if (paths == null) {
            return new ArrayList<>();
        }

        List<PycrunchTestMetadata> selectedValuesList = new ArrayList<>();

        for (TreePath path : paths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            System.out.println("You've selected: " + node);
            if (node.getUserObject() instanceof PycrunchTestMetadata) {
                selectedValuesList.add((PycrunchTestMetadata) node.getUserObject());
            } else {
//                has children
                if (node.getChildCount() > 0) {
                    Collections.list(node.children()).forEach(__ -> {
                        Object userObject = ((DefaultMutableTreeNode) __).getUserObject();
                        if (userObject instanceof PycrunchTestMetadata) {
                            selectedValuesList.add((PycrunchTestMetadata) userObject);
                        }
                    });
                }
            }
        }
        return selectedValuesList;
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
        runSelectedButton.addActionListener(e -> run_selected());
        _btnTerminate.addActionListener(e -> terminate_run());
        _expandAllButton .addActionListener(e-> expandAll());
        _collapseAllButton .addActionListener(e-> collapseAll());
//        highlightFileButton.addActionListener(e -> update_all_highlighting());
    }

    private void collapseAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) _testTree.getModel().getRoot();
        if (root == null) {
            return;
        }
        Collections.list(root.children()).forEach(__ -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) __;
            _treeState.nodeWillCollapse((String) node.getUserObject());
            _testTree.collapsePath(new TreePath(node.getPath()));
        });
    }

    private void expandAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) _testTree.getModel().getRoot();
        if (root == null) {
            return;
        }
        Collections.list(root.children()).forEach(__ -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) __;
            _treeState.nodeWillExpand((String) node.getUserObject());
            _testTree.expandPath(new TreePath(node.getPath()));
        });
    }

    private void update_all_highlighting() {
        long start = System.nanoTime();
        ConcurrentHashMap<String, TestRunResult> results = _connector.get_results();
        if (results.size() <= 0) {
            return;
        }

        PycrunchHighlighterMarkersState connector = ServiceManager.getService(_project, PycrunchHighlighterMarkersState.class);

        HashSet<String> files_to_redraw = new HashSet<>();

        for (TestRunResult result: results.values()) {
            result.files_covered.values().forEach(__ -> {
                files_to_redraw.add(__.filename);

            });
        }

        files_to_redraw.forEach(__ -> {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(__);
            if (fileByPath != null) {
                Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(fileByPath);
                if (cachedDocument != null) {
                    connector.invalidate_markers(cachedDocument, _project);
                } else {
                    System.out.println("cached document is null " + __);
                }
            } else {
                System.out.println("!! updating highlighting -> fileByPath is null " + __);
            }
        });

        long elapsedTime = System.nanoTime() - start;
        long diffInMillis = elapsedTime/1000000;
        System.out.println("redraw markers elapsed: " + diffInMillis + "ms");
    }

    private void update_highlighting_in_single_file(PycrunchHighlighterMarkersState connector, VirtualFile fileByPath) {
        Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(fileByPath);
        if (cachedDocument != null) {
            connector.invalidate_markers(cachedDocument, _project);
        } else {
            System.out.println("cached document is null " + fileByPath.getPath());
        }
    }

    public void connect_to_message_bus() {
        connect_pycrunch_bus();
        connect_watchdog_bug();

        connect_intellij_events_bus();
    }

    private void connect_intellij_events_bus() {
        _bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                EventQueue.invokeLater(() -> {
                    PycrunchHighlighterMarkersState highlighterMarkersState = ServiceManager.getService(_project, PycrunchHighlighterMarkersState.class);
                    update_highlighting_in_single_file(highlighterMarkersState, file);
                });
            }

        });
    }
    private void setStatus(String text) {
        label_engine_status.setText(text);

    }

    private void connect_watchdog_bug() {
        _bus.connect().subscribe(PycrunchWatchdogBusNotifier.CHANGE_ACTION_TOPIC, new PycrunchWatchdogBusNotifier() {
            @Override
            public void watchdogBegin(int test_count) {
                System.out.println("watchdogBegin");
                System.out.println(test_count);
                String text;
                if (test_count == 1) {
                    text = "1 test queued...";
                } else {
                    text = test_count + " tests queued...";
                }
                _lblWatchdogText.setText(text);
                _btnTerminate.setEnabled(true);
                panelWatchdog.setVisible(true);
            }

            @Override
            public void watchdogEnd() {
                panelWatchdog.setVisible(false);
                _btnTerminate.setEnabled(false);
                System.out.println("watchdogEnd");
                System.out.println();
            }
        });
    }
    private void connect_pycrunch_bus() {
        _bus.connect().subscribe(PycrunchBusNotifier.CHANGE_ACTION_TOPIC, new PycrunchBusNotifier() {
            @Override
            public void beforeAction(String event) {
                textArea1.setText("Test output will be captured here");
                fill_test_list();
//                update_all_highlighting();

            }
            @Override
            public void engineDidConnect(String apiRoot) {
                _connectionState._apiRoot = apiRoot;
                setStatus(_connectionState.statusText());
            }
            @Override
            public void engineDidDisconnect(String context){
                setStatus("Lost connection to PyCrunch Engine");
            }
            @Override
            public void combinedCoverageDidUpdate(String context) {
                update_all_highlighting();
            }

            @Override
            public void licenceInvalid() {
                top_toolbar.setVisible(false);
                _splitPane.setVisible(false);
                label_engine_status.setVisible(false);
                activateButton.setVisible(true);
            }

            @Override
            public void did_select_test(PycrunchTestMetadata userObject) {
//                System.out.println("FAKE! did_select_test" );

            }

            @Override
            public void engineDidLoadMode(String new_mode){
                _engineMode.WillChangeTo(new_mode);
            }

            @Override
            public void engineWillTryToReconnect(String unused) {
                setStatus("Connection lost. Trying to reconnect to the engine...");
            }

            @Override
            public void engineDidLoadVersion(int version_major, int version_minor) {
                _connectionState.engineVersion(version_major, version_minor);

                setStatus(_connectionState.statusText());
            }

            @Override
            public void engineDidFailToReconnect(String dummy) {
                setStatus("Failed to reconnect. Please start the engine again.");
            }

            @Override
            public void licenceActivated() {
                top_toolbar.setVisible(true);
                _splitPane.setVisible(true);
                label_engine_status.setVisible(true);
                activateButton.setVisible(false);
            }
        });
    }
    private void terminate_run() {
        try {
            _connector.TerminateRun();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void run_selected() {
        List<PycrunchTestMetadata> selectedValue = get_selected_tests_from_tree();
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
        applyWordWrap();
        configure_test_tree();
        configure_split_pane();
        configure_watchdog_panel();
    }

    private void configure_watchdog_panel() {
        panelWatchdog.setVisible(false);
    }

    private void configure_split_pane() {
        _splitPane.setDividerLocation(_uiState._splitPanePosition);

        _splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        System.out.println(pce.toString());
                        _uiState._splitPanePosition = (int)pce.getNewValue();


                    }
                });
    }

    private void configure_test_tree() {
        _testTree.addTreeSelectionListener(e -> tree_selection_did_change(e));
        _testTree.addMouseListener(tree_mouse_click_listener());
        _treeSpeedSearch = new TreeSpeedSearch(_testTree);
        TreeWillExpandListener treeWillExpandListener = new TreeWillExpandListener() {
            public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent)
                    throws ExpandVetoException {

                TreePath path = treeExpansionEvent.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                //Print the name of the node if toString() was implemented
                String data = node.getUserObject().toString();
                System.out.println("WillCollapse: " + data);
                _treeState.nodeWillCollapse(data);
            }

            public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {

                TreePath path = treeExpansionEvent.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                //print the name of the node if toString was implemented
                String data = node.getUserObject().toString();
                _treeState.nodeWillExpand(data);

            }
        };

        _testTree.addTreeWillExpandListener(treeWillExpandListener);
        _testTree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree jTree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

                if (value instanceof DefaultMutableTreeNode) {

                    DefaultMutableTreeNode tree_node = (DefaultMutableTreeNode) value;
                    Object userObject = tree_node.getUserObject();
                    if (userObject instanceof PycrunchTestMetadata) {

                        ImageIcon icon = icon_from_state(((PycrunchTestMetadata) userObject).state);
                        setIcon(icon);
                    } else {
                        // module node
                        String moduleStatus = _connector.GetModuleStatus((String) userObject);
                        ImageIcon icon = icon_from_state(moduleStatus);
                        setIcon(icon);
                    }
                }

                append(value.toString());
                SpeedSearchUtil.applySpeedSearchHighlighting(_testTree, this, false, selected);
            }


            @NotNull
            private ImageIcon icon_from_state(String state) {
                URL resource = getClass().getResource("/list_pending.png");
                if (state != null) {
                    if (state.equals("success")) {
                        resource = getClass().getResource("/list_success.png");
                    }
                    if (state.equals("failed")) {
                        resource = getClass().getResource("/list_failed.png");
                    }
                    if (state.equals("queued")) {
                        resource = getClass().getResource("/list_queued.png");
                    }
                }

                return new ImageIcon(resource);
            }
        });
    }
    // top bar icons - toggle passed/failed/pinned
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
                _uiState._showPassedTests = state;
                fill_test_list();
            }
        });
        togglePassedTests.setSelected(_uiState._showPassedTests);

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
                _uiState._showFailedTests = state;
                fill_test_list();
            }
        });
        toggleFailedTests.setSelected(_uiState._showFailedTests);

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
                _uiState._showPendingTests = state;

                fill_test_list();

            }
        });
        togglePendingTests.setSelected(_uiState._showPendingTests);

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
                _uiState._showPinnedTests = state;

                fill_test_list();

            }
        });
        togglePinnedTests.setSelected(_uiState._showPinnedTests);

        _expandAllButton.setIcon(AllIcons.Actions.Expandall);
        _collapseAllButton.setIcon(AllIcons.Actions.Collapseall);

    }

    @NotNull
    private MetalToggleButtonUI get_metal_toggle_ui() {
        return new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return JBColor.background().darker();
            }
        };
    }

    private void fill_test_list() {
        preserveTreeSelection();
        Collection<PycrunchTestMetadata> tests = _connector.GetTestsSorted();
        ArrayList<PycrunchTestMetadata> list = new ArrayList<>();
        for (PycrunchTestMetadata test: tests) {
            if (pass_list_filter(test)) {
                list.add(test);
            }
        }
        PycrunchDefaultTestTree tree = new PycrunchDefaultTestTree(list);
        DefaultMutableTreeNode root = tree.getRoot();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        _testTree.setModel(treeModel);

//        _testTree.setRootVisible(false);
        restore_tree_expand_state(root);
        restoreTreeSelection();

    }


    private void restore_tree_expand_state(DefaultMutableTreeNode root) {
        Enumeration<TreeNode> enumeration = root.breadthFirstEnumeration();
        while(enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            if (_treeState.isNodeExpanded(node.getUserObject().toString())) {
                _testTree.expandPath(new TreePath(node.getPath()));
            }
        }
    }

    private boolean pass_list_filter(PycrunchTestMetadata test) {
        if (_uiState._showPinnedTests && test.pinned) {
            return true;
        }

        if (_uiState._showPassedTests && test.state.equals("success")){
            return true;
        }
        if (_uiState._showFailedTests && test.state.equals("failed")) {
            return true;
        }

        if (_uiState._showPendingTests && (test.state.equals("pending"))) {
            return true;
        }

        if (test.state.equals("queued")) {
            return true;
        }
        return false;
    }

    private void preserveTreeSelection() {
        Object selectedValue = _testTree.getLastSelectedPathComponent();
        if (selectedValue != null) {
            DefaultMutableTreeNode t = (DefaultMutableTreeNode) selectedValue;
            Object userObject = t.getUserObject();
            if (userObject instanceof PycrunchTestMetadata) {
                _selectedTestFqn = ((PycrunchTestMetadata) userObject).fqn;
            } else {
                _selectedTestFqn = (String) userObject;
            }
        }
    }

    private void restoreTreeSelection() {
        Object root = _testTree.getModel().getRoot();
        if (root == null) {
            return;
        }
        if (_selectedTestFqn == null) {
            return;
        }
        Enumeration<TreeNode> enumeration = ((DefaultMutableTreeNode)root).breadthFirstEnumeration();
        while(enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();

            Object userObject = node.getUserObject();
            if (!(userObject instanceof PycrunchTestMetadata)) {
                continue;
            }

            PycrunchTestMetadata testMetadata = (PycrunchTestMetadata) userObject;
            if (_selectedTestFqn.equals(testMetadata.fqn)) {
                _testTree.setSelectionPath(new TreePath(node.getPath()));
                break;
            }
        }
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private void tree_selection_did_change(TreeSelectionEvent e) {
        Object lastSelectedPathComponent = _testTree.getLastSelectedPathComponent();
        if ( lastSelectedPathComponent != null) {
            DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) lastSelectedPathComponent;
            Object userObject = selected_node.getUserObject();
            if (userObject instanceof PycrunchTestMetadata) {
                PycrunchTestMetadata selectedValue1 = (PycrunchTestMetadata) userObject;
                textArea1.setText(_connector.GetCapturedOutput(selectedValue1.fqn));
//                System.out.println("invoking did_select_test" );
                ((PycrunchBusNotifier)
                            this._bus.syncPublisher(PycrunchBusNotifier.CHANGE_ACTION_TOPIC))
                            .did_select_test(selectedValue1);
            }
        }

    }

    private void createUIComponents() {

    }

}