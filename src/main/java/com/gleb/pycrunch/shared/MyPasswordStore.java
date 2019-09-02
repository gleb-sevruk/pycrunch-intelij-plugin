package com.gleb.pycrunch.shared;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

public class MyPasswordStore {
    private final static String key = "api_account";

    public static void saveCredentials(String username, String password) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(key); // see previous sample
        Credentials credentials = new Credentials(username, password);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public static Credentials getAccountCredentials() {
        CredentialAttributes credentialAttributes = createCredentialAttributes(key);

        Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
        return credentials;
// or get password only
    }

    private static CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("PyCrunch", key));
    }

    public static void clearCredentials() {
        CredentialAttributes credentialAttributes = createCredentialAttributes(key); // see previous sample
        PasswordSafe.getInstance().set(credentialAttributes, null);
    }
}
