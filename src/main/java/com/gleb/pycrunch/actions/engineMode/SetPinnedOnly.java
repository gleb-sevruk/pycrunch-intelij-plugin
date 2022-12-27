package com.gleb.pycrunch.actions.engineMode;

import com.gleb.pycrunch.shared.EngineMode;

public class SetPinnedOnly extends BaseSetEngineMode {

    public SetPinnedOnly() {
        super(EngineMode.mode_pinned_automatically);
    }
}