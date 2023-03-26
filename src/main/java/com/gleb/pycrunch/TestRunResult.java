package com.gleb.pycrunch;

import com.gleb.pycrunch.exceptionPreview.CapturedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

//#DEFINE string = String

public class TestRunResult {
    public String captured_output;
    public Hashtable<String, TestRunResultFileCoverage> files_covered;
    public String status;
    public String fqn;
    public JSONArray variables_state;

    public CapturedException captured_exception;

    public static TestRunResult from_json(JSONObject json) throws JSONException {
        TestRunResult result = new TestRunResult();
        result.captured_output = json.getString("captured_output");
        result.variables_state = json.getJSONArray("variables_state");
//        Backward compatible check >= engine 1.5
        if (json.has("captured_exception")) {
            if (!json.isNull("captured_exception")) {
                var ce_json = json.getJSONObject("captured_exception");
                result.captured_exception = new CapturedException(
                        ce_json.getString("filename"),
                        ce_json.getInt("line_number"),
                        ce_json.getString("full_traceback"),
                        ce_json.getJSONObject("variables")
                        );
            }
        }
        result.status = json.getString("status");
        result.files_covered = new Hashtable<>();
        JSONArray files = json.getJSONArray("files");
        for (int i=0; i < files.length(); i++) {
            JSONObject x = files.getJSONObject(i);
            result.files_covered.put(x.getString("filename"), TestRunResultFileCoverage.from_json(x));
        }
        JSONObject testMetadata = json.getJSONObject("test_metadata");
        result.fqn = testMetadata.getString("fqn");

        return result;
    }


}
