package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.MapCollection;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collection;

import static com.btxtech.server.gameengine.ClientGameConnection.MAPPER;

@Service
public class ClientGameConnectionService extends TextWebSocketHandler {
    private static final String CLIENT_GAME_CONNECTION = "ClientGameConnection";
    private final Logger logger = LoggerFactory.getLogger(ClientGameConnectionService.class);
    private final MapCollection<Integer, ClientGameConnection> gameConnections = new MapCollection<>();
    private final SessionService sessionService;
    @Autowired
    @Lazy
    private PlanetService planetService;

    public ClientGameConnectionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var clientGameConnection = new ClientGameConnection(session, planetService);
        session.getAttributes().put(CLIENT_GAME_CONNECTION, clientGameConnection);
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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        // TODO clientGameConnectionService.onClose(this);
        // TODO async = null;
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
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
            sendToClient(playerBase.getUserId(), GameConnectionPacket.RESOURCE_BALANCE_CHANGED, resources);
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

    public Collection<ClientGameConnection> getClientGameConnections() {
        synchronized (gameConnections) {
            return gameConnections.getAll();
        }
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
            gameConnections.getAll().forEach(clientGameConnection -> {
                try {
                    clientGameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    logger.warn(throwable.getMessage(), throwable);
                }
            });
        }
    }

    private void sendToClient(int userId, GameConnectionPacket packet, Object object) {
        Collection<ClientGameConnection> clientGameConnections;
        synchronized (gameConnections) {
            clientGameConnections = gameConnections.get(userId);
            if (clientGameConnections == null) {
                return;
            }
        }
        try {
            String text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
            clientGameConnections.forEach(clientGameConnection -> {
                try {
                    clientGameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    logger.warn(throwable.getMessage(), throwable);
                }
            });
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    private PlayerSession getPlayerSessionBase(PlayerBase playerBase) {
        return sessionService.findPlayerSession(playerBase.getUserId());
    }


    private void sendInitialSlaveSyncInfo(int userId) {
        try {
            sendToClient(userId, GameConnectionPacket.INITIAL_SLAVE_SYNC_INFO, planetService.generateSlaveSyncItemInfo(userId));
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }
}
