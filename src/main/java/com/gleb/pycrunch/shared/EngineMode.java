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
        WillChangeTo(mode_run_all_automatically);
        sendModeUpdateToEngine();
    }

    public void SetManualMode() {
        WillChangeTo(mode_manual);
        sendModeUpdateToEngine();
    }

    private void sendModeUpdateToEngine() {
        _connector.update_mode(_mode);
    }

    public void SetPinnedOnlyMode() {
        WillChangeTo(mode_pinned_automatically);
        sendModeUpdateToEngine();
    }

    public void WillChangeTo(String new_mode) {
        _mode = new_mode;
    }
}
