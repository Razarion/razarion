package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.system.ConnectionMarshaller;

/**
 * Created by Beat
 * 20.04.2017.
 */
public abstract class AbstractServerGameConnection {
    private final GameEngineWorker gameEngineWorker;
    private final BaseItemService baseItemService;
    private final ResourceService resourceService;
    private final BoxService boxService;
    private final PlanetService planetService;

    public AbstractServerGameConnection(PlanetService planetService,
                                        BoxService boxService,
                                        ResourceService resourceService,
                                        BaseItemService baseItemService,
                                        GameEngineWorker gameEngineWorker) {
        this.planetService = planetService;
        this.boxService = boxService;
        this.resourceService = resourceService;
        this.baseItemService = baseItemService;
        this.gameEngineWorker = gameEngineWorker;
    }

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

    public void sellItems(IdsDto items) {
        sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.SELL_ITEMS, toJson(items)));
    }

    public void useInventoryItem(UseInventoryItem useInventoryItem) {
        sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.USE_INVENTORY_ITEM, toJson(useInventoryItem)));
    }

    public void tickCountRequest() {
        sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.TICK_COUNT_REQUEST, null));
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
            case BASE_NAME_CHANGED:
                gameEngineWorker.onServerBaseNameChanged((PlayerBaseInfo) param);
                break;
            case BASE_HUMAN_PLAYER_ID_CHANGED:
                gameEngineWorker.onServerBaseHumanPlayerIdChanged((PlayerBaseInfo) param);
                break;
            case TICK_INFO:
                baseItemService.onTickInfo(planetService.getTickCount(), (TickInfo) param);
                break;
            case SYNC_RESOURCE_ITEM_CHANGED:
                resourceService.onSlaveSyncResourceItemChanged((SyncResourceItemInfo) param);
                break;
            case SYNC_BOX_ITEM_CHANGED:
                boxService.onSlaveSyncBoxItemChanged((SyncBoxItemInfo) param);
                break;
            case SYNC_ITEM_DELETED:
                gameEngineWorker.onServerSyncItemDeleted((SyncItemDeletedInfo) param);
                break;
            case RESOURCE_BALANCE_CHANGED:
                gameEngineWorker.updateResourceSlave((Integer) param);
                break;
            case INITIAL_SLAVE_SYNC_INFO:
                gameEngineWorker.onInitialSlaveSyncItemInfo((InitialSlaveSyncItemInfo) param);
                break;
            case TICK_COUNT_RESPONSE:
                planetService.setTickCount((long) ((double) param));
                break;
            default:
                throw new IllegalArgumentException("Unknown Packet: " + packet);
        }
    }
}
