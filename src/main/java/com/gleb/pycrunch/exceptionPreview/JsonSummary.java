package com.gleb.pycrunch.exceptionPreview;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSummary {

    public static String getJsonSummary(Object value, int maxLength) {
        if (value instanceof JSONObject) {
            return getJsonObjectSummary((JSONObject) value, maxLength);
        } else if (value instanceof JSONArray) {
            return getJsonArraySummary((JSONArray) value, maxLength);
        } else {
            return getObjectSummary(value, maxLength);
        }
    }

    private static String getJsonObjectSummary(JSONObject jsonObject, int maxLength) {
        StringBuilder summary = new StringBuilder();
        summary.append("{");

        int remainingLength = maxLength - 2; // Subtracting the length of the opening and closing brackets
        int keyCount = jsonObject.length();
        int keysProcessed = 0;
        Iterator<String> keys = jsonObject.sortedKeys();
        ArrayList<String> keyList = new ArrayList<>();
        while (keys.hasNext()) {
            String key = keys.next();
            String keyValueStr = key + ": " + jsonObject.optString(key, "N/A") + ", ";

            if (keyValueStr.length() > remainingLength) {
                keyValueStr = keyValueStr.substring(0, remainingLength) + "...";
                summary.append(keyValueStr);
                break;
            }

            summary.append(keyValueStr);
            remainingLength -= keyValueStr.length();
            keysProcessed++;
        }

        int keysRemaining = keyCount - keysProcessed;
        if (keysRemaining > 0) {
            summary.append("..., ");
            summary.append(keysRemaining).append(" keys more to see");
        }

        summary.append("}");

        return summary.toString();
    }

    private static String getJsonArraySummary(JSONArray jsonArray, int maxLength) {
        StringBuilder summary = new StringBuilder();
        summary.append("[");

        int remainingLength = maxLength - 2; // Subtracting the length of the opening and closing brackets
        int itemCount = jsonArray.length();
        int itemsProcessed = 0;

        for (int i = 0; i < itemCount; i++) {
            String itemStr = jsonArray.optString(i, "N/A") + ", ";

            if (itemStr.length() > remainingLength) {
                itemStr = itemStr.substring(0, remainingLength) + "...";
                summary.append(itemStr);
                break;
            }

            summary.append(itemStr);
            remainingLength -= itemStr.length();
            itemsProcessed++;
        }

        int itemsRemaining = itemCount - itemsProcessed;
        if (itemsRemaining > 0) {
            summary.append("..., ");
            summary.append(itemsRemaining).append(" items more to see");
        }

        summary.append("]");

        return summary.toString();
    }

    private static String getObjectSummary(Object value, int maxLength) {
        String stringValue = value.toString();
        if (stringValue.length() > maxLength) {
            stringValue = stringValue.substring(0, maxLength) + "...";
        }
        return stringValue;
    }
}

