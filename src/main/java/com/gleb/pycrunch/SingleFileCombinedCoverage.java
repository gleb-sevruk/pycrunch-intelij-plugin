package com.gleb.pycrunch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

public class SingleFileCombinedCoverage {
    public HashMap<Integer, HashSet<String>> _lines_hit_by_run = new HashMap<>();

    public static SingleFileCombinedCoverage from_json(JSONObject j) throws JSONException {
        JSONObject lines = j.getJSONObject("lines_with_entrypoints");
        JSONArray array = lines.names();

        SingleFileCombinedCoverage result = new SingleFileCombinedCoverage();
        for (int i = 0; i < array.length(); i++) {
            String line_number = array.getString(i);
            HashSet<String> current_line_fqns = new HashSet<>();
            JSONArray test_fqns = lines.getJSONArray(line_number);
            for (int x = 0; x < test_fqns.length(); x++) {
                String fqn = test_fqns.getString(x);
                current_line_fqns.add(fqn);
            }
            if (current_line_fqns.size() > 0) {
                result._lines_hit_by_run.put(Integer.parseInt(line_number), current_line_fqns);
            }
        }
        return result;
    }

    public HashSet<String> TestsAtLine(Integer line_number) {
        return _lines_hit_by_run.getOrDefault(line_number, new HashSet<>());
    }
}
