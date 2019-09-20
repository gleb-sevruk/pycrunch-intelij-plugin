package com.gleb.pycrunch.actions;

import com.github.nkzawa.socketio.client.Socket;
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

public class UpdateModeAction {
    private final Socket _socket;

    public UpdateModeAction(Socket socket) {

        _socket = socket;
    }

    public void run(String mode) throws JSONException {

        JSONObject final_payload = new JSONObject();
        final_payload.put("action", "engine-mode");
        final_payload.put("mode", mode);

        this._socket.emit("my event", final_payload);
    }
}
