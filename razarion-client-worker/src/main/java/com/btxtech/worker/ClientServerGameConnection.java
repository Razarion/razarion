package com.btxtech.worker;

import com.btxtech.common.WebSocketHelper;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
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
public class ClientServerGameConnection extends AbstractServerGameConnection {
    @Inject
    private ExceptionHandler exceptionHandler;
    private Logger logger = Logger.getLogger(ClientServerGameConnection.class.getName());
    private WebSocket webSocket;

    @Override
    public void init() {
        webSocket = Browser.getWindow().newWebSocket(WebSocketHelper.getUrl(RestUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT));
        webSocket.setOnerror(evt -> logger.severe("ClientServerGameConnection WebSocket OnError: " + evt));
        webSocket.setOnclose(evt -> logger.severe("ClientServerGameConnection WebSocket Close: " + evt));
        webSocket.setOnmessage(this::handleMessage);
        webSocket.setOnopen(evt -> logger.severe("ClientServerGameConnection WebSocket Open"));
    }

    private void handleMessage(Event event) {
        try {
            MessageEvent messageEvent = (MessageEvent) event;
            handleMessage((String) messageEvent.getData());
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerGameConnection.handleMessage() failed", throwable);
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
    protected Object fromJson(String jsonString, GameConnectionPacket packet) {
        return MarshallingWrapper.fromJSON(jsonString, packet.getTheClass());
    }

    @Override
    public void close() {
        try {
            webSocket.close();
            webSocket = null;
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerGameConnection.close()", throwable);
        }
    }
}
