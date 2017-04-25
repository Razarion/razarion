package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDtoUtils;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.effects.EffectVisualizationService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.projectile.ProjectileUiService;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.uiservice.tip.tiptask.CommandInfo;
import com.btxtech.uiservice.user.UserUiService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 02.01.2017.
 */
public abstract class GameEngineControl {
    // private Logger logger = Logger.getLogger(GameEngineControl.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private GameTipService gameTipService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    @Inject
    private AudioService audioService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private SelectionHandler selectionHandler;
    @Inject
    private EffectVisualizationService effectVisualizationService;
    @Inject
    private ProjectileUiService projectileUiService;
    @Inject
    private UserUiService userUiService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    private Consumer<Collection<PerfmonStatistic>> perfmonConsumer;
    private DeferredStartup deferredStartup;

    protected abstract void sendToWorker(GameEngineControlPackage.Command command, Object... data);

    protected abstract void onLoaded();

    public abstract boolean isStarted();

    public void start() {
        sendToWorker(GameEngineControlPackage.Command.START);
        sendToWorker(GameEngineControlPackage.Command.TICK_UPDATE_REQUEST);
    }

    public void stop(DeferredStartup deferredStartup) {
        perfmonConsumer = null;
        this.deferredStartup = deferredStartup;
        sendToWorker(GameEngineControlPackage.Command.STOP_REQUEST);
    }

    public void init(GameEngineConfig gameEngineConfig, DeferredStartup initializationReferredStartup) {
        this.deferredStartup = initializationReferredStartup;
        sendToWorker(GameEngineControlPackage.Command.INITIALIZE, gameEngineConfig, userUiService.getUserContext());
    }

    public void initWarm(PlanetConfig planetConfig, DeferredStartup deferredStartup) {
        this.deferredStartup = deferredStartup;
        sendToWorker(GameEngineControlPackage.Command.INITIALIZE_WARM, planetConfig, userUiService.getUserContext());
    }

    void startBots(List<BotConfig> botConfigs) {
        sendToWorker(GameEngineControlPackage.Command.START_BOTS, botConfigs);
    }

    void executeBotCommands(List<? extends AbstractBotCommandConfig> botCommandConfigs) {
        sendToWorker(GameEngineControlPackage.Command.EXECUTE_BOT_COMMANDS, botCommandConfigs);
    }

