package com.gleb.pycrunch.shared;

import com.gleb.pycrunch.PycrunchConnector;
import com.gleb.pycrunch.activation.MyStateService;
import com.intellij.openapi.components.ServiceManager;

public class EngineMode {
    public static String mode_run_all_automatically = "auto";
    public static String mode_manual = "manual";
    public static String mode_pinned_automatically = "pinned";

    public String _mode = mode_run_all_automatically;

    public void SetAutomaticMode() {
        WillChangeTo(mode_run_all_automatically);
    }

    public void SetManualMode(){
        WillChangeTo(mode_manual);
    }



    public void SetPinnedOnlyMode() {
        WillChangeTo(mode_pinned_automatically);
    }

    public void WillChangeTo(String new_mode) {
        _mode = new_mode;
    }
}
