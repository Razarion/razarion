package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.system.ConnectionMarshaller;

import javax.inject.Inject;

/**
 * Created by Beat
 * 20.04.2017.
 */
public abstract class AbstractServerGameConnection {
    @Inject
    private GameEngineWorker gameEngineWorker;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ResourceService resourceService;

    protected abstract void sendToServer(String text);

    protected abstract String toJson(Object param);

    protected abstract Object fromJson(String jsonString, GameConnectionPacket packet);

    public abstract void init();

    public abstract void close();

    public void createHumanBaseWithBaseItem(DecimalPosition position) {
        sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.CREATE_BASE, toJson(position)));
    }

    public void onCommandSent(BaseCommand baseCommand) {
        sendToServer(ConnectionMarshaller.marshall(baseCommand.connectionPackage(), toJson(baseCommand)));
    }

    public void handleMessage(String text) {
        GameConnectionPacket packet = ConnectionMarshaller.deMarshallPackage(text, GameConnectionPacket.class);
        String jsonString = ConnectionMarshaller.deMarshallPayload(text);
        Object param = fromJson(jsonString, packet);
        switch (packet) {

            case BASE_CREATED:
                gameEngineWorker.onServerBaseCreated((PlayerBaseInfo) param);
                break;
            case BASE_DELETED:
                gameEngineWorker.onServerBaseDeleted((int) param);
                break;
            case SYNC_BASE_ITEM_CHANGED:
                baseItemService.onSlaveSyncBaseItemChanged((SyncBaseItemInfo) param);
                break;
            case SYNC_RESOURCE_ITEM_CHANGED:
                resourceService.onSlaveSyncResourceItemChanged((SyncResourceItemInfo) param);
                break;
            case SYNC_ITEM_DELETED:
                 gameEngineWorker.onServerSyncItemDeleted((SyncItemDeletedInfo) param);
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }
}