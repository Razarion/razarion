package com.btxtech.worker;

import com.btxtech.common.WebSocketHelper;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import elemental.client.Browser;
import elemental.events.CloseEvent;
import elemental.events.ErrorEvent;
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
    @Inject
    private GameEngineWorker gameEngineWorker;
    private Logger logger = Logger.getLogger(ClientServerGameConnection.class.getName());
    private WebSocket webSocket;

    @Override
    public void init() {
        webSocket = Browser.getWindow().newWebSocket(WebSocketHelper.getUrl(CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT));
        webSocket.setOnerror(evt -> {
            try {
                ErrorEvent errorEvent = (ErrorEvent) evt;
                logger.severe("ClientServerGameConnection WebSocket OnError. Message " + errorEvent.getMessage());
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });
        webSocket.setOnclose(evt -> {
            try {
                CloseEvent closeEvent = (CloseEvent) evt;
                logger.severe("ClientServerGameConnection WebSocket Close. Code: " + closeEvent.getCode() + " Reason: " + closeEvent.getReason() + " WasClean: " + closeEvent.getReason());
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        });
        webSocket.setOnmessage(this::handleMessage);
        webSocket.setOnopen(evt -> {
            sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.SET_GAME_SESSION_UUID, toJson(gameEngineWorker.getGameSessionUuid())));
        });
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
