package com.gleb.pycrunch;

public class ConnectionState {
    public int max_retries;
    public int current_retry;

    public void connectionFailed () {
        current_retry++;
    }

    public void reset() {
        current_retry = 0;
    }
}
