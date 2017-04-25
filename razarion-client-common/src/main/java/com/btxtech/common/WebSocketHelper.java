package com.btxtech.common;

import elemental.client.Browser;

/**
 * Created by Beat
 * 25.04.2017.
 */
public interface WebSocketHelper {

    static String getUrl(String endpoint) {
        String wsProtocol;
        if (Browser.getWindow().getLocation().getProtocol().equals("https:")) {
            wsProtocol = "wss";
        } else {
            wsProtocol = "ws";
        }
        String port;
        if (Browser.getWindow().getLocation().getPort() == null || Browser.getWindow().getLocation().getPort().trim().isEmpty()) {
            port = "";
        } else {
            port = ":" + Browser.getWindow().getLocation().getPort();
        }

        return wsProtocol + "://" + Browser.getWindow().getLocation().getHostname() + port + endpoint;
    }
}
