package com.btxtech.uiservice.control;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
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
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.cockpit.CockpitService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.BoxUiService;
import com.btxtech.uiservice.item.ResourceUiService;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.uiservice.tip.tiptask.CommandInfo;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.01.2017.
 */
public abstract class GameEngineControl {
    private Logger logger = Logger.getLogger(GameEngineControl.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private GameTipService gameTipService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private AudioService audioService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private CockpitService cockpitService;

    protected abstract void sendToWorker(GameEngineControlPackage.Command command, Object... data);

    public void start() {
        sendToWorker(GameEngineControlPackage.Command.START);
    }

    public void init(GameEngineConfig gameEngineConfig, UserContext userContext) {
        sendToWorker(GameEngineControlPackage.Command.INITIALIZE, gameEngineConfig, userContext);
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

    void createHumanBaseWithBaseItem(int levelId, int userId, String name, int baseItemTypeId, DecimalPosition position) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_HUMAN_BASE_WITH_BASE_ITEM, levelId, userId, name, baseItemTypeId, position);
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

    public void activateQuest(UserContext userContext, QuestConfig questConfig) {
        sendToWorker(GameEngineControlPackage.Command.ACTIVATE_QUEST, questConfig);
    }

    public void dropBoxes(List<BoxItemPosition> boxItemPositions) {
        sendToWorker(GameEngineControlPackage.Command.CREATE_BOXES, boxItemPositions);
    }

    public void updateLevel(int levelId) {
        sendToWorker(GameEngineControlPackage.Command.UPDATE_LEVEL, levelId);
    }

    protected void dispatch(GameEngineControlPackage controlPackage) {
        switch (controlPackage.getCommand()) {
            case INITIALIZED:
                logger.severe("!!!Initialized!!!!"); // TODO
                break;
            case STARTED:
                logger.severe("!!!Started!!!!");
                break;
            case TICK_UPDATE:
                baseItemUiService.updateSyncBaseItems((Collection<SyncBaseItemSimpleDto>) controlPackage.getData(0));
                gameUiControl.setGameInfo((GameInfo) controlPackage.getData(1));
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
                gameUiControl.onOnBoxPicked((BoxContent) controlPackage.getSingleData());
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + controlPackage.getCommand());
        }
    }
}
