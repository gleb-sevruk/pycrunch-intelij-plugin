package com.gleb.pycrunch;

import com.gleb.pycrunch.actions.ToggleTestPinnedState;
import com.gleb.pycrunch.exceptionPreview.CapturedException;
import com.gleb.pycrunch.messaging.PycrunchBusNotifier;
import com.gleb.pycrunch.messaging.PycrunchToolbarBus;
import com.gleb.pycrunch.messaging.PycrunchWatchdogBusNotifier;
import com.gleb.pycrunch.shared.EngineMode;
import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.gleb.pycrunch.ui.PycrunchDefaultTestTree;
import com.gleb.pycrunch.ui.PycrunchTreeState;
import com.intellij.ide.ActivityTracker;
import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonPainter;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.*;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBus;
import icons.PycrunchIcons;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PycrunchToolWindow {
    private PycrunchWindowStateService _uiState;
    private EngineMode _engineMode;
    private PycrunchConnector _connector;
    private JPanel myToolWindowContent;
    private JTextArea textArea1;
    private JLabel label_engine_status;
    private JButton activateButton;
    private Tree _testTree;
    private JLabel _lblWatchdogText;
    private JPanel panelWatchdog;
    private JPanel _pcPanelToolbar;
    private JScrollPane _surfaceTestList;
    private JScrollPane _surfaceTestOutput;
    private JBSplitter _splitterJb;


    private JLabel label1;
    private Project _project;
    private MessageBus _bus;
    private String _selectedTestFqn;

    private ListSpeedSearch _listSpeedSearch;
    private TreeSpeedSearch _treeSpeedSearch;
    private PycrunchTreeState _treeState;
    private PycrunchConnectionState _connectionState = new PycrunchConnectionState();
    private boolean _needs_redraw_on_next_editor_change = false;

    public PycrunchToolWindow(ToolWindow toolWindow, Project project, MessageBus bus, PycrunchConnector connector) {
        _bus = bus;
        _project = project;
        _connector = connector;
        _uiState = _project.getService(PycrunchWindowStateService.class);
        _engineMode = _project.getService(EngineMode.class);
        _treeState = new PycrunchTreeState();

        this.ui_will_mount();


        connect_to_message_bus();
        _connector.invalidateLicenseStateAndNotifyUI();
    }


    private void _applyWordWrap() {
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
    private JBPopupMenu create_test_list_popup(List<PycrunchTestMetadata> selectedValuesList) {
        JBPopupMenu menu = new JBPopupMenu();
        JBMenuItem navigateToTest = new JBMenuItem("Navigate to test");
        navigateToTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new NavigateToTest().Go(selectedValuesList.get(0), _connector);
            }
        });

        CapturedException exceptionForCurrentTestUnderCursor = _connector.getExceptionFor(selectedValuesList.get(0).fqn);
        JBMenuItem navigateToException = null;
        if (exceptionForCurrentTestUnderCursor != null) {
            navigateToException = new JBMenuItem("Navigate to exception");
            navigateToException.addActionListener(e -> {
                        // Optimization: redraw markers on newly opened file only if this flag is set
                        _needs_redraw_on_next_editor_change = true;
                        new NavigateToException().Go(selectedValuesList.get(0), exceptionForCurrentTestUnderCursor, _connector);
                    }
            );

        }

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
        if (exceptionForCurrentTestUnderCursor != null) {
            menu.add(navigateToException);
        }
        menu.add(pinTest);
        menu.add(unpinTest);

        return menu;
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

    private void _collapseAll() {
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

    private void _expandAll() {
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
        ConcurrentHashMap<String, TestRunResult> results = _connector.get_test_run_results();
        if (results.size() <= 0) {
            return;
        }

        PycrunchHighlighterMarkersState markersState = _project.getService(PycrunchHighlighterMarkersState.class);

        HashSet<String> files_to_redraw = new HashSet<>();

        for (TestRunResult result: results.values()) {
            result.files_covered.values().forEach(__ -> {
                files_to_redraw.add(__.filename);

            });
        }
//        Add previously highlighted files
        files_to_redraw.addAll(markersState.get_all_files_with_markers());

        files_to_redraw.forEach(__ -> {
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(__);
            if (fileByPath != null) {
                Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(fileByPath);
//                Document cachedDocument = FileDocumentManager.getInstance().getDocument(fileByPath);
                if (cachedDocument != null) {
//                    System.out.println("Invalidating markers for " + __);
                    markersState.invalidate_markers(cachedDocument, _project);
                } else {
//                    System.out.println("cached document is null " + __);
                }
            } else {
//                System.out.println("!! updating highlighting -> fileByPath is null " + __);
            }
        });

        long elapsedTime = System.nanoTime() - start;
        long diffInMillis = elapsedTime/1000000;
//        System.out.println("redraw markers elapsed: " + diffInMillis + "ms");
    }

    private void update_highlighting_in_single_file(PycrunchHighlighterMarkersState connector, VirtualFile fileByPath) {
//        System.out.println("update_highlighting_in_single_file " + fileByPath.getPath());
        Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(fileByPath);
        if (cachedDocument != null) {
            connector.invalidate_markers(cachedDocument, _project);

        } else {
//            System.out.println("update_highlighting_in_single_file cached document is null " + fileByPath.getPath());
        }
    }

    public void connect_to_message_bus() {
        connect_pycrunch_bus();
        connect_watchdog_bus();
        connect_toolbar_bus();

        connect_intellij_events_bus();
    }

    private void connect_toolbar_bus() {
        _bus.connect().subscribe(PycrunchToolbarBus.CHANGE_ACTION_TOPIC, new PycrunchToolbarBus() {
            @Override
            public void runSelectedTests() {
                run_selected();
            }

            @Override
            public void debugSelectedTests() {
                debug_selected();
            }

            @Override
            public void terminateTestRun() {
                terminate_run();
            }

            @Override
            public void refillTestList() {
                fill_test_list();
            }

            @Override
            public void collapseAll() {
                _collapseAll();
            }

            @Override
            public void expandAll() {
                _expandAll();
            }

            @Override
            public void applyWordWrap() {
                _applyWordWrap();
            }
        });
    }

    private void connect_intellij_events_bus() {
        _bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                update_highlighting_thread_safe(file);
            }
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                if (!_needs_redraw_on_next_editor_change) {
                    return;
                }
                VirtualFile newFile = event.getNewFile();
                if (newFile != null) {
                    update_highlighting_thread_safe(newFile);
                    _needs_redraw_on_next_editor_change = false;
                }
            }
        });
    }

    private void update_highlighting_thread_safe(@NotNull VirtualFile file) {
        EventQueue.invokeLater(() -> {
            if(_bus.isDisposed()) {
                return;
            }

            PycrunchHighlighterMarkersState highlighterMarkersState = _project.getService(PycrunchHighlighterMarkersState.class);
            update_highlighting_in_single_file(highlighterMarkersState, file);
        });
    }

    private void setStatus(String text) {
        label_engine_status.setText(text);

    }

    private void connect_watchdog_bus() {
        _bus.connect().subscribe(PycrunchWatchdogBusNotifier.CHANGE_ACTION_TOPIC, new PycrunchWatchdogBusNotifier() {
            @Override
            public void watchdogBegin(int test_count) {
                String text;
                if (test_count == 1) {
                    text = "1 test queued...";
                } else {
                    text = test_count + " tests queued...";
                }
                _lblWatchdogText.setText(text);
                panelWatchdog.setVisible(true);
                ActivityTracker.getInstance().inc();
            }

            @Override
            public void watchdogEnd() {
                panelWatchdog.setVisible(false);
                ActivityTracker.getInstance().inc();
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
                setStatus("Lost connection to PyCrunch Engine. Reconnecting...");
            }
            @Override
            public void combinedCoverageDidUpdate(String context) {
                update_all_highlighting();
            }

            @Override
            public void licenceInvalid() {
                label_engine_status.setVisible(false);
                activateButton.setVisible(true);
            }

            @Override
            public void did_select_test(PycrunchTestMetadata userObject) {
            }

            @Override
            public void engineDidLoadMode(String new_mode){
                _engineMode.WillChangeTo(new_mode);
            }

            @Override
            public void engineDidLoadVersion(int version_major, int version_minor, int version_patch) {
                _connectionState.engineVersion(version_major, version_minor, version_patch);

                setStatus(_connectionState.statusText());
            }

            @Override
            public void engineDidFailToReconnect(ConnectionState cs) {
                if (cs.current_retry < cs.max_retries) {
                    setStatus("Connection lost. Trying to reconnect to the engine...");
                } else {
                    setStatus("Failed to reconnect. Please start the engine again.");
                }
            }

            @Override
            public void licenceActivated() {
                label_engine_status.setVisible(true);
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

    public void debug_selected() {

        List<PycrunchTestMetadata> selectedValue = get_selected_tests_from_tree();
        if (selectedValue == null || selectedValue.size() <= 0) {
            return;
        }

        try {
            _connector.DebugTests(selectedValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void ui_will_mount() {

        fill_test_list();
        build_action_toolbars();
        _applyWordWrap();
        configure_test_tree();
        configure_watchdog_panel();
    }

    private void build_action_toolbars() {
        DefaultActionGroup toolbarGroup = new DefaultActionGroup();
        ActionManager actionManager = ActionManager.getInstance();
        String loc = "Pycrunch_Right_Toolbar";
        ActionToolbar toolbar =
                actionManager.createActionToolbar(
                        loc, toolbarGroup, true);
        toolbarGroup.add(
                actionManager.getAction("PyChrunch.RunSelectedTests")
        );
        toolbarGroup.addSeparator();

        toolbarGroup.add(
                actionManager.getAction("PyChrunch.DebugSelectedTests")
        );
        toolbarGroup.addSeparator();


        toolbarGroup.add(
                actionManager.getAction("PyChrunch.TerminateTestRun")
        );

        toolbarGroup.addSeparator();

        ArrayList<String> actions = new ArrayList<String>();
        actions.add("PyChrunch.TogglePassedTests");
        actions.add("PyChrunch.ToggleFailedTests");
        actions.add("PyChrunch.TogglePendingTests");
        actions.add("PyChrunch.TogglePinnedTests");
        for (String __ : actions) {
            toolbarGroup.add(actionManager.getAction(__));
        }
        toolbarGroup.addSeparator();

        toolbarGroup.add(actionManager.getAction("PyChrunch.ExpandAllTests"));
        toolbarGroup.add(actionManager.getAction("PyChrunch.CollapseAllTests"));



        String ROW_CONTEXT_MENU_ID = "PyChrunch.SettingsPopup";
        ActionGroup rowContextMenu = (ActionGroup) actionManager.getAction(ROW_CONTEXT_MENU_ID);
        ActionPopupMenu popupMenu = actionManager.createActionPopupMenu(ROW_CONTEXT_MENU_ID, rowContextMenu);
        toolbarGroup.addAction(popupMenu.getActionGroup());
        toolbarGroup.addSeparator();
        toolbarGroup.add(
                actionManager.getAction("PyChrunch.RunEngine")
        );

        toolbar.setTargetComponent(_pcPanelToolbar);
        _pcPanelToolbar.add(toolbar.getComponent());
    }


    private void fix_button_press_effect(JButton buttonToFix) {
//        This fixed buttons inability to be in pressed state.
        buttonToFix.setBorder(new DarculaButtonPainter());
        buttonToFix.setUI(new BasicButtonUI());
        buttonToFix.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ButtonModel model = (ButtonModel) e.getSource();
                if (model.isRollover()) {
                    buttonToFix.setBorderPainted(false);
                } else if (model.isPressed()) {
                    buttonToFix.setBorderPainted(true);
                } else {
                    buttonToFix.setBorderPainted(false);
                }
            }
        });
    }

    private void configure_watchdog_panel() {
        panelWatchdog.setVisible(false);
    }

    private void configure_test_tree() {
        _surfaceTestList.setBorder(BorderFactory.createEmptyBorder());

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
//                System.out.println("WillCollapse: " + data);
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

                        Icon icon = icon_from_state(((PycrunchTestMetadata) userObject).state);
                        setIcon(icon);
                    } else {
                        // module node
                        String moduleStatus = _connector.GetModuleStatus((String) userObject);
                        Icon icon = icon_from_state(moduleStatus);
                        setIcon(icon);
                    }
                }

                append(value.toString());
                SpeedSearchUtil.applySpeedSearchHighlighting(_testTree, this, false, selected);
            }


            @NotNull
            private Icon icon_from_state(String state) {
                Icon resource = PycrunchIcons.TEST_LIST_PENDING;
                if (state != null) {
                    if (state.equals("success")) {
                        resource = PycrunchIcons.TEST_LIST_SUCCESS;
                    }
                    if (state.equals("failed")) {
                        resource = PycrunchIcons.TEST_LIST_FAILED;
                    }
                    if (state.equals("queued")) {
                        resource = PycrunchIcons.TEST_LIST_QUEUED;
                    }
                }

                return resource;
            }
        });
    }
    // top bar icons - toggle passed/failed/pinned

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