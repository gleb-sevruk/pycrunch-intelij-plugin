package com.gleb.pycrunch.timeline;

import com.gleb.pycrunch.PycrunchBusNotifier;
import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.PycrunchTestMetadata;
import com.gleb.pycrunch.shared.PycrunchVariablesWindowStateService;
import com.gleb.pycrunch.shared.PycrunchWindowStateService;
import com.intellij.icons.AllIcons;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.util.messages.MessageBus;
import org.json.JSONArray;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class PyCrunchVariablesWindow {
    private final ToolWindow _toolWindow;
    private final Project _project;
    private final MessageBus _bus;
    private final PycrunchConnector _connector;
    private final PycrunchVariablesWindowStateService _uiState;
    private JPanel _mainPanel;
    private JLabel _label_selected_test;
    private JBList _variablesList;
    private JTextArea _variableValueText;
    private JLabel _labelEventTime;
    private JSplitPane _splitPane;
    private PycrunchVariablesState _currentState;
    private PycrunchTestMetadata _selectedTest;
    private PyCrunchVariableSnapshot _selectedVariable;
    private ListSpeedSearch _listSpeedSearch;

    public PyCrunchVariablesWindow(ToolWindow toolWindow, Project project, MessageBus bus, PycrunchConnector connector) {

        _toolWindow = toolWindow;
        _project = project;
        _bus = bus;
        _connector = connector;
        _uiState = ServiceManager.getService(_project, PycrunchVariablesWindowStateService.class);

        configure_split_pane();
        connect_pycrunch_bus();
        ui_will_mount();
    }


    private void ui_will_mount() {
        _variablesList.addListSelectionListener(e -> selected_variable_will_change(e));
        _variablesList.addMouseListener(list_mouse_click_listener());
        _listSpeedSearch = new ListSpeedSearch(_variablesList);
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
    private MouseAdapter list_mouse_click_listener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PyCrunchVariableSnapshot selectedVariable = _selectedVariable;
                if (e.getClickCount() == 2) {
//                    System.out.println("Mouse clicked " + e);
                    openValueInNewTab(selectedVariable);

                }
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                if (selectedVariable == null) {
                    System.out.println("null is selected instead of tests; returning...");
                    return;
                }
                JBPopupMenu menu = create_variable_popup(selectedVariable);
                if (_variablesList.isShowing()) {
                    menu.show(_variablesList, e.getPoint().x, e.getPoint().y);
                }

            }
        };
    }

    private void openValueInNewTab(PyCrunchVariableSnapshot selectedVariable) {
        if (selectedVariable == null) {
            return;
        }

        ScrollableJBTextEditor editor = new ScrollableJBTextEditor(_variableValueText.getText(), _project, new JsonFileType());
        Font font = EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN);
//                    font = new Font(font.getFontName(), font.getStyle(), font.getSize());
        editor.setFont(font);
        Content x = _toolWindow.getContentManager().getFactory().createContent(editor,  selectedVariable._name+ " - " + _selectedTest.name, false);
        _toolWindow.getContentManager().addContent(x);
        _toolWindow.getContentManager().setSelectedContent(x);
    }

    private JBPopupMenu create_variable_popup(PyCrunchVariableSnapshot selectedVariable) {
        JBPopupMenu menu = new JBPopupMenu();
        JBMenuItem openInNewTab = new JBMenuItem("Open in new tab");
        openInNewTab.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openValueInNewTab(selectedVariable);

            }
        });



        menu.add(openInNewTab);

        return menu;
    }

    private void selected_variable_will_change(ListSelectionEvent e) {
        int firstIndex = e.getFirstIndex();
        Object x = _variablesList.getSelectedValue();
        if (x == null) {
            return;
        }
        if (x instanceof PyCrunchVariableSnapshot) {
            PyCrunchVariableSnapshot snapshot = (PyCrunchVariableSnapshot)x;
            _selectedVariable =snapshot;
            _variableValueText.setText(snapshot.get_value_as_text());

            double ts = snapshot._ts;
            updateTimestampLabel(ts);
        }
    }

    private void updateTimestampLabel(double ts) {
//        DecimalFormat df = new DecimalFormat("#.###");
//        df.setRoundingMode(RoundingMode.CEILING);
        double x = Math.round(ts * 1000) / 1000.0;

//        String txt = df.format(ts);
        _labelEventTime.setText(x + " ms");
    }

    public JPanel getContent() {
        return _mainPanel;
    }

    private void connect_pycrunch_bus() {
        _bus.connect().subscribe(PycrunchBusNotifier.CHANGE_ACTION_TOPIC, new PycrunchBusNotifier() {
            @Override
            public void beforeAction(String event) {

            }
            @Override
            public void engineDidConnect(String apiRoot) {
            }
            @Override
            public void engineDidDisconnect(String context){
            }
            @Override
            public void combinedCoverageDidUpdate(String context) {

            }

            @Override
            public void licenceInvalid() {

            }

            @Override
            public void did_select_test(PycrunchTestMetadata userObject) {
                System.out.println("Got to did_select_test" );
                _selectedTest = userObject;
                JSONArray jsonArray = _connector.GetVariablesState(userObject.fqn);
                try {
                    _currentState = PycrunchVariablesState.from_json_array(jsonArray);
                    invalidate_list();
                } catch (JSONException e) {
                    System.out.println("!!! error converting variables on did_select_test" );
                    e.printStackTrace();
                }

                _label_selected_test.setText(userObject.name);
            }

            @Override
            public void licenceActivated() {
            }
        });
    }

    private void invalidate_list() {
        _variablesList.setListData(_currentState._variableSnapshots.toArray());
    }



}
