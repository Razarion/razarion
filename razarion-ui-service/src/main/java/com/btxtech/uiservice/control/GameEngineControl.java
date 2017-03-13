package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.gameengine.GameEngineControlPackage;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.GameInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.PlayerBaseDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDtoUtils;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.shared.system.perfmon.PerfmonStatistic;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.effects.EffectVisualizationService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.projectile.ProjectileUiService;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
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
    private Consumer<Collection<PerfmonStatistic>> perfmonConsumer;
    private DeferredStartup initializationReferredStartup;

    protected abstract void sendToWorker(GameEngineControlPackage.Command command, Object... data);

    protected abstract void onLoaded();

    public abstract boolean isStarted();

    public void start() {
        sendToWorker(GameEngineControlPackage.Command.START);
    }

    public void init(GameEngineConfig gameEngineConfig, DeferredStartup initializationReferredStartup) {
        this.initializationReferredStartup = initializationReferredStartup;
        sendToWorker(GameEngineControlPackage.Command.INITIALIZE, gameEngineConfig, userUiService.getUserContext());
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
    public void createHumanBaseWithBaseItem(int baseItemTypeId, DecimalPosition position) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_HUMAN_BASE_WITH_BASE_ITEM,
                userUiService.getUserContext().getLevelId(),
                userUiService.getUserContext().getUserId(),
                userUiService.getUserContext().getName(),
                baseItemTypeId,
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

    private void onPerfmonResponse(Collection<PerfmonStatistic> statisticEntries) {
        if (perfmonConsumer != null) {
            perfmonConsumer.accept(statisticEntries);
            perfmonConsumer = null;
        }
    }

    private void onInitialized(GroundUi groundUi, Collection<SlopeUi> slopeUis) {
        terrainUiService.setBuffers(groundUi, slopeUis);
        if (initializationReferredStartup != null) {
            initializationReferredStartup.finished();
            initializationReferredStartup = null;
        }
    }

    private void onInitialisingFailed(String errorText) {
        if (initializationReferredStartup != null) {
            initializationReferredStartup.failed(errorText);
            initializationReferredStartup = null;
        }
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case LOADED:
                onLoaded();
                break;
            case INITIALIZED:
                onInitialized((GroundUi) controlPackage.getData(0), (Collection<SlopeUi>)controlPackage.getData(1));
                break;
            case INITIALISING_FAILED:
                onInitialisingFailed((String) controlPackage.getSingleData());
                break;
            case TICK_UPDATE:
                baseItemUiService.updateSyncBaseItems((Collection<SyncBaseItemSimpleDto>) controlPackage.getData(0));
                gameUiControl.setGameInfo((GameInfo) controlPackage.getData(1));
                selectionHandler.baseItemRemoved((Collection<SyncBaseItemSimpleDto>) controlPackage.getData(2));
                selectionHandler.baseItemRemoved((Collection<SyncBaseItemSimpleDto>) controlPackage.getData(3));
                effectVisualizationService.baseItemRemoved((Collection<SyncBaseItemSimpleDto>) controlPackage.getData(2));
                effectVisualizationService.onSyncBaseItemsExplode((Collection<SyncBaseItemSimpleDto>) controlPackage.getData(3));
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
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }
}
