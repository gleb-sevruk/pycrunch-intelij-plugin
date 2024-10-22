package com.gleb.pycrunch.activation;

import com.gleb.pycrunch.PycrunchCombinedCoverage;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class ActivationInfo {
    public final String file;
    public String sig;
    public String exp;
    public String exp_sig;

    public ActivationInfo(String file, String sig, String exp, String exp_sig) {
        this.sig = sig;
        this.file = file;
        this.exp = exp;
        this.exp_sig = exp_sig;
    }

    public boolean has_license() {
        return this.file != null;
    }

    public boolean verify_license_sig() {
        String msg = this.file;
        String sign = this.sig;
        return verify_sig(msg, sign);
    }

    public boolean verify_trial_sig() {
        String msg = this.exp;
        String sign = this.exp_sig;
        return verify_sig(msg, sign);
    }

    private boolean verify_sig(String msg, String sign) {
        try {
            RSAPublicKey publicKey = open_rsa_file();
            Signature signature = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            signature.initVerify(publicKey);
            byte[] decoded_message = Base64.getDecoder().decode(msg);
            signature.update(decoded_message);
            byte[] decoded_signature = Base64.getDecoder().decode(sign);
            boolean result = signature.verify(decoded_signature);
            System.out.println("sign verification: " + result);
            return result;
        }
        catch (Exception e) {
            System.out.println("Ok, little hacker... ");
            return false;
        }
    }


    public RSAPublicKey open_rsa_file() {
        List<String> lines = new ArrayList<>();
        try {
            InputStream inputStream = getClass().getResource("/public.pem").openStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while(reader.ready()) {
                String line = reader.readLine();
                lines.add(line);
            }
            if (lines.size() < 2)
                throw new IllegalArgumentException("Insufficient input");
            if (!lines.remove(0).startsWith("--"))
                throw new IllegalArgumentException("Expected header");
            if (!lines.remove(lines.size() - 1).startsWith("--"))
                throw new IllegalArgumentException("Expected footer");
            String join = String.join("", lines);
            byte[] raw = Base64.getDecoder().decode(join);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKey pub = (RSAPublicKey)factory.generatePublic(new X509EncodedKeySpec(raw));
            return pub;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String get_details(MyStateService _persistentState) {
        String mode = "unknown";
        String date = " -- ";
        ActivationValidation activationValidation = new ActivationValidation();
        if (!has_license()) {
            mode = "trial";
            Instant exp = activationValidation.trial_exp_date(_persistentState);
            date = format_date(exp);
        } else {
            mode = "licenced";
            Instant d = activationValidation.license_exp_date(_persistentState);
            date = format_date(d);
        }
        return mode + " - expires on " + date;
    }

    @NotNull
    private String format_date(Instant exp) {
        String date;
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
                        .withLocale( Locale.getDefault() )
                        .withZone( ZoneId.systemDefault() );
        date = formatter.format(exp);
        return date;
    }
}
