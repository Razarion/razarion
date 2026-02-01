package com.btxtech.worker;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.connection.AbstractServerGameConnection;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.worker.jso.JsConsole;
import com.btxtech.worker.jso.JsMessageEvent;
import com.btxtech.worker.jso.JsUtils;

import jakarta.inject.Inject;

/**
 * TeaVM implementation of ClientServerGameConnection
 * Uses TeaVM WebSocket wrapper instead of elemental2
 */
public class TeaVMClientServerGameConnection extends AbstractServerGameConnection {
    private final GameEngineWorker gameEngineWorker;
    private final TeaVMWebSocketWrapper webSocketWrapper;

    @Inject
    public TeaVMClientServerGameConnection(GameEngineWorker gameEngineWorker,
                                           TeaVMWebSocketWrapper webSocketWrapper,
                                           PlanetService planetService,
                                           BoxService boxService,
                                           ResourceService resourceService,
                                           BaseItemService baseItemService) {
        super(planetService, boxService, resourceService, baseItemService, gameEngineWorker);
        this.gameEngineWorker = gameEngineWorker;
        this.webSocketWrapper = webSocketWrapper;
    }

    @Override
    public void init(String bearerToken) {
        String url = CommonUrl.GAME_CONNECTION_WEB_SOCKET_ENDPOINT + JwtHelper.bearerTokenToUrl(bearerToken);

        webSocketWrapper.start(
                url,
                // onOpen callback
                () -> sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.SET_GAME_SESSION_UUID, toJson(gameEngineWorker.getGameSessionUuid()))),
                // onMessage callback
                this::handleMessageEvent,
                // onServerRestart callback
                () -> {
                },
                // onConnectionLost callback
                gameEngineWorker::onConnectionLost
        );
    }

    private void handleMessageEvent(JsMessageEvent event) {
        try {
            String data = JsUtils.asString(event.getData());
            handleMessage(data);
        } catch (Throwable throwable) {
            JsConsole.error("TeaVMClientServerGameConnection.handleMessage() failed: " + throwable.getMessage());
        }
    }

    @Override
    protected void sendToServer(String text) {
        webSocketWrapper.send(text);
    }

    @Override
    protected String toJson(Object param) {
        return TeaVMWorkerMarshaller.toJson(param);
    }

    @Override
    protected Object fromJson(String jsonString, GameConnectionPacket packet) {
        return TeaVMWorkerMarshaller.fromJson(jsonString, packet.getTheClass());
    }

    @Override
    public void close() {
        webSocketWrapper.close();
    }
}
