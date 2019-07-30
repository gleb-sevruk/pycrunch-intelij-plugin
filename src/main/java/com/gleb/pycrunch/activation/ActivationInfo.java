package com.gleb.pycrunch.activation;

public class ActivationInfo {
    public final String file;
    public String sig;

    public ActivationInfo(String file, String sig) {
        this.sig = sig;
        this.file = file;
    }

    public boolean verify_sig() {
        if (sig.equals("777")) {
            return true;
        }

        return false;

    }
}
