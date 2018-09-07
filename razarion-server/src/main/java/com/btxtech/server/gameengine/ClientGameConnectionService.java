package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.tracker.ConnectionTrackingPersistence;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

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
    private PlanetService planetService;
    private final MapCollection<HumanPlayerId, ClientGameConnection> gameConnections = new MapCollection<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void onOpen(ClientGameConnection clientGameConnection, HumanPlayerId humanPlayerId) {
        synchronized (gameConnections) {
            gameConnections.put(humanPlayerId, clientGameConnection);
        }
        connectionTrackingPersistence.onGameConnectionOpened(clientGameConnection.getHttpSessionId(), humanPlayerId);
        sendInitialSlaveSyncInfo(humanPlayerId);
    }

    public void onClose(ClientGameConnection clientGameConnection) {
        synchronized (gameConnections) {
            gameConnections.remove(clientGameConnection.getHumanPlayerId(), clientGameConnection);
        }
        connectionTrackingPersistence.onGameConnectionClosed(clientGameConnection.getHttpSessionId(), clientGameConnection.getHumanPlayerId());
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
        sendToClients(GameConnectionPacket.BASE_HUMAN_PLAYER_ID_CHANGED, new PlayerBaseInfo().setBaseId(playerBase.getBaseId()).setHumanPlayerId(playerBase.getHumanPlayerId()));
    }

    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        SyncBaseItemInfo syncBaseItemInfo = syncBaseItem.getSyncInfo();
        sendToClients(GameConnectionPacket.SYNC_BASE_ITEM_CHANGED, syncBaseItemInfo);
    }

    public void sendSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (!syncBaseItem.isAlive()) {
            return;
        }
        SyncBaseItemInfo syncBaseItemInfo = syncBaseItem.getSyncInfo();
        if (syncBaseItem.getSyncPhysicalArea().canMove()) {
            System.out.println("*** sendSyncBaseItem: " + syncBaseItem.getId() + ". P: " + syncBaseItem.getSyncPhysicalArea().getPosition2d() + ". V: " + syncBaseItem.getSyncPhysicalMovable().getVelocity());
            // Thread.dumpStack();
        }
        sendToClients(GameConnectionPacket.SYNC_BASE_ITEM_CHANGED, syncBaseItemInfo);
    }

    public void sendResourcesBalanceChanged(PlayerBase playerBase, int resources) {
        PlayerSession playerSession = getPlayerSessionBase(playerBase);
        if (playerSession != null) {
            sendToClient(playerBase.getHumanPlayerId(), GameConnectionPacket.RESOURCE_BALANCE_CHANGED, resources);
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

    private void sendToClient(HumanPlayerId humanPlayerId, GameConnectionPacket packet, Object object) {
        Collection<ClientGameConnection> clientGameConnections;
        synchronized (gameConnections) {
            clientGameConnections = gameConnections.get(humanPlayerId);
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
        return sessionService.findPlayerSession(playerBase.getHumanPlayerId());
    }


    private void sendInitialSlaveSyncInfo(HumanPlayerId humanPlayerId) {
        try {
            sendToClient(humanPlayerId, GameConnectionPacket.INITIAL_SLAVE_SYNC_INFO, planetService.generateSlaveSyncItemInfo(humanPlayerId));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }
}
