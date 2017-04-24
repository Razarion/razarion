package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.04.2017.
 */
public abstract class AbstractServerConnection {
    @Inject
    private GameEngineWorker gameEngineWorker;
    @Inject
    private BaseItemService baseItemService;

    protected abstract void sendToServer(String text);

    protected abstract String toJson(Object param);

    protected abstract Object fromJson(String jsonString, ConnectionMarshaller.Package aPackage);

    public abstract void init();

    public void createHumanBaseWithBaseItem(DecimalPosition position) {
        sendToServer(ConnectionMarshaller.marshall(ConnectionMarshaller.Package.CREATE_BASE, toJson(position)));
    }

    public void onCommandSent(BaseCommand baseCommand) {
        sendToServer(ConnectionMarshaller.marshall(baseCommand.connectionPackage(), toJson(baseCommand)));
    }

    public void handleMessage(String text) {
        ConnectionMarshaller.Package aPackage = ConnectionMarshaller.deMarshallPackage(text);
        String jsonString = ConnectionMarshaller.deMarshallPayload(text);
        Object param = fromJson(jsonString, aPackage);
        switch (aPackage) {

            case BASE_CREATED:
                gameEngineWorker.onServerBaseCreated((PlayerBaseInfo) param);
                break;
            case BASE_DELETED:
                gameEngineWorker.onServerBaseDeleted((int) param);
                break;
            case SYNC_BASE_ITEM_CHANGED:
                baseItemService.onSlaveSyncBaseItemChanged((SyncBaseItemInfo) param);
                break;
            case SYNC_ITEM_DELETED:
                 gameEngineWorker.onServerSyncItemDeleted((SyncItemDeletedInfo) param);
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + aPackage);
        }
    }
}
