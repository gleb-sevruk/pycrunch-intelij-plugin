package com.gleb.pycrunch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PycrunchCombinedCoverage {

    private HashMap<String, String> _aggregated_results = new HashMap<>();

    public HashMap<String, SingleFileCombinedCoverage> _files = new HashMap<>();

    public static PycrunchCombinedCoverage from_json(JSONObject j) throws JSONException {
        PycrunchCombinedCoverage coverage = new PycrunchCombinedCoverage();
        append_aggregated_statuses(j, coverage);

        append_files_covered(j, coverage);

        return coverage;
    }

    private static void append_files_covered(JSONObject j, PycrunchCombinedCoverage coverage) throws JSONException {
        JSONArray combined_coverage = j.getJSONArray("combined_coverage");

        for (int i = 0; i < combined_coverage.length(); i++) {
            JSONObject combined_for_single_file = combined_coverage.getJSONObject(i);
            String filename = combined_for_single_file.getString("filename");
            coverage._files.put(filename, SingleFileCombinedCoverage.from_json(combined_for_single_file));
        }
    }

    private static void append_aggregated_statuses(JSONObject j, PycrunchCombinedCoverage coverage) throws JSONException {
        JSONObject aggregated_results = j.getJSONObject("aggregated_results");
        JSONArray array = aggregated_results.names();

        for (int i = 0; i < array.length(); i++) {
            String fqn = array.getString(i);
            JSONObject status = aggregated_results.getJSONObject(fqn);
            String state = status.getString("state");
            coverage._aggregated_results.put(fqn, state);
        }
    }



    public SingleFileCombinedCoverage GetLinesCovering(String filename) {
        return _files.get(filename);
    }


}
