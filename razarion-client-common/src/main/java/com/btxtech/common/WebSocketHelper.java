package com.btxtech.common;

import elemental2.dom.DomGlobal;

/**
 * Created by Beat
 * 25.04.2017.
 */
public interface WebSocketHelper {

    static String getUrl(String endpoint) {
        String wsProtocol;
        if (DomGlobal.window.location.protocol.equals("https:")) {
            wsProtocol = "wss";
        } else {
            wsProtocol = "ws";
        }
        String port;
        if (DomGlobal.window.location.port == null || DomGlobal.window.location.port.trim().isEmpty()) {
            port = "";
        } else {
            port = ":" + DomGlobal.window.location.port;
        }

        return wsProtocol + "://" + DomGlobal.window.location.hostname + port + endpoint;
    }
}
