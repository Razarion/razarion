package com.btxtech.shared.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.GameLogicListener;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.PlanetTickListener;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.NoInterpolatedTerrainTriangleException;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.perfmon.PerfmonService;
import com.btxtech.shared.utils.ExceptionUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.07.2016.
 */
public abstract class GameEngineWorker implements PlanetTickListener, QuestListener, GameLogicListener {
    private Logger logger = Logger.getLogger(GameEngineWorker.class.getName());
    @Inject
    private PlanetService planetService;
    @Inject
    private Event<GameEngineInitEvent> gameEngineInitEvent;
    @Inject
    private BotService botService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private QuestService questService;
    @Inject
    private BoxService boxService;
    @Inject
    private CommandService commandService;
    @Inject
    private GameLogicService logicService;
    @Inject
    private PerfmonService perfmonService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ItemTypeService itemTypeService;
    private UserContext userContext;
    private List<SyncBaseItemSimpleDto> killed = new ArrayList<>();
    private List<SyncBaseItemSimpleDto> removed = new ArrayList<>();
    private int xpFromKills;
    private boolean sendTickUpdate;

    protected abstract void sendToClient(GameEngineControlPackage.Command command, Object... object);

    @PostConstruct
    public void postConstruct() {
        questService.addQuestListener(this);
        logicService.setGameLogicListener(this);
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case INITIALIZE:
                initialise((GameEngineConfig) controlPackage.getData(0), (UserContext) controlPackage.getData(1));
                break;
            case START:
                start();
                break;
            case TICK_UPDATE_REQUEST:
                sendTickUpdate = true;
                break;
            case START_BOTS:
                botService.startBots((Collection<BotConfig>) controlPackage.getSingleData());
                break;
            case EXECUTE_BOT_COMMANDS:
                botService.executeCommands((List<? extends AbstractBotCommandConfig>) controlPackage.getSingleData());
                break;
            case CREATE_RESOURCES:
                resourceService.createResources((Collection<ResourceItemPosition>) controlPackage.getSingleData());
                break;
            case CREATE_HUMAN_BASE_WITH_BASE_ITEM:
                baseItemService.createHumanBaseWithBaseItem((Integer) controlPackage.getData(0), (Integer) controlPackage.getData(1), (String) controlPackage.getData(2), (Integer) controlPackage.getData(3), (DecimalPosition) controlPackage.getData(4));
                break;
            case SPAWN_BASE_ITEMS:
                baseItemService.spawnSyncBaseItem((Integer) controlPackage.getData(0), (Collection<DecimalPosition>) controlPackage.getData(1), baseItemService.getPlayerBase4UserId(userContext.getUserId()));
                break;
            case CREATE_BOXES:
                boxService.dropBoxes((List<BoxItemPosition>) controlPackage.getSingleData());
                break;
            case ACTIVATE_QUEST:
                questService.activateCondition(userContext.getUserId(), (QuestConfig) controlPackage.getData(0));
                break;
            case COMMAND_ATTACK:
                commandService.attack((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_FINALIZE_BUILD:
                commandService.finalizeBuild((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_BUILD:
                commandService.build((int) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1), (int) controlPackage.getData(2));
                break;
            case COMMAND_FABRICATE:
                commandService.fabricate((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_HARVEST:
                commandService.harvest((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case COMMAND_MOVE:
                commandService.move((List<Integer>) controlPackage.getData(0), (DecimalPosition) controlPackage.getData(1));
                break;
            case COMMAND_PICK_BOX:
                commandService.pickupBox((List<Integer>) controlPackage.getData(0), (int) controlPackage.getData(1));
                break;
            case UPDATE_LEVEL:
                baseItemService.updateLevel(userContext.getUserId(), (int) controlPackage.getData(0));
                break;
            case PERFMON_REQUEST:
                onPerfmonRequest();
                break;
            case SINGLE_Z_TERRAIN:
                getTerrainZ((DecimalPosition) controlPackage.getData(0));
                break;
            case TERRAIN_OVERLAP:
                getTerrainOverlap((DecimalPosition) controlPackage.getData(0));
                break;
            case TERRAIN_OVERLAP_TYPE:
                getTerrainOverlapBaseItemType((int) controlPackage.getData(0), (List<DecimalPosition>) controlPackage.getData(1), (int) controlPackage.getData(2));
                break;
            case TERRAIN_TILE_REQUEST:
                getTerrainTile((Index) controlPackage.getData(0));
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }

    private void initialise(GameEngineConfig gameEngineConfig, UserContext userContext) {
        try {
            this.userContext = userContext;
            gameEngineInitEvent.fire(new GameEngineInitEvent(gameEngineConfig));
            planetService.initialise(gameEngineConfig.getPlanetConfig());
            planetService.addTickListener(this);
            sendToClient(GameEngineControlPackage.Command.INITIALIZED, terrainService.createGroundVertexList(), terrainService.getSlopes(), terrainService.getWater());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            sendToClient(GameEngineControlPackage.Command.INITIALISING_FAILED, ExceptionUtil.setupStackTrace(null, t));
        }
    }

    public void start() {
        planetService.start();
    }

    public void stop() {
        planetService.stop();
    }

    @Override
    public void onPostTick() {
        if (!sendTickUpdate) {
            return;
        }
        sendTickUpdate = false;
        List<SyncBaseItemSimpleDto> syncItems = new ArrayList<>();
        syncItemContainerService.iterateOverItems(false, false, null, syncItem -> {
            if (syncItem instanceof SyncBaseItem) {
                SyncBaseItem syncBaseItem = (SyncBaseItem) syncItem;
                SyncBaseItemSimpleDto simpleDto = syncBaseItem.createSyncBaseItemSimpleDto();
                syncItems.add(simpleDto);
            }
            return null;
        });
        PlayerBase playerBase = baseItemService.getPlayerBase4UserId(userContext.getUserId());
        GameInfo gameInfo = new GameInfo();
        gameInfo.setXpFromKills(xpFromKills);
        xpFromKills = 0;
        List<SyncBaseItemSimpleDto> tmpKilled = killed;
        killed = new ArrayList<>();
        List<SyncBaseItemSimpleDto> tmpRemoved = removed;
        removed = new ArrayList<>();
        if (playerBase != null) {
            gameInfo.setHouseSpace(playerBase.getHouseSpace());
            gameInfo.setUsedHouseSpace(playerBase.getUsedHouseSpace());
            gameInfo.setResources((int) playerBase.getResources());
        }
        sendToClient(GameEngineControlPackage.Command.TICK_UPDATE, syncItems, gameInfo, tmpRemoved, tmpKilled);
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        SyncResourceItemSimpleDto syncResourceItemSimpleDto = new SyncResourceItemSimpleDto();
        syncResourceItemSimpleDto.setId(syncResourceItem.getId());
        syncResourceItemSimpleDto.setItemTypeId(syncResourceItem.getItemType().getId());
        syncResourceItemSimpleDto.setPosition2d(syncResourceItem.getSyncPhysicalArea().getPosition2d());
        syncResourceItemSimpleDto.setPosition3d(syncResourceItem.getSyncPhysicalArea().getPosition3d());
        syncResourceItemSimpleDto.setModel(syncResourceItem.getSyncPhysicalArea().getModelMatrices());
        sendToClient(GameEngineControlPackage.Command.RESOURCE_CREATED, syncResourceItemSimpleDto);
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        sendToClient(GameEngineControlPackage.Command.RESOURCE_DELETED, syncResourceItem.getId());
    }

    @Override
    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        SyncBoxItemSimpleDto syncBoxItemSimpleDto = new SyncBoxItemSimpleDto();
        syncBoxItemSimpleDto.setId(syncBoxItem.getId());
        syncBoxItemSimpleDto.setItemTypeId(syncBoxItem.getItemType().getId());
        syncBoxItemSimpleDto.setPosition2d(syncBoxItem.getSyncPhysicalArea().getPosition2d());
        syncBoxItemSimpleDto.setPosition3d(syncBoxItem.getSyncPhysicalArea().getPosition3d());
        syncBoxItemSimpleDto.setModel(syncBoxItem.getSyncPhysicalArea().getModelMatrices());
        sendToClient(GameEngineControlPackage.Command.BOX_CREATED, syncBoxItemSimpleDto);
    }

    @Override
    public void onBoxPicked(int userId, BoxContent boxContent) {
        if (userContext.getUserId() == userId) {
            sendToClient(GameEngineControlPackage.Command.BOX_PICKED, boxContent);
        }
    }

    @Override
    public void onSyncBoxDeleted(SyncBoxItem syncBoxItem) {
        sendToClient(GameEngineControlPackage.Command.BOX_DELETED, syncBoxItem.getId());
    }

    @Override
    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        sendToClient(GameEngineControlPackage.Command.SYNC_ITEM_IDLE, syncBaseItem.createSyncBaseItemSimpleDto());
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        sendToClient(GameEngineControlPackage.Command.SYNC_ITEM_START_SPAWNED, syncBaseItem.createSyncBaseItemSimpleDto());
    }

    @Override
    public void onBaseCreated(PlayerBase playerBase) {
        PlayerBaseDto playerBaseDto = new PlayerBaseDto();
        playerBaseDto.setBaseId(playerBase.getBaseId());
        playerBaseDto.setName(playerBase.getName());
        playerBaseDto.setCharacter(playerBase.getCharacter());
        playerBaseDto.setUserId(playerBase.getUserId());
        sendToClient(GameEngineControlPackage.Command.BASE_CREATED, playerBaseDto);
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase) {
        sendToClient(GameEngineControlPackage.Command.BASE_DELETED, playerBase.getBaseId());
    }

    @Override
    public void onQuestPassed(int userId, QuestConfig questConfig) {
        if (userContext.getUserId() == userId) {
            sendToClient(GameEngineControlPackage.Command.QUEST_PASSED);
        }
    }

    public UserContext getUserContext() {
        return userContext;
    }

    @Override
    public void onSyncItemKilled(SyncBaseItem target, SyncBaseItem actor) {
        killed.add(target.createSyncBaseItemSimpleDto());
        if (actor.getBase().getUserId() != null && actor.getBase().getUserId() == userContext.getUserId()) {
            xpFromKills += target.getBaseItemType().getXpOnKilling();
        }
    }

    @Override
    public void onSyncItemRemoved(SyncBaseItem syncBaseItem) {
        removed.add(syncBaseItem.createSyncBaseItemSimpleDto());
    }

    @Override
    public void onProjectileFired(int baseItemTypeId, Vertex muzzlePosition, Vertex target) {
        sendToClient(GameEngineControlPackage.Command.PROJECTILE_FIRED, baseItemTypeId, muzzlePosition, target);
    }

    @Override
    public void onProjectileDetonation(int baseItemTypeId, Vertex position) {
        sendToClient(GameEngineControlPackage.Command.PROJECTILE_DETONATION, baseItemTypeId, position);
    }

    private void onPerfmonRequest() {
        sendToClient(GameEngineControlPackage.Command.PERFMON_RESPONSE, perfmonService.getPerfmonStatistics(-1));
    }

    private void getTerrainZ(DecimalPosition position) {
        try {
            double z = terrainService.getInterpolatedTerrainTriangle(position).getHeight();
            sendToClient(GameEngineControlPackage.Command.SINGLE_Z_TERRAIN_ANSWER, position, z);
        } catch (NoInterpolatedTerrainTriangleException e) {
            logger.warning("GameEngineWorker.getTerrainZ() " + e.getMessage());
            sendToClient(GameEngineControlPackage.Command.SINGLE_Z_TERRAIN_ANSWER_FAIL, position);
        }
    }

    private void getTerrainOverlap(DecimalPosition position) {
        sendToClient(GameEngineControlPackage.Command.TERRAIN_OVERLAP_ANSWER, position, terrainService.overlap(position));
    }

    private void getTerrainOverlapBaseItemType(int uuid, List<DecimalPosition> positions, int baseItemType) {
        boolean overlaps = terrainService.overlap(positions, baseItemType);
        sendToClient(GameEngineControlPackage.Command.TERRAIN_OVERLAP_TYPE_ANSWER, uuid, overlaps);
    }

    private void getTerrainTile(Index terrainTileIndex) {
        TerrainTile terrainTile = terrainService.generateTerrainTile(terrainTileIndex);
        sendToClient(GameEngineControlPackage.Command.TERRAIN_TILE_RESPONSE, terrainTile);
    }
}
