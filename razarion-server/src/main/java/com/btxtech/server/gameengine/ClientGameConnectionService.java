package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 21.04.2017.
 */
@Singleton
public class ClientGameConnectionService {
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Map<PlayerSession, ClientGameConnection> clientGameConnections = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void onOpen(ClientGameConnection clientGameConnection, PlayerSession session) {
        synchronized (clientGameConnections) {
            clientGameConnections.put(session, clientGameConnection);
        }
    }

    public void onClose(ClientGameConnection clientGameConnection) {
        synchronized (clientGameConnections) {
            clientGameConnections.remove(clientGameConnection.getSession());
        }
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        sendToClients(GameConnectionPacket.BASE_CREATED, playerBase.getPlayerBaseInfo());
    }

    public void onBaseDeleted(PlayerBase playerBase) {
        sendToClients(GameConnectionPacket.BASE_DELETED, playerBase.getBaseId());
    }

    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        SyncBaseItemInfo syncBaseItemInfo = syncBaseItem.getSyncInfo();
        sendToClients(GameConnectionPacket.SYNC_BASE_ITEM_CHANGED, syncBaseItemInfo);
    }

    public void sendSyncBaseItem(SyncBaseItem syncBaseItem) {
        SyncBaseItemInfo syncBaseItemInfo = syncBaseItem.getSyncInfo();
        sendToClients(GameConnectionPacket.SYNC_BASE_ITEM_CHANGED, syncBaseItemInfo);
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
            text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
            return;
        }

        synchronized (clientGameConnections) {
            for (ClientGameConnection clientGameConnection : clientGameConnections.values()) {
                try {
                    clientGameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    exceptionHandler.handleException(throwable);
                }
            }
        }
    }

    private void sendToClient(PlayerSession playerSession, GameConnectionPacket packet, Object object) {
        String text;
        try {
            text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
            return;
        }

        synchronized (clientGameConnections) {
            try {
                clientGameConnections.get(playerSession).sendToClient(text);
            } catch (Throwable throwable) {
                exceptionHandler.handleException(throwable);
            }
        }
    }
}
