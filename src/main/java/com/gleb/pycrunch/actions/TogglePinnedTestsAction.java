package com.gleb.pycrunch.actions;

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
    private String _api_uri;

    public TogglePinnedTestsAction(String api_uri) {
        _api_uri = api_uri;
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
        String d =  _api_uri + "/" + action + "-tests";
        HttpPost post = new HttpPost(d);
        JSONObject final_payload = new JSONObject();

        JSONArray payload = new JSONArray(fqns);
        final_payload.put("fqns", payload);
        post.setEntity(new StringEntity(final_payload.toString(), ContentType.APPLICATION_JSON));
        HttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}