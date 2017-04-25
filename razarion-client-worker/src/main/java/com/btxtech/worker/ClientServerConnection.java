package com.btxtech.worker;

import com.btxtech.shared.gameengine.planet.connection.AbstractServerConnection;
import com.btxtech.shared.gameengine.planet.connection.ConnectionMarshaller;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.MessageEvent;
import elemental.html.WebSocket;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.04.2017.
 */
@Dependent
public class ClientServerConnection extends AbstractServerConnection {
    @Inject
    private ExceptionHandler exceptionHandler;
    private Logger logger = Logger.getLogger(ClientServerConnection.class.getName());
    private WebSocket webSocket;

    @Override
    public void init() {
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

        String url = wsProtocol + "://" + Browser.getWindow().getLocation().getHostname() + port + RestUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT;

        logger.severe("WS URL: " + url);

        webSocket = Browser.getWindow().newWebSocket(url);
        webSocket.setOnerror(evt -> logger.severe("WebSocket OnError: " + evt));
        webSocket.setOnclose(evt -> logger.severe("WebSocket Close: " + evt));
        webSocket.setOnmessage(this::handleMessage);
        webSocket.setOnopen(evt -> logger.severe("WebSocket Open"));
    }

    private void handleMessage(Event event) {
        try {
            MessageEvent messageEvent = (MessageEvent) event;
            handleMessage((String) messageEvent.getData());
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerConnection.handleMessage() failed", throwable);
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocket.send(text);
    }

    @Override
    protected String toJson(Object param) {
        return MarshallingWrapper.toJSON(param);
    }

    @Override
    protected Object fromJson(String jsonString, ConnectionMarshaller.Package aPackage) {
        return MarshallingWrapper.fromJSON(jsonString, aPackage.getTheClass());
    }

    @Override
    public void close() {
        try {
            webSocket.close();
            webSocket = null;
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerConnection.close()", throwable);
        }
    }
}
