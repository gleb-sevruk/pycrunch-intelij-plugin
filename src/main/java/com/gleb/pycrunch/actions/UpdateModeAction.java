package com.gleb.pycrunch.actions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UpdateModeAction {
    public void run(String mode, String api_uri) {
        String d = api_uri + "/engine-mode";
        HttpPost post = new HttpPost(d);
        JSONObject final_payload = new JSONObject();

        try {
            final_payload.put("mode", mode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        post.setEntity(new StringEntity(final_payload.toString(), ContentType.APPLICATION_JSON));
        HttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
