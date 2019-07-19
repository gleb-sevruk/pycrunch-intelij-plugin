package com.gleb.pycrunch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class TestRunResult {
    public String captured_output;
    public Hashtable<String, TestRunResultFileCoverage> files_covered;
    public String status;

    public static TestRunResult from_json(JSONObject json) throws JSONException {
        TestRunResult result = new TestRunResult();
        result.captured_output = json.getString("captured_output");
        result.status = json.getString("status");
        result.files_covered = new Hashtable<>();
        JSONArray files = json.getJSONArray("files");
        for (int i=0; i < files.length(); i++) {
            JSONObject x = files.getJSONObject(i);
            result.files_covered.put(x.getString("filename"), TestRunResultFileCoverage.from_json(x));
        }
        return result;
    }
}
