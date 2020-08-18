package com.gleb.pycrunch.shared;

import java.io.IOException;
import java.net.ServerSocket;

public class FreePort {
    public static int find_free_port() {
        int port = 7777;
        ServerSocket s = null;
        try {
            s = new ServerSocket(0);
            port = s.getLocalPort();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }
}