    void createResources(List<ResourceItemPosition> resourceItemTypePositions) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_RESOURCES, resourceItemTypePositions);
    }

    // Needs to be public or userUiService is not set
    public void createHumanBaseWithBaseItem(DecimalPosition position) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_HUMAN_BASE_WITH_BASE_ITEM,
                userUiService.getUserContext().getLevelId(),
                userUiService.getUserContext().getHumanPlayerId(),
                userUiService.getUserContext().getName(),
                position);
    }

    public void spawnSyncBaseItem(BaseItemType baseItemType, Collection<DecimalPosition> decimalPositions) {
        sendToWorker(GameEngineControlPackage.Command.SPAWN_BASE_ITEMS, baseItemType.getId(), new ArrayList<>(decimalPositions));
    }

    public void moveCmd(Collection<SyncBaseItemSimpleDto> items, DecimalPosition position) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_MOVE, SyncItemSimpleDtoUtils.toIds(items), position);
        gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.MOVE));
    }

    public void attackCmd(Collection<SyncBaseItemSimpleDto> attackers, SyncBaseItemSimpleDto target) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_ATTACK, SyncItemSimpleDtoUtils.toIds(attackers), target.getId());
        gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.ATTACK));
    }

    public void harvestCmd(Collection<SyncBaseItemSimpleDto> harvesters, SyncResourceItemSimpleDto resource) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_HARVEST, SyncItemSimpleDtoUtils.toIds(harvesters), resource.getId());
        gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.HARVEST));
    }

    public void pickBoxCmd(Collection<SyncBaseItemSimpleDto> pickers, SyncBoxItemSimpleDto box) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_PICK_BOX, SyncItemSimpleDtoUtils.toIds(pickers), box.getId());
        gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.PICK_BOX).setSynBoxItemId(box.getId()));
    }

    public void buildCmd(SyncBaseItemSimpleDto builder, DecimalPosition position, BaseItemType toBeBuild) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_BUILD, builder.getId(), position, toBeBuild.getId());
        gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.BUILD).setToBeBuiltId(toBeBuild.getId()));
    }

    public void finalizeBuildCmd(Collection<SyncBaseItemSimpleDto> builders, SyncBaseItemSimpleDto toBeFinalized) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_FINALIZE_BUILD, SyncItemSimpleDtoUtils.toIds(builders), toBeFinalized.getId());
        gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.FINALIZE_BUILD).setToBeFinalizedId(toBeFinalized.getId()));
    }

    public void fabricateCmd(Collection<SyncBaseItemSimpleDto> factories, BaseItemType toBeBuild) {
        sendToWorker(GameEngineControlPackage.Command.COMMAND_FABRICATE, SyncItemSimpleDtoUtils.toIds(factories), toBeBuild.getId());
        gameTipService.onCommandSent(new CommandInfo(CommandInfo.Type.FABRICATE).setToBeBuiltId(toBeBuild.getId()));
    }

    public void activateQuest(QuestConfig questConfig) {
        sendToWorker(GameEngineControlPackage.Command.ACTIVATE_QUEST, questConfig);
    }

    public void dropBoxes(List<BoxItemPosition> boxItemPositions) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_BOXES, boxItemPositions);
    }

    public void updateLevel(int levelId) {
        sendToWorker(GameEngineControlPackage.Command.UPDATE_LEVEL, levelId);
    }

    public void perfmonRequest(Consumer<Collection<PerfmonStatistic>> perfmonConsumer) {
        this.perfmonConsumer = perfmonConsumer;
        sendToWorker(GameEngineControlPackage.Command.PERFMON_REQUEST);
    }

    public void askTerrainZ(DecimalPosition position) {
        sendToWorker(GameEngineControlPackage.Command.SINGLE_Z_TERRAIN, position);
    }

    public void askOverlap(DecimalPosition position) {
        sendToWorker(GameEngineControlPackage.Command.TERRAIN_OVERLAP, position);
    }

    public void askOverlapType(int uuid, Collection<DecimalPosition> positions, int baseItemTypeId) {
        sendToWorker(GameEngineControlPackage.Command.TERRAIN_OVERLAP_TYPE, uuid, new ArrayList<>(positions), baseItemTypeId);
    }

    public void requestTerrainTile(Index terrainTileIndex) {
        sendToWorker(GameEngineControlPackage.Command.TERRAIN_TILE_REQUEST, terrainTileIndex);
    }

    public void overrideTerrain4Editor(List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        sendToWorker(GameEngineControlPackage.Command.EDITOR_OVERRIDE_TERRAIN, terrainSlopePositions, terrainObjectPositions);

    }

    private void onTickUpdate(Collection<SyncBaseItemSimpleDto> updatedSyncBaseItems, GameInfo gameInfo, Collection<SyncBaseItemSimpleDto> baseItemRemoved, Collection<SyncBaseItemSimpleDto> baseItemKilled) {
        baseItemUiService.updateSyncBaseItems(updatedSyncBaseItems);
        gameUiControl.setGameInfo(gameInfo);
        selectionHandler.baseItemRemoved(baseItemRemoved);
        selectionHandler.baseItemRemoved(baseItemKilled);
        effectVisualizationService.baseItemRemoved(baseItemRemoved);
        effectVisualizationService.onSyncBaseItemsExplode(baseItemKilled);
        sendToWorker(GameEngineControlPackage.Command.TICK_UPDATE_REQUEST);
    }

    private void onTickUpdateFailed() {
        sendToWorker(GameEngineControlPackage.Command.TICK_UPDATE_REQUEST);
    }

    private void onPerfmonResponse(Collection<PerfmonStatistic> statisticEntries) {
        if (perfmonConsumer != null) {
            perfmonConsumer.accept(statisticEntries);
            perfmonConsumer = null;
        }
    }

    private void onInitialized() {
        if (deferredStartup != null) {
            deferredStartup.finished();
            deferredStartup = null;
        }
    }

    private void onInitialisingFailed(String errorText) {
        if (deferredStartup != null) {
            deferredStartup.failed(errorText);
            deferredStartup = null;
        }
    }

    private void onStopped() {
        if (deferredStartup != null) {
            deferredStartup.finished();
            deferredStartup = null;
        }
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case LOADED:
                onLoaded();
                break;
            case INITIALIZED:
                onInitialized();
                break;
            case INITIALISING_FAILED:
                onInitialisingFailed((String) controlPackage.getSingleData());
                break;
            case TICK_UPDATE_RESPONSE:
                onTickUpdate((Collection<SyncBaseItemSimpleDto>) controlPackage.getData(0), (GameInfo) controlPackage.getData(1),
                        (Collection<SyncBaseItemSimpleDto>) controlPackage.getData(2), (Collection<SyncBaseItemSimpleDto>) controlPackage.getData(3));
                break;
            case TICK_UPDATE_RESPONSE_FAIL:
                onTickUpdateFailed();
                break;
            case SYNC_ITEM_START_SPAWNED:
                audioService.onSpawnSyncItem((SyncBaseItemSimpleDto) controlPackage.getSingleData());
                gameTipService.onSpawnSyncItem((SyncBaseItemSimpleDto) controlPackage.getSingleData());
                break;
            case SYNC_ITEM_IDLE:
                gameTipService.onSyncBaseItemIdle((SyncBaseItemSimpleDto) controlPackage.getSingleData());
                break;
            case RESOURCE_CREATED:
                resourceUiService.addResource((SyncResourceItemSimpleDto) controlPackage.getSingleData());
                break;
            case RESOURCE_DELETED:
                resourceUiService.removeResource((Integer) controlPackage.getSingleData());
                break;
            case BOX_CREATED:
                boxUiService.addBox((SyncBoxItemSimpleDto) controlPackage.getSingleData());
                break;
            case BOX_DELETED:
                boxUiService.removeBox((Integer) controlPackage.getSingleData());
                break;
            case BASE_CREATED:
                baseItemUiService.addBase((PlayerBaseDto) controlPackage.getSingleData());
                break;
            case BASE_DELETED:
                baseItemUiService.removeBase((Integer) controlPackage.getSingleData());
                break;
            case QUEST_PASSED:
                gameUiControl.onQuestPassed();
                break;
            case BOX_PICKED:
                userUiService.onOnBoxPicked((BoxContent) controlPackage.getSingleData());
                break;
            case PROJECTILE_FIRED:
                projectileUiService.onProjectileFired((int) controlPackage.getData(0), (Vertex) controlPackage.getData(1), (Vertex) controlPackage.getData(2));
                break;
            case PROJECTILE_DETONATION:
                effectVisualizationService.onProjectileDetonation((int) controlPackage.getData(0), (Vertex) controlPackage.getData(1));
                break;
            case PERFMON_RESPONSE:
                onPerfmonResponse((Collection<PerfmonStatistic>) controlPackage.getData(0));
                break;
            case SINGLE_Z_TERRAIN_ANSWER:
                terrainUiService.onTerrainZAnswer((DecimalPosition) controlPackage.getData(0), (double) controlPackage.getData(1));
                break;
            case SINGLE_Z_TERRAIN_ANSWER_FAIL:
                terrainUiService.onTerrainZAnswerFail((DecimalPosition) controlPackage.getData(0));
                break;
            case TERRAIN_OVERLAP_ANSWER:
                terrainUiService.onOverlapAnswer((DecimalPosition) controlPackage.getData(0), (boolean) controlPackage.getData(1));
                break;
            case TERRAIN_OVERLAP_TYPE_ANSWER:
                terrainUiService.onOverlapTypeAnswer((int) controlPackage.getData(0), (boolean) controlPackage.getData(1));
                break;
            case TERRAIN_TILE_RESPONSE:
                terrainUiService.onTerrainTileResponse((TerrainTile) controlPackage.getData(0));
                break;
            case STOP_RESPONSE:
                onStopped();
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }
}
