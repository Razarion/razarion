package com.btxtech.worker;

import com.btxtech.common.system.WebSocketWrapper;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import elemental2.dom.Event;
import elemental2.dom.MessageEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.04.2017.
 */
public class ClientServerGameConnection extends AbstractServerGameConnection {
    private final Logger logger = Logger.getLogger(ClientServerGameConnection.class.getName());
    private final GameEngineWorker gameEngineWorker;
    private final WebSocketWrapper webSocketWrapper;

    @Inject
    public ClientServerGameConnection(GameEngineWorker gameEngineWorker,
                                      WebSocketWrapper webSocketWrapper,
                                      PlanetService planetService,
                                      BoxService boxService,
                                      ResourceService resourceService,
                                      BaseItemService baseItemService) {
        super(planetService, boxService, resourceService, baseItemService, gameEngineWorker);
        this.gameEngineWorker = gameEngineWorker;
        this.webSocketWrapper = webSocketWrapper;
    }

    @Override
    public void init() {
        webSocketWrapper.start(CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT,
                () -> sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.SET_GAME_SESSION_UUID, toJson(gameEngineWorker.getGameSessionUuid()))),
                this::handleMessage,
                () -> {
                },
                gameEngineWorker::onConnectionLost);
    }

    private void handleMessage(Event event) {
        try {
            MessageEvent messageEvent = (MessageEvent) event;
            handleMessage((String) messageEvent.data);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "ClientServerGameConnection.handleMessage() failed", throwable);
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocketWrapper.send(text);
    }

    @Override
    protected String toJson(Object param) {
        // return MarshallingWrapper.toJSON(param);
        throw new UnsupportedOperationException("TODO.........MarshallingWrapper.toJSON(param)");
    }

    @Override
    protected Object fromJson(String jsonString, GameConnectionPacket packet) {
        // return MarshallingWrapper.fromJSON(jsonString, packet.getTheClass());
        throw new UnsupportedOperationException("TODO.........MarshallingWrapper.fromJSON(jsonString, packet.getTheClass())");
    }

    @Override
    public void close() {
        webSocketWrapper.close();
    }
}
