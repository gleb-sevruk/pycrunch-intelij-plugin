package com.gleb.pycrunch;

import org.jetbrains.annotations.NotNull;

public class PycrunchConnectionState {
    public String _version_string;
    public String _apiRoot;

    public String statusText() {
        return  "Connected to " + _apiRoot + versionStringOrEmpty();
    }

    @NotNull
    private String versionStringOrEmpty() {
        if (_version_string != null) {
            return " (" + _version_string + ")";
        } else {
            return "";
        }
    }


    public void engineVersion(int version_major, int version_minor) {
        _version_string = String.format("v%s.%d", String.valueOf(version_major), version_minor);
    }
}
