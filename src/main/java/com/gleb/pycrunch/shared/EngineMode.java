package com.gleb.pycrunch.shared;

import com.gleb.pycrunch.PycrunchConnector;

public class EngineMode {
    public String mode_run_all_automatically = "auto";
    public String mode_manual = "manual";
    public String mode_pinned_automatically = "pinned";

    public String _mode = mode_run_all_automatically;
    private PycrunchConnector _connector;

    public EngineMode(PycrunchConnector connector) {
        _connector = connector;
    }

    public void SetAutomaticMode() {
        _mode = mode_run_all_automatically;
        _connector.update_mode(_mode);
    }

    public void SetManualMode() {
        _mode = mode_manual;
        _connector.update_mode(_mode);
    }

    public void SetPinnedOnlyMode() {
        _mode = mode_pinned_automatically;
        _connector.update_mode(_mode);
    }
}
