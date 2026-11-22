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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
    /**
     * More than a start ever holds: the server sends some ten messages a second, and the start is
     * done within seconds. A worker that holds this many has been waiting for minutes, and the
     * world it holds is worth less than a fresh one.
     */
    static final int MAX_HELD_MESSAGES = 3000;
    private final Logger logger = Logger.getLogger(AbstractServerGameConnection.class.getName());
    private final GameEngineWorker gameEngineWorker;
    private final BaseItemService baseItemService;
    private final ResourceService resourceService;
    private final BoxService boxService;
    private final PlanetService planetService;
    /** Non-null while the connection is open but the engine has not started - see #holdMessages. */
    private List<String> heldMessages;
    private boolean heldOverflow;
    private double holdingSince;

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

    /**
     * Keeps everything the server sends until #releaseHeldMessages, instead of applying it.
     * <p>
     * Called before #init when the connection is opened ahead of the start. The server sends the
     * whole world the moment the connection is up - 337 kB, 33 kB on the wire (PROD 22.11.2026) -
     * and until 2026-11-22 the worker only opened the connection at RUN_GAME. Then the handshake
     * and that message queued behind the download burst RUN_GAME sets off (start glb, materials,
     * some forty UI chunks, ground textures): GAME_START to the first scene took 1.2 s in the
     * middle on PROD, 2-4 s in a phone profile, against 0.5 s on a free line. Opened during
     * INIT_WORKER, the world is already in when the engine starts.
     * <p>
     * Applied any earlier, the messages would meet a planet still being initialised.
     */
    public void holdMessages() {
        heldMessages = new ArrayList<>();
        heldOverflow = false;
        holdingSince = System.currentTimeMillis();
    }

    public boolean isHoldingMessages() {
        return heldMessages != null;
    }

    /**
     * The entry for every message from the server.
     */
    public void onMessageText(String text) {
        if (heldMessages == null) {
            handleMessage(text);
            return;
        }
        if (heldOverflow) {
            return;
        }
        // A reconnect while holding brings the whole world again; what came before it is stale.
        if (ConnectionMarshaller.deMarshallPackage(text, GameConnectionPacket.class) == GameConnectionPacket.INITIAL_SLAVE_SYNC_INFO) {
            heldMessages.clear();
        }
        if (heldMessages.size() >= MAX_HELD_MESSAGES) {
            heldOverflow = true;
            heldMessages.clear();
            return;
        }
        heldMessages.add(text);
    }

    /**
     * Applies what was held, in the order it came, and from then on applies every message as it
     * arrives. The held tick infos are older than the server by the time this runs; that is the
     * case of a worker that stood still for a moment, which #catchUpWithServerTick and the tick
     * count request after the initial sync already handle.
     *
     * @return false if more came than could be held - then nothing was applied, and the caller
     * has to open a new connection to get a world that fits together
     */
    public boolean releaseHeldMessages() {
        List<String> held = heldMessages;
        heldMessages = null;
        if (held == null) {
            return true;
        }
        if (heldOverflow) {
            logger.warning("[EarlyConnect] more than " + MAX_HELD_MESSAGES + " messages before the start, reconnecting");
            return false;
        }
        logger.info("[EarlyConnect] " + held.size() + " messages held for " + Math.round(System.currentTimeMillis() - holdingSince) + " ms");
        for (String text : held) {
            try {
                handleMessage(text);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "[EarlyConnect] held message failed: " + text.substring(0, Math.min(80, text.length())), t);
            }
        }
        return true;
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
