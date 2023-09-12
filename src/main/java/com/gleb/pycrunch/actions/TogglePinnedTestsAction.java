package com.gleb.pycrunch.actions;

import io.socket.client.Socket;

import com.gleb.pycrunch.PycrunchTestMetadata;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TogglePinnedTestsAction {
    private final Socket _socket;

    public TogglePinnedTestsAction(Socket _socket) {
        this._socket = _socket;
    }

    public void pin_tests(List<PycrunchTestMetadata> tests) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        for (PycrunchTestMetadata test: tests) {
            list.add(test.fqn);
        }

        String action = "pin";
        post_toggle_pin_command(action, list);

    }

    public void unpin_tests(List<PycrunchTestMetadata> tests) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        for (PycrunchTestMetadata test: tests) {
            list.add(test.fqn);
        }

        String action = "unpin";
        post_toggle_pin_command(action, list);

    }

    private void post_toggle_pin_command(String action, ArrayList<String> fqns) throws JSONException {
        String command_name = action + "-tests";

        JSONObject final_payload = new JSONObject();
        JSONArray payload = new JSONArray(fqns);
        final_payload.put("action", command_name);
        final_payload.put("fqns", payload);
        this._socket.emit("my event", final_payload);
    }
}