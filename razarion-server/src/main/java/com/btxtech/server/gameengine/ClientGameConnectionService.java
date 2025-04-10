package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ConnectionMarshaller;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.HashMap;
import java.util.Map;

import static com.btxtech.server.gameengine.ClientGameConnection.MAPPER;

@Service
public class ClientGameConnectionService extends TextWebSocketHandler {
    private static final String CLIENT_GAME_CONNECTION = "ClientGameConnection";
    private final Logger logger = LoggerFactory.getLogger(ClientGameConnectionService.class);
    private final Map<String, ClientGameConnection> gameConnections = new HashMap<>();
    private final SessionService sessionService;
    private final Provider<ClientGameConnection> provider;
    private final UserService userService;
    @Autowired
    @Lazy
    private PlanetService planetService;

    public ClientGameConnectionService(SessionService sessionService,
                                       Provider<ClientGameConnection> provider,
                                       UserService userService) {
        this.sessionService = sessionService;
        this.provider = provider;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) {
        var clientGameConnection = provider.get();

        var httpSessionId = (String) wsSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        clientGameConnection.init(wsSession, httpSessionId);
        wsSession.getAttributes().put(CLIENT_GAME_CONNECTION, clientGameConnection);
        synchronized (gameConnections) {
            gameConnections.put(httpSessionId, clientGameConnection);
        }
        clientGameConnection.sendInitialSlaveSyncInfo(1); // TODO get correct user id
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        var clientGameConnection = (ClientGameConnection) session.getAttributes().get(CLIENT_GAME_CONNECTION);
        clientGameConnection.handleMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.warn("handleTransportError. Session: {}", session, exception);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus closeStatus) {
        var httpSessionId = (String) wsSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        synchronized (gameConnections) {
            var clientGameConnection = gameConnections.remove(httpSessionId);
            logger.info("Websocket clientGameConnection closed {}", clientGameConnection);
        }
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        sendToClients(GameConnectionPacket.BASE_CREATED, playerBase.getPlayerBaseInfo());
    }

    public void onBaseDeleted(PlayerBase playerBase) {
        sendToClients(GameConnectionPacket.BASE_DELETED, playerBase.getBaseId());
    }

    public void onBaseNameChanged(PlayerBase playerBase) {
        sendToClients(GameConnectionPacket.BASE_NAME_CHANGED, new PlayerBaseInfo().setBaseId(playerBase.getBaseId()).setName(playerBase.getName()));
    }

    public void onBaseHumanPlayerIdChanged(PlayerBase playerBase) {
        sendToClients(GameConnectionPacket.BASE_HUMAN_PLAYER_ID_CHANGED, new PlayerBaseInfo().setBaseId(playerBase.getBaseId()).setUserId(playerBase.getUserId()));
    }

    public void sendTickinfo(TickInfo tickInfo) {
        sendToClients(GameConnectionPacket.TICK_INFO, tickInfo);
    }

    public void sendResourcesBalanceChanged(PlayerBase playerBase, int resources) {
        PlayerSession playerSession = getPlayerSessionBase(playerBase);
        if (playerSession != null) {
            sendToClient(playerSession.getHttpSessionId(), GameConnectionPacket.RESOURCE_BALANCE_CHANGED, resources);
        }
    }

    public void onSyncItemRemoved(SyncItem syncItem, boolean explode) {
        SyncItemDeletedInfo syncItemDeletedInfo = new SyncItemDeletedInfo();
        syncItemDeletedInfo.setId(syncItem.getId()).setExplode(explode);
        sendToClients(GameConnectionPacket.SYNC_ITEM_DELETED, syncItemDeletedInfo);
    }

    public void onSyncResourceItemCreated(SyncResourceItem syncResourceItem) {
        sendToClients(GameConnectionPacket.SYNC_RESOURCE_ITEM_CHANGED, syncResourceItem.getSyncInfo());
    }

    public void onSyncBoxCreated(SyncBoxItem syncBoxItem) {
        sendToClients(GameConnectionPacket.SYNC_BOX_ITEM_CHANGED, syncBoxItem.getSyncInfo());
    }

    private void sendToClients(GameConnectionPacket packet, Object object) {
        String text;
        try {
            text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
            return;
        }

        synchronized (gameConnections) {
            gameConnections.values().forEach(gameConnection -> {
                try {
                    gameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    logger.warn(throwable.getMessage(), throwable);
                }
            });
        }
    }

    private void sendToClient(String httpSessionId, GameConnectionPacket packet, Object object) {
        ClientGameConnection clientGameConnection;
        synchronized (gameConnections) {
            clientGameConnection = gameConnections.get(httpSessionId);
            if (clientGameConnection == null) {
                return;
            }
        }
        try {
            String text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
            try {
                clientGameConnection.sendToClient(text);
            } catch (Throwable throwable) {
                logger.warn(throwable.getMessage(), throwable);
            }
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    private PlayerSession getPlayerSessionBase(PlayerBase playerBase) {
        return sessionService.findPlayerSession(playerBase.getUserId());
    }
}
