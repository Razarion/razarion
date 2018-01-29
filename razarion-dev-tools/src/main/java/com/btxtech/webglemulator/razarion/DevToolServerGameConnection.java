package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by Beat
 * 21.04.2017.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class DevToolServerGameConnection extends AbstractServerGameConnection implements DevToolConnectionDefault {
    @Inject
    private ExceptionHandler exceptionHandler;
    private RemoteEndpoint remoteEndpoint;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        try {
            init("ws://localhost:8080" + CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT, this);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("DevToolServerGameConnection closed: %d - %s%n", statusCode, reason);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        remoteEndpoint = session.getRemote();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        try {
            handleMessage(msg);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    protected void sendToServer(String text) {
        try {
            remoteEndpoint.sendString(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String toJson(Object param) {
        try {
            return mapper.writeValueAsString(param);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object fromJson(String jsonString, GameConnectionPacket packet) {
        try {
            return mapper.readValue(jsonString, packet.getTheClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        remoteEndpoint = null;
    }
}
