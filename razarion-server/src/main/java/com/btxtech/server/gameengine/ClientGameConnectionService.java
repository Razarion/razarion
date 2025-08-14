package com.btxtech.server.gameengine;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ConnectionMarshaller;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final Provider<ClientGameConnection> provider;
    private final Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter;
    private final UserService userService;

    public ClientGameConnectionService(Provider<ClientGameConnection> provider,
                                       Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
                                       UserService userService) {
        this.provider = provider;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) {
        var clientGameConnection = provider.get();

        Authentication auth = WebSocketUtils.getJwtFromWsSession(wsSession, jwtAuthenticationConverter);
        var httpSessionId = (String) wsSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        String userId = userService.getOrCreateUserId(auth, httpSessionId);

        clientGameConnection.init(wsSession, userId);
        wsSession.getAttributes().put(CLIENT_GAME_CONNECTION, clientGameConnection);
        synchronized (gameConnections) {
            gameConnections.put(userId, clientGameConnection);
        }
        clientGameConnection.sendInitialSlaveSyncInfo(clientGameConnection.getUserId());
        // TODO connectionTrackingPersistence.onGameConnectionOpened(clientSystemConnection.getSession().getHttpSessionId(), clientSystemConnection.getSession());
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
        var clientGameConnection = (ClientGameConnection) wsSession.getAttributes().get(CLIENT_GAME_CONNECTION);
        synchronized (gameConnections) {
            gameConnections.remove(clientGameConnection.getUserId());
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
        sendToClients(GameConnectionPacket.BASE_NAME_CHANGED, new PlayerBaseInfo().baseId(playerBase.getBaseId()).name(playerBase.getName()));
    }

    public void sendTickinfo(TickInfo tickInfo) {
        sendToClients(GameConnectionPacket.TICK_INFO, tickInfo);
    }

    public void sendResourcesBalanceChanged(PlayerBase playerBase, int resources) {
        sendToClient(playerBase.getUserId(), GameConnectionPacket.RESOURCE_BALANCE_CHANGED, resources);
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

    private void sendToClient(String userId, GameConnectionPacket packet, Object object) {
        ClientGameConnection clientGameConnection;
        synchronized (gameConnections) {
            clientGameConnection = gameConnections.get(userId);
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
}
