package com.gleb.pycrunch.activation;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Base64;

public class ActivationValidation {
    public boolean is_valid_licence(MyStateService state) {
        if (state.ActivationData == null) {
            return false;
        }

        ActivationInfo activationInfo = new ActivationInfo(state.ActivationData, state.Sig);
        if (!activationInfo.verify_sig()) {
            return false;
        }

        try {
            String decoded = decodeString(activationInfo.file);
            JSONObject j = null;
            j = new JSONObject(decoded);

            String until = j.getString("license_valid_until");
            Instant date = DateWrapper.parse_from_iso(until);
            return DateWrapper.licence_still_valid(date);
        }
         catch (JSONException e) {
            return false;
        }
    }

    public static String decodeString(String encodedString) {
        byte[] bytes = Base64.getDecoder().decode(encodedString);
        return new String(bytes);
    }
}
