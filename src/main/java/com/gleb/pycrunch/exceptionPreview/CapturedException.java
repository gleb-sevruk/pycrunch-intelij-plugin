package com.gleb.pycrunch.exceptionPreview;

import org.json.JSONObject;

public class CapturedException {
    public String traceback;

    public CapturedException(String filename, int line_number, String traceback, JSONObject variables){
        this.filename = filename;
        this.line_number = line_number;
        this.traceback = traceback;
        this.variables = variables;
    }
    public String filename;
    public int line_number;
    public JSONObject variables;
}
