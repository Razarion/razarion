package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.planet.connection.AbstractServerConnection;
import com.btxtech.shared.gameengine.planet.connection.ConnectionMarshaller;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import javax.inject.Inject;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

/**
 * Created by Beat
 * 21.04.2017.
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class DevToolServerConnection extends AbstractServerConnection {
    @Inject
    private ExceptionHandler exceptionHandler;
    private RemoteEndpoint remoteEndpoint;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        try {
            String destUri = "ws://localhost:8080" + RestUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT;
            WebSocketClient client = new WebSocketClient();
            NewCookie newCookie = HttpConnectionEmu.getInstance().getSessionCookie();
            CookieStore cookieStore = new HttpCookieStore();
            cookieStore.add(new URI("http://localhost:8080"), new HttpCookie(newCookie.getName(), newCookie.getValue()));
            client.setCookieStore(cookieStore);
            client.start();
            client.connect(this, new URI(destUri), new ClientUpgradeRequest());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
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
    protected Object fromJson(String jsonString, ConnectionMarshaller.Package aPackage) {
        try {
            return mapper.readValue(jsonString, aPackage.getTheClass());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        remoteEndpoint = null;
    }
}
