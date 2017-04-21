package com.btxtech.server.gameengine;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by Beat
 * 21.04.2017.
 */
public class WebSocketEndpointConfigAware extends ServerEndpointConfig.Configurator {
    public static final String HTTP_SESSION_KEY = "httpSession";

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        config.getUserProperties().put(HTTP_SESSION_KEY, httpSession);
    }

}
