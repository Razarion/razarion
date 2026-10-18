package com.btxtech.shared.gameengine.planet.connection;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.UseInventoryItem;
import com.btxtech.shared.gameengine.GameEngineWorker;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.ProjectileFiredInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBoxItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemDeletedInfo;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemSpawnStart;
import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.system.ConnectionMarshaller;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.04.2017.
 */
public abstract class AbstractServerGameConnection {
    /**
     * How far the server's tick may run ahead of the worker's before the worker jumps to it. Half a
     * second: a tick info normally arrives a tick or two ahead (the worker's count was set a round
     * trip ago), and the jump is only for a worker that stood still.
     */
    private static final long MAX_TICKS_BEHIND = 5;
    private final Logger logger = Logger.getLogger(AbstractServerGameConnection.class.getName());
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

    public abstract void init(String bearerToken);

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

    public void surrenderBase() {
        sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.SURRENDER_BASE, null));
    }

    public void useInventoryItem(UseInventoryItem useInventoryItem) {
        sendToServer(ConnectionMarshaller.marshall(GameConnectionPacket.USE_INVENTORY_ITEM, toJson(useInventoryItem)));
    }

    /**
     * A worker that stood still - the tab in the background, the screen off, a long collection - drops
     * the runs it missed and goes on from now, and its tick count stays that far behind the server's
     * for good: it was set once, at the initial sync. Every tick info from then on waited in the
     * queue and was shown that much later - orders seemed to do nothing, a quest passed before the
     * unit moved - and the queue held old tick infos that brought deleted units back (PROD
     * 14.-16.10.2026: up to 76 ticks dropped in one stall). The tick info carries the server's tick,
     * so the worker jumps to it: first the queued older ones in order, then this one.
     */
    private void catchUpWithServerTick(TickInfo tickInfo) {
        long serverTick = (long) tickInfo.getTickCount();
        long workerTick = planetService.getTickCount();
        if (serverTick - workerTick <= MAX_TICKS_BEHIND) {
            return;
        }
        logger.info("[TickCatchUp] worker " + workerTick + " -> server " + serverTick + " (" + (serverTick - workerTick) + " ticks behind)");
        planetService.setTickCount(serverTick);
        baseItemService.processPendingReceivedTickInfos(serverTick);
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
                catchUpWithServerTick((TickInfo) param);
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
            case SYNC_ITEM_SPAWN_START:
                gameEngineWorker.onSpawnSyncItemStartSlave((SyncItemSpawnStart) param);
                break;
            case PROJECTILE_FIRED:
                gameEngineWorker.onServerProjectileFired((ProjectileFiredInfo) param);
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
