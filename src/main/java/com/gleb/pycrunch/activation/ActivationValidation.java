package com.gleb.pycrunch.activation;

import com.gleb.pycrunch.shared.IdeNotifications;
import com.gleb.pycrunch.shared.MyPasswordStore;
import com.intellij.credentialStore.Credentials;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
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
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class ActivationValidation {
    public void ping_user_id(MyStateService state) {
        if (state.UserId == null) {
            state.UserId = UUID.randomUUID().toString();
        }

        new ActivationConnector().PingAlive(state);

    }


    public boolean is_valid_licence(MyStateService state) {
        ActivationInfo activationInfo = new ActivationInfo(state.ActivationData, state.Sig, state.Exp, state.ExpSig);
        if (!activationInfo.has_license()) {
//           we are in trial mode
            return check_trial(activationInfo);
        }

        if (state.ActivationData == null) {
            return false;
        }

        if (!activationInfo.verify_license_sig()) {
            return false;
        }

        try {
            String decoded = decodeString(activationInfo.file);
            JSONObject j = null;
            j = new JSONObject(decoded);

            String until = j.getString("license_valid_until");
            Instant date = DateWrapper.parse_from_iso(until);
            return DateWrapper.licence_still_valid(date);
        } catch (JSONException e) {
            return false;
        }
    }

    private boolean check_trial(ActivationInfo activationInfo) {
        if (!activationInfo.verify_trial_sig()) {
            return false;
        }

        try {
            String decoded = decodeString(activationInfo.exp);
            JSONObject j = new JSONObject(decoded);

            String until = j.getString("exp_date");
            Instant date = DateWrapper.parse_from_iso(until);
            return DateWrapper.licence_still_valid(date);
        } catch (JSONException e) {
            return false;
        }
    }

    public Instant license_exp_date(MyStateService state) {
        ActivationInfo activationInfo = new ActivationInfo(state.ActivationData, state.Sig, state.Exp, state.ExpSig);
        if (!activationInfo.verify_license_sig()) {
            return null;
        }

        try {
            String decoded = decodeString(activationInfo.file);
            JSONObject j = null;
            j = new JSONObject(decoded);

            String until = j.getString("license_valid_until");
            Instant date = DateWrapper.parse_from_iso(until);
            return date;
        } catch (JSONException e) {
            return null;
        }
    }

    public Instant trial_exp_date(MyStateService state) {
        ActivationInfo activationInfo = new ActivationInfo(state.ActivationData, state.Sig, state.Exp, state.ExpSig);
        if (!activationInfo.verify_trial_sig()) {
            return null;
        }

        try {
            String decoded = decodeString(activationInfo.exp);
            JSONObject j = null;
            j = new JSONObject(decoded);

            String until = j.getString("exp_date");
            Instant date = DateWrapper.parse_from_iso(until);
            return date;
        } catch (JSONException e) {
            return null;
        }
    }

    public static String decodeString(String encodedString) {
        byte[] bytes = Base64.getDecoder().decode(encodedString);
        return new String(bytes);
    }

    public void try_renew(Project _project, MyStateService state) {
        ActivationConnector activationConnector = new ActivationConnector();
        Credentials accountCredentials = MyPasswordStore.getAccountCredentials();
        if (accountCredentials == null) {
            System.out.println("No credentials found...");
            return;
        }
        ActivationInfo result = activationConnector.activate(accountCredentials.getUserName(), accountCredentials.getPasswordAsString());
        if (result != null) {
            if (!Objects.equals(result.sig, state.Sig)) {
//                License changed on server
                state.ActivationData = result.file;
                state.Sig = result.sig;
                ActivationInfo info = new ActivationInfo(state.ActivationData, state.Sig, state.Exp, state.ExpSig);
                IdeNotifications.notify(_project, "Licence for PyCrunch has been updated!", info.get_details(state), NotificationType.INFORMATION);
            }

        }
    }
}
