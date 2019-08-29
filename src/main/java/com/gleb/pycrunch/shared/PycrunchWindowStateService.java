package com.gleb.pycrunch.shared;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(name="pycrunch.window_state", storages = {@Storage(value = "pycrunch-window-jetbrains.xml")})
public class PycrunchWindowStateService implements PersistentStateComponent<PycrunchWindowStateService> {
    public boolean _showPassedTests = true;
    public boolean _showFailedTests = true;
    public boolean _showPendingTests = true;
    public boolean _showPinnedTests = true;
    public boolean _wrapOutput = false;

    @Override
    public PycrunchWindowStateService getState() {
        return this;
    }

    @Override
    public void loadState(PycrunchWindowStateService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static PycrunchWindowStateService getInstance() {
        return ServiceManager.getService(PycrunchWindowStateService.class);
    }
}
