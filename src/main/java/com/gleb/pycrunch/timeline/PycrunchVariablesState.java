package com.gleb.pycrunch.timeline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PycrunchVariablesState {

    public final List<PyCrunchVariableSnapshot> _variableSnapshots;

    public PycrunchVariablesState(List<PyCrunchVariableSnapshot> variableSnapshots) {
        _variableSnapshots = variableSnapshots;
    }

    public static PycrunchVariablesState from_json_array(JSONArray jsonArray) throws JSONException {
        List<PyCrunchVariableSnapshot> result = new ArrayList<>();
        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject x = jsonArray.getJSONObject(i);
            String name = x.getString("name");
            Object value = x.get("value");
            double ts = x.getDouble("ts");
            result.add(new PyCrunchVariableSnapshot(name, value, ts)) ;
        }

        PycrunchVariablesState newObject = new PycrunchVariablesState(result);
        return newObject;
    }
}
