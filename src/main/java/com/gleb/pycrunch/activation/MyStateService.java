package com.gleb.pycrunch.activation;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(name="pycrunch.state", storages = {@Storage(value = "pycrunch-jetbrains.xml")})
public class MyStateService implements PersistentStateComponent<MyStateService> {
    public boolean INCLUDE_ALL_FIELDS_IN_COMPLETIONS = false;
    public boolean SHOW_TAIL_CALLS_IN_GUTTER = true;
    public String ActivationData = null;
    public String Sig = null;
    public String Exp = null;
    public String ExpSig = null;

    @Override
    public MyStateService getState() {
        return this;
    }

    @Override
    public void loadState(MyStateService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static MyStateService getInstance() {
        return ServiceManager.getService(MyStateService.class);
    }
}
