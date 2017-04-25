package com.btxtech.server.gameengine;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 21.04.2017.
 */
@Singleton
public class ClientGameConnectionService {
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Collection<ClientGameConnection> clientGameConnections = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void onOpen(ClientGameConnection clientGameConnection) {
        synchronized (clientGameConnections) {
            clientGameConnections.add(clientGameConnection);
        }
    }

    public void onClose(ClientGameConnection clientGameConnection) {
        synchronized (clientGameConnections) {
            clientGameConnections.remove(clientGameConnection);
        }
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        PlayerBaseInfo playerBaseInfo = new PlayerBaseInfo().setBaseId(playerBase.getBaseId()).setName(playerBase.getName()).setCharacter(playerBase.getCharacter()).setHumanPlayerId(playerBase.getHumanPlayerId()).setResources(playerBase.getResources());
        sendToClients(GameConnectionPacket.BASE_CREATED, playerBaseInfo);
    }

    public void onBaseDeleted(PlayerBase playerBase) {
        sendToClients(GameConnectionPacket.BASE_DELETED, playerBase.getBaseId());
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

    private void sendToClients(GameConnectionPacket packet, Object object) {
        String text;
        try {
            text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
            return;
        }

        synchronized (clientGameConnections) {
            for (ClientGameConnection clientGameConnection : clientGameConnections) {
                try {
                    clientGameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    exceptionHandler.handleException(throwable);
                }
            }
        }
    }
}
