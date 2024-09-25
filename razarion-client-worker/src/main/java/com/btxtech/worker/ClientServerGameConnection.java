package com.btxtech.worker;

import com.btxtech.common.system.WebSocketWrapper;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.Event;
import elemental2.dom.MessageEvent;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 20.04.2017.
 */

public class ClientServerGameConnection extends AbstractServerGameConnection {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameEngineWorker gameEngineWorker;
    @Inject
    private WebSocketWrapper webSocketWrapper;

    @Override
    public void init() {
        webSocketWrapper.start(CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT,
                () -> sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.SET_GAME_SESSION_UUID, toJson(gameEngineWorker.getGameSessionUuid()))),
                this::handleMessage,
                () -> {
                },
                () -> gameEngineWorker.onConnectionLost());
    }

    private void handleMessage(Event event) {
        try {
            MessageEvent messageEvent = (MessageEvent) event;
            handleMessage((String) messageEvent.data);
        } catch (Throwable throwable) {
            exceptionHandler.handleException("ClientServerGameConnection.handleMessage() failed", throwable);
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocketWrapper.send(text);
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
        webSocketWrapper.close();
    }
}
