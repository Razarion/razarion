package com.btxtech.server.gameengine;

import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.connection.ConnectionMarshaller;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
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
public class ClientConnectionService {
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Collection<ClientConnection> clientConnections = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    public void onOpen(ClientConnection clientConnection) {
        synchronized (clientConnections) {
            clientConnections.add(clientConnection);
        }
    }

    public void onClose(ClientConnection clientConnection) {
        synchronized (clientConnections) {
            clientConnections.remove(clientConnection);
        }
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        PlayerBaseInfo playerBaseInfo = new PlayerBaseInfo().setBaseId(playerBase.getBaseId()).setName(playerBase.getName()).setCharacter(playerBase.getCharacter()).setHumanPlayerId(playerBase.getHumanPlayerId()).setResources(playerBase.getResources());
        sendToClients(ConnectionMarshaller.Package.BASE_CREATED, playerBaseInfo);
    }

    public void onBaseDeleted(PlayerBase playerBase) {
        sendToClients(ConnectionMarshaller.Package.BASE_DELETED, playerBase.getBaseId());
    }

    public void sendSyncBaseItem(SyncBaseItem syncBaseItem) {
        SyncBaseItemInfo syncBaseItemInfo = syncBaseItem.getSyncInfo();
        sendToClients(ConnectionMarshaller.Package.SYNC_BASE_ITEM_CHANGED, syncBaseItemInfo);
    }

    public void onSyncItemRemoved(SyncItem syncItem, boolean explode) {
        SyncItemDeletedInfo syncItemDeletedInfo = new SyncItemDeletedInfo();
        syncItemDeletedInfo.setId(syncItem.getId()).setExplode(explode);
        sendToClients(ConnectionMarshaller.Package.SYNC_ITEM_DELETED, syncItemDeletedInfo);
    }

    private void sendToClients(ConnectionMarshaller.Package aPackage, Object object) {
        String text;
        try {
            text = ConnectionMarshaller.marshall(aPackage, mapper.writeValueAsString(object));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
            return;
        }

        synchronized (clientConnections) {
            for (ClientConnection clientConnection : clientConnections) {
                try {
                    clientConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    exceptionHandler.handleException(throwable);
                }
            }
        }
    }
}
