package com.gleb.pycrunch.activation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

// Cached until expiration
// Check on startup

public class ActivationConnector {
    public static String api_url = "https://api.pycrunch.com";
    public static String site_url = "https://pycrunch.com";
    public String licence_file = null;

    public ActivationInfo activate(String email, String password) {
        String endpoint = api_url + "/api/activation-status";
        HttpPost post = new HttpPost(endpoint);
        JSONObject final_payload = new JSONObject();

        try {
            final_payload.put("email", email);
            final_payload.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        post.setEntity(new StringEntity(final_payload.toString(), ContentType.APPLICATION_JSON));
        HttpClient client = HttpClients.createDefault();
        try {
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            String result = convertStreamToString(response.getEntity().getContent());
            JSONObject j = new JSONObject(result);
//            boolean licence_valid = j.getBoolean("licence_valid");
            String data = null;
            if (j.has("data")) {
                data = j.getString("data");
            }
            String sig = null;
            if (j.has("data")) {
                sig = j.getString("sig");
            }
            String exp = j.getString("exp");
            String exp_sig = j.getString("exp_sig");
            ActivationInfo activationInfo = new ActivationInfo(data, sig, exp, exp_sig);
            return activationInfo;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(2048); // Define a size if you have an idea of it.
        char[] read = new char[128]; // Your buffer size.
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i));
        }
        return sb.toString();
    }

}
