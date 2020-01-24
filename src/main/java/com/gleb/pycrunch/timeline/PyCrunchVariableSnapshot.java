package com.gleb.pycrunch.timeline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PyCrunchVariableSnapshot {
    public final String _name;
    private final Object _value;
    // timestamp
    public final double _ts;

    public PyCrunchVariableSnapshot(String name, Object value, double ts) {
        _name = name;
        _value = value;
        _ts = ts;
    }

    public String get_value_as_text() {
        if (_value instanceof JSONArray) {
            try {
                return ((JSONArray)_value).toString(2);
            } catch (JSONException e) {
                return "unhandled exception during conversion array " + e.toString();
            }
        }
        if (_value instanceof JSONObject) {
            try {
               return ((JSONObject)_value).toString(2);
            } catch (JSONException e) {
                return "unhandled exception during conversion object " + e.toString();
            }
        }

        return _value.toString();
    }

    @Override
    public String toString() {
        return _name;
    }
}
