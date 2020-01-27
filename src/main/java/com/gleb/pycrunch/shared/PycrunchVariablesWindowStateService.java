package com.gleb.pycrunch.shared;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(name="pycrunch.variables_window_state", storages = {@Storage(value = "pycrunch-vars-window-jetbrains.xml")})
public class PycrunchVariablesWindowStateService implements PersistentStateComponent<PycrunchVariablesWindowStateService> {
    public int _splitPanePosition = 120;

    @Override
    public PycrunchVariablesWindowStateService getState() {
        return this;
    }

    @Override
    public void loadState(PycrunchVariablesWindowStateService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static PycrunchVariablesWindowStateService getInstance() {
        return ServiceManager.getService(PycrunchVariablesWindowStateService.class);
    }
}
