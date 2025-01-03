package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.tracker.ConnectionTrackingPersistence;
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
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Provider;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

/**
 * Created by Beat
 * 21.04.2017.
 */
@Singleton
public class ClientGameConnectionService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SessionService sessionService;
    @Inject
    private ConnectionTrackingPersistence connectionTrackingPersistence;
    @Inject
    private Provider<PlanetService> planetServiceInstance;
    private final MapCollection<Integer, ClientGameConnection> gameConnections = new MapCollection<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void onOpen(ClientGameConnection clientGameConnection, int userId) {
        synchronized (gameConnections) {
            gameConnections.put(userId, clientGameConnection);
        }
        connectionTrackingPersistence.onGameConnectionOpened(clientGameConnection.getHttpSessionId(), userId);
        sendInitialSlaveSyncInfo(userId);
    }

    public void onClose(ClientGameConnection clientGameConnection) {
        synchronized (gameConnections) {
            gameConnections.remove(clientGameConnection.getUserId(), clientGameConnection);
        }
        connectionTrackingPersistence.onGameConnectionClosed(clientGameConnection.getHttpSessionId(), clientGameConnection.getUserId());
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
            text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
            return;
        }

        synchronized (gameConnections) {
            gameConnections.getAll().forEach(clientGameConnection -> {
                try {
                    clientGameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    exceptionHandler.handleException(throwable);
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
            String text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
            clientGameConnections.forEach(clientGameConnection -> {
                try {
                    clientGameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    exceptionHandler.handleException(throwable);
                }
            });
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    private PlayerSession getPlayerSessionBase(PlayerBase playerBase) {
        return sessionService.findPlayerSession(playerBase.getUserId());
    }


    private void sendInitialSlaveSyncInfo(int userId) {
        try {
            sendToClient(userId, GameConnectionPacket.INITIAL_SLAVE_SYNC_INFO, planetServiceInstance.get().generateSlaveSyncItemInfo(userId));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }
}
