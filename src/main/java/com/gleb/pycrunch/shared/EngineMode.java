package com.gleb.pycrunch.shared;

public class EngineMode {
    public static String mode_run_all_automatically = "auto";
    public static String mode_manual = "manual";
    public static String mode_pinned_automatically = "pinned";

    public String _mode = mode_run_all_automatically;

    public void WillChangeTo(String new_mode) {
        _mode = new_mode;
    }
}
